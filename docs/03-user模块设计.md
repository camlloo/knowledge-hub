# user 模块设计（阶段①）

> 接口清单见 docs/02 §2。本设计补充实现层面的关键决策，确认后编码。

## 1. 接口设计（3 个）

### GET /v1/users/me —— 个人信息 + 配额

```
流程：SecurityUtils.getCurrentUserId() → getById → 组装 UserMeVO
返回 R<UserMeVO>：
{
  "user":  { id, username, nickname, email, avatar, role, storageQuota, storageUsed },  // 复用 UserInfoVO
  "quota": { quota, used, percentage }   // percentage = used/quota*100，保留 1 位小数；quota 为 0 时取 0，防除零
}
```

### PUT /v1/users/me —— 修改资料

```
body: { nickname?, email?, avatar? }   // 三个字段可选，传了才更新（null 忽略）
校验：nickname ≤50 字符；email @Email 且 ≤100；avatar ≤255
返回：R<UserMeVO>（最新数据，前端免二次请求）
```

**防提权硬规则**：`username / role / storageQuota / status` 四个字段即使前端传了也直接忽略——只白名单式更新上述三个可变字段。

### PUT /v1/users/me/password —— 修改密码（改完全端踢下线）

```
body: { oldPassword, newPassword(6~64) }
流程：① getById → ② BCrypt 验证旧密码（错 → 1004）
      → ③ 新旧密码相同拒绝（1005）→ ④ BCrypt 加密更新落库
      → ⑤ 撤销该用户全部 refreshToken（见 §2）
返回 R<Void>；前端收到成功后清除本地 token 跳登录页
```

错误码新增：`1004 OLD_PASSWORD_ERROR 原密码不正确`、`1005 NEW_PASSWORD_SAME 新密码不能与原密码相同`（同步 ErrorCode 枚举与 docs/02 表）。

## 2. 关键设计：改密码如何"踢下线"

**问题**：当前 Redis 结构是 `kb:token:refresh:{refreshToken} → userId`（按令牌反查），无法从 userId 找到某用户的全部会话；且 accessToken 是无状态 JWT，本身无法主动作废。

**方案：新增反向索引 SET**

```
kb:token:user-sessions:{userId}  =  SET{ refreshToken1, refreshToken2, ... }   TTL 7天
```

| 动作 | 对 SET 的维护 |
|---|---|
| 登录 / 刷新轮换 | SADD 新 refreshToken + EXPIRE 7d |
| 退出 | SREM 该 refreshToken + DEL 令牌 key |
| **改密码（revokeAllSessions）** | SMEMBERS → 逐个 DEL 令牌 key → DEL 整个 SET |

**明确的风险窗口**（与退出登录一致，注释中说明）：已签发的 accessToken 最多残留 2 小时。若后续要求强一致（如被盗号场景），升级路径是 token 版本号方案（user 表加 version 字段，过滤器比对），现阶段不做，代码留 TODO。

## 3. 模块依赖与编排（避免循环依赖）

依赖方向保持 `auth → user`（auth 已使用 UserService），因此 **user 模块不能反向调用 AuthService**。改密码的"改库 + 踢会话"两个动作由 UserController 薄编排：

```java
@PutMapping("/me/password")
public R<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    userService.updatePassword(userId, request);   // 验旧密码 + 更新（含 1004/1005 校验）
    authService.revokeAllSessions(userId);         // 踢掉全部会话（auth 模块职责）
    return R.ok();
}
```

`revokeAllSessions(Long userId)` 新增到 AuthService 接口；Redis 会话集合的维护（SADD/SREM）全部收敛在 AuthServiceImpl.buildTokenResponse / logout / refresh 内部，user 模块完全不感知 Redis。

## 4. DTO / VO（user/dto）

| 类 | 字段 |
|---|---|
| `UpdateMeRequest` | nickname(@Size 50)?、email(@Email @Size 100)?、avatar(@Size 255)? |
| `UpdatePasswordRequest` | oldPassword(@NotBlank)、newPassword(@NotBlank @Size 6~64) |
| `UserMeVO` | record(UserInfoVO user, QuotaVO quota) |
| `QuotaVO` | record(long quota, long used, double percentage)，静态工厂 `of(quota, used)` |

## 5. 改动清单

| 文件 | 改动 |
|---|---|
| `ErrorCode` | +1004、1005 |
| `AuthService` / `AuthServiceImpl` | +revokeAllSessions(userId)；buildTokenResponse/logout/refresh 维护 sessions SET |
| `UserService` / `UserServiceImpl` | +getMe / updateMe / updatePassword（注入 PasswordEncoder） |
| `UserController` | 实现 3 个端点（@Tag/@Operation + 流程 javadoc） |
| `user/dto` | +UpdateMeRequest、UpdatePasswordRequest、UserMeVO、QuotaVO |
| `docs/01` | Redis key 表 +user-sessions；`docs/02` | 错误码 +1004/1005 |

## 6. 验收清单

- 未带 token 访问 /users/me → 1010
- PUT /me 改昵称后 GET /me 生效；body 里塞 role=ADMIN 被忽略
- 改密码：旧密码错 → 1004；新旧相同 → 1005；成功后旧 refreshToken 调 /refresh → 1011，新密码可正常登录
- 空库新用户 GET /me 的 percentage = 0.0

# AGENTS.md —— 项目协作约定（AI 与开发者共同遵守）

> 每次生成/修改代码前先读本文件。与 docs/01-项目架构设计.md、docs/02-文件模块接口设计.md 配合使用。

## 一、注释规范（强制）

1. **Controller 接口方法**：javadoc 一行写清完整流程，用 `→` 串联步骤。例：
   `/** 登录：查库验密（BCrypt）→ 签发双令牌 → 返回令牌与用户信息 */`
2. **Service 方法体**：关键步骤用 `① ② ③` 编号行注释，写"做什么 + 为什么"，不复述代码字面。
3. **安全 / 并发 / 设计取舍处必须注释原因**：如防账号枚举（统一错误提示）、防重放（refreshToken 轮换先删旧值）、ThreadLocal 限制（异步线程需显式传 userId）。
4. **实体类与 DTO 字段**：每个字段一行注释，含义与数据库对齐。
5. **类级 javadoc**：说明职责、协作对象、关键策略（如令牌双轨制的取舍）。
6. **禁止废话注释**：`// 调用service`、`// getter/setter`、翻译代码字面的注释一律不写。
7. 注释语言：中文；`TODO` 标注未实现且有明确挂点的能力（如限流）。

## 二、已确立的工程约定

- 统一返回 `R<T>`：HTTP 恒为 200，业务结果看 `code`（ErrorCode 枚举）；未认证 = 1010。
- **userId 一律取自 `SecurityUtils.getCurrentUserId()`，不从请求参数接收**（防越权）。
- 异步/新线程中不可调 `SecurityUtils`（ThreadLocal），提交任务前取出 userId 显式传参。
- 实体不出参：对外一律用 VO（如 `UserInfoVO.from(user)`，防密码哈希泄露）。
- 模块化单体：跨模块只调用对方 Service 接口，禁止互摸 Mapper（后续用 ArchUnit 锁定）。
- 本地敏感配置写 `application-dev.yml`（已被 .gitignore 排除），同步更新 `application-dev.yml.example` 模板。
- 文档栈被兼容链锁死：springdoc 2.8.x（匹配 Boot 3.5）+ knife4j 4.5.0（`knife4j.enable=false`，仅 doc.html 静态页），勿改版本。
- 数据库变更：现阶段改 `sql/init.sql`；接入 Flyway 后走 `db/migration` 增量脚本。

## 三、目录速查

| 路径 | 内容 |
|---|---|
| `knowledge-hub/src/main/java/com/kh/<module>` | 模块化单体：auth/user/folder/file/tag/storage 已就绪，其余模块为 package-info 占位 |
| `knowledge-hub/src/main/resources/prompts/` | AI 提示词（阶段②起） |
| `docs/` | 架构设计（01）、接口设计（02） |
| `sql/init.sql` | 阶段①建表脚本（虚拟机 MySQL 执行） |

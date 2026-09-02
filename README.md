# AI Knowledge Hub

AI 原生知识文件管理系统。架构设计见 [docs/01-项目架构设计.md](docs/01-项目架构设计.md)。

## 项目结构

```
zCodeWord/
├── docs/                    # 设计文档
├── sql/                     # 数据库初始化脚本（在虚拟机 MySQL 执行）
├── knowledge-hub/           # 后端：Spring Boot 3.5 + JDK 21（模块化单体）
└── knowledge-hub-web/       # 前端：Vue 3 + TypeScript + Vite + Element Plus
```

## 环境说明

| 组件 | 地址 | 说明 |
|---|---|---|
| MySQL 8 | 192.168.150.101:3306 | 库名 `knowledge_hub`，先执行 `sql/init.sql` |
| Redis 7 | 192.168.150.101:6379 | |
| MinIO | 192.168.150.101:9000（API）/ 9001（控制台） | 需预先创建桶 `knowledge-hub` |

所有连接配置在 `knowledge-hub/src/main/resources/application-dev.yml`，账号密码按虚拟机实际情况修改（标了 TODO）。

## 后端启动

```bash
cd knowledge-hub
./mvnw spring-boot:run          # 或 mvn spring-boot:run（本机 Maven: D:\apache-maven-3.9.9）
```

- 接口前缀：`http://localhost:8080/api`
- **Knife4j 接口文档：`http://localhost:8080/api/doc.html`**
- 原生 Swagger UI：`http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/api/v3/api-docs`
- 健康检查：`http://localhost:8080/api/actuator/health`

> 文档栈说明：knife4j 4.5.0（doc.html 增强UI）+ springdoc 2.3.0（knife4j 官方适配版本）。knife4j 4.5.0 与 springdoc 2.8.x 存在 `getGroupConfigs()` 方法不兼容（NoSuchMethodError），故 springdoc 锁定 2.3.0；文档右上角 Authorize 已预置 `Authorization` 请求头，阶段① JWT 完成后可直接在文档里带 token 调试。

## 前端启动

```bash
cd knowledge-hub-web
npm install
npm run dev        # http://localhost:5173，/api 已代理到 localhost:8080
```

## 开发阶段

① 文件管理 → ② 文档解析+流水线 → ③ 全文检索 → ④ RAG → ⑤ 知识图谱 → ⑥ GraphRAG → ⑦ Agent → ⑧ 打磨

当前进度：**项目骨架初始化完成**，业务代码按阶段逐步实现（各模块职责见 `com.kh` 下 package-info）。

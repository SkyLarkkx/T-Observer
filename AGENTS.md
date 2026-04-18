# AGENTS.md

## 1. 适用范围与基线

本文件用于约束本仓库内的协作开发与自动化代理行为，适用于项目根目录及所有子目录。

实施基线以以下资料为准：

1. `doc/教师听课评课记录与分析系统需求规格说明书.md`
2. `doc/教师听课评课记录与分析系统系统架构设计文档.md`
3. 根目录 `pom.xml`
4. `t-observer-web/package.json`、`.editorconfig`、`.prettierrc.json`、`eslint.config.ts`

如代码与文档不一致，优先保持与 `doc/` 下的需求和架构文档一致，并在提交中说明差异原因。根据架构设计文档，当前仓库仍处于工程骨架阶段，新增代码应优先向目标分层结构收敛。

## 2. 技术栈与版本信息

### 后端

- Java 17
- Spring Boot `4.0.5`
- MyBatis Spring Boot Starter `4.0.1`
- Spring Validation
- Spring Web MVC
- Redis
- MySQL 8.x
- Lombok

### 前端

- Node.js `^20.19.0 || >=22.12.0`
- Vue `^3.5.31`
- Vue Router `^5.0.4`
- Pinia `^3.0.4`
- TypeScript `~6.0.0`
- Vite `^8.0.3`
- ESLint `^10.1.0`
- Oxlint `~1.57.0`
- Prettier `3.8.1`

### 部署与运行约束

- 架构模式：前后端分离 + 模块化单体 + 分层架构
- 前端通过 Vite 构建，产物由 Nginx 承载
- 后端以 Spring Boot 单体运行，可采用 Jar 或 Docker 部署
- 数据存储：MySQL
- 缓存与热点数据：Redis

## 3. 项目结构说明

当前仓库主要结构如下：

```text
T-Observer/
├─ doc/                        # 需求规格说明书、系统架构设计文档
├─ pom.xml                     # 根级 Maven 配置
├─ mvnw / mvnw.cmd             # Maven Wrapper
├─ t-observer-server/          # 后端代码骨架与资源
│  ├─ main/java/com/edu/tobserver
│  ├─ main/resources
│  └─ test/java
└─ t-observer-web/             # Vue 3 前端工程
   ├─ public/
   ├─ src/
   │  ├─ router/
   │  ├─ stores/
   │  ├─ App.vue
   │  └─ main.ts
   ├─ package.json
   └─ eslint / prettier / editorconfig 配置
```

根据架构设计文档，后续代码应逐步向以下目标结构收敛：

- 后端包结构：`com.edu.tobserver.<module>`
  - 推荐模块：`common`、`config`、`auth`、`masterdata`、`task`、`record`、`review`、`report`、`appeal`、`notice`、`audit`、`integration`
  - 每个业务模块内部推荐分层：`controller`、`dto`、`service`、`domain`、`mapper`、`model`
- 前端 `src/` 推荐结构：
  - `api`、`assets`、`components`、`layouts`、`router`、`stores`、`types`、`utils`、`views`

## 4. 代码规范

### 通用规范

- 文件编码统一使用 UTF-8。
- 前端行尾使用 LF。
- 优先使用语义化命名，禁止使用无业务含义的缩写。
- 需求编号、业务规则编号、状态机约束应与 `doc/` 文档保持可追踪。
- 关键业务状态变更、导出、审核、申诉、结果发布等操作必须考虑审计日志。

### 后端规范

- 包名全部小写，统一使用 `com.edu.tobserver` 作为根包。
- 类名使用 `PascalCase`，方法和字段使用 `camelCase`，常量使用 `UPPER_SNAKE_CASE`。
- 状态字段必须优先使用枚举表达，禁止在业务代码中散落硬编码状态字符串。
- Controller 统一使用 REST 风格路径，建议前缀为 `/api/...`。
- 请求参数校验使用 `@Valid`、`@Validated` 和 Bean Validation 注解。
- 写操作接口应统一返回结构：`code`、`message`、`data`、`traceId`。
- 模块职责按分层边界实现：
  - `controller` 负责入参校验、鉴权入口、响应封装
  - `service` 负责流程编排、事务控制
  - `domain` 负责业务规则、状态流转
  - `mapper` 负责数据访问
- 公共接口、复杂业务规则和状态流转建议补充简洁注释或 JavaDoc。
- Lombok 可用于样板代码收敛，但不要牺牲可读性。

### 前端规范

前端格式化规则以现有配置为准：

- 缩进：2 个空格
- `singleQuote: true`
- `semi: false`
- `printWidth: 100`

前端开发约定：

- Vue 单文件组件文件名使用 `PascalCase.vue`。
- 目录名按业务域使用小写，例如 `views/task`、`views/report`。
- 组合式逻辑、工具函数、store、变量、方法统一使用 `camelCase`。
- 类型声明集中放在 `src/types`，避免在页面中重复定义。
- 路由、状态管理、接口封装分别放入 `router`、`stores`、`api`。
- 页面表单需优先覆盖文档中的业务校验规则，例如必填项、评分范围、重复提交限制、时限控制。

### 注解与业务约束

- 与业务规则相关的校验必须前置，尤其是：
  - 听课记录提交时限
  - 同课程实例重复提交限制
  - 评分范围 `1.0 ~ 5.0`，步长 `0.5`
  - 退回必须附原因
  - 样本不足时不生成雷达图
- 审核、归档、申诉、导出、通知等关键链路必须保留可追踪信息。
- 涉及权限的页面和接口必须遵循 RBAC 约束。

## 5. 测试要求与检查命令

### 基本要求

- 提交前至少完成与改动范围对应的静态检查、类型检查、构建检查和测试。
- 新增业务规则时，必须补充覆盖正常流程和异常流程的测试。
- 涉及状态流转、参数校验、幂等控制、审计日志的改动，必须优先补充测试。
- 涉及接口契约变更时，前后端需同时完成联调检查。

### 建议检查命令

根目录后端检查：

```bash
./mvnw test
```

Windows 可使用：

```powershell
.\mvnw.cmd test
```

前端检查：

```bash
cd t-observer-web
npm run type-check
npm run lint
npm run build
```

按需格式化：

```bash
cd t-observer-web
npm run format
```

### 测试关注点

- 听课记录草稿、提交、退回、审核通过、归档的状态流转
- 重复记录校验
- 评分区间与精度校验
- 申诉时限校验
- 样本量不足时的降级处理
- 审计日志是否随事务一起写入

## 6. Git 提交规范

提交信息采用 Conventional Commits 风格：

```text
<type>(<scope>): <summary>
```

常用 `type`：

- `feat`：新功能
- `fix`：缺陷修复
- `refactor`：重构
- `docs`：文档变更
- `test`：测试补充或修复
- `chore`：构建、脚本、依赖等杂项
- `perf`：性能优化
- `build`：构建流程调整
- `ci`：持续集成相关

常用 `scope`：

- `server`
- `web`
- `doc`
- `auth`
- `task`
- `record`
- `review`
- `report`
- `appeal`
- `audit`

提交信息要求：

- 使用祈使语气，概述本次改动，不写流水账。
- 一次提交只解决一类问题，避免把功能、重构、格式化混在同一提交。
- 如果改动涉及需求或业务规则，正文中应引用对应编号，如 `FR-04`、`BR-06`。

示例：

```text
feat(record): 支持听课记录草稿保存与重复校验
fix(review): 补充退回原因为空时的服务端校验
docs(doc): 补充 AGENTS 协作规范与 PR 模板
```

## 7. PR 消息模板

请使用以下模板创建 Pull Request：

```markdown
## 背景

- 关联需求：FR-xx / BR-xx / NFR-xx
- 问题描述：

## 本次改动

1. 
2. 
3. 

## 影响范围

- 后端模块：
- 前端模块：
- 数据库/缓存：
- 接口契约：

## 验证方式

- [ ] `./mvnw test`
- [ ] `cd t-observer-web && npm run type-check`
- [ ] `cd t-observer-web && npm run lint`
- [ ] `cd t-observer-web && npm run build`
- [ ] 手工验证关键流程

## 风险与回滚

- 风险点：
- 回滚方案：

## 检查清单

- [ ] 代码结构符合项目分层规范
- [ ] 关键业务规则已补充校验
- [ ] 必要测试已补齐
- [ ] 审计、权限、异常流程已评估
- [ ] 文档已同步更新
```

## 8. 协作要求

- 任何实现都不得偏离 `doc/` 中定义的核心业务闭环：
  - 任务发布
  - 听课填报
  - 审核退回/通过
  - 汇总分析
  - 结果发布
  - 申诉复核
  - 周期归档
- 新增模块、接口、字段或状态前，先确认是否能映射到现有需求和架构文档。
- 若当前代码骨架与文档推荐结构存在差异，优先在不破坏现有可运行性的前提下向目标结构演进。

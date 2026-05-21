# P1-DB-01 Development Log

- Issue: #17
- Branch: `codex/p1-db-01-issue-17-group-foundation`
- Worktree: `.worktrees/p1-db-01-issue-17`
- Scope: 建立教研组组织底座
- Source docs: `doc/闭环增强版/MVP架构重构与闭环功能落地执行计划.md`, `doc/闭环增强版/教师听课评课记录与分析系统需求规格说明书-闭环增强版.md`, `doc/闭环增强版/闭环增强版后端架构设计.md`

## 2026-05-21 15:27

- Goal: 为多教研组权限模型补齐教研组主数据与成员关系表，并让 H2 初始化覆盖新增表
- Files: `src/main/resources/schema.sql`, `src/main/resources/schema-mysql.sql`, `src/test/java/com/edu/tobserver/bootstrap/BootstrapDataTest.java`
- Changes: 在 H2 与 MySQL schema 新增 `org_teaching_group`、`org_teaching_group_member`；补充 `uk_group_code`、`uk_group_user`、`idx_member_user_role`；在 `BootstrapDataTest` 增加新表存在断言
- Reason: `P1-BE-01` 和后续组级权限判断依赖稳定的组织底座，历史 MVP 仅有 `sys_user.role_code`，还不能表达“同一用户属于多个教研组且岗位不同”
- Verification: `.\mvnw.cmd "-Dspring.profiles.active=test" "-Dtest=BootstrapDataTest" test` -> pass，H2 成功加载新增表，测试断言 `ORG_TEACHING_GROUP`、`ORG_TEACHING_GROUP_MEMBER` 存在
- Risks: 历史用户尚无组归属初始化数据；MySQL schema 本次仅做脚本同步，未连接本地 MySQL 实例做启动验证

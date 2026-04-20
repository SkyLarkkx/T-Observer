# Task 7 任务列表与听课记录表单前端设计

## 范围

本设计文档覆盖 MVP 计划中的 Task 7，对应 GitHub issue `MVP Task 7: Build Task List and Record Form Frontend #8`。

本次设计只覆盖以下前端范围：

- 成员任务列表页 `t-observer-web/src/views/member/MemberTaskListView.vue`
- 听课记录填写页 `t-observer-web/src/views/member/RecordFormView.vue`
- 组长任务管理页 `t-observer-web/src/views/leader/LeaderTaskManageView.vue`
- 任务与记录 API 模块
- 任务与记录类型定义
- 任务状态标签组件
- 五维评分面板组件
- 路由接入与主导航调整
- Task 7 最小测试与构建验证方案

本次设计不包含以下内容：

- 后端接口扩展
- 成员选择下拉接口
- 听课记录详情查询接口
- 组长审核页
- 分析雷达图页
- 登录与鉴权架构调整

## 目标

- 让成员登录后能够查看自己被分配的听课任务。
- 让成员按任务状态筛选任务，并进入独立页面填写听课记录。
- 让成员可以保存草稿，并在满足校验条件后正式提交记录。
- 让组长在同一页面查看自己创建的任务，并通过抽屉快速创建新任务。
- 在不改变后端契约的前提下，把 Task 7 细化到可直接实施的程度，包括接口字段、页面状态、异常态与测试边界。

## 当前项目基线

当前仓库已经具备以下前置基础，可作为 Task 7 的直接承接点：

- 后端已存在认证、任务、记录、审核、分析模块。
- 后端已提供以下接口：
  - `GET /api/tasks`
  - `POST /api/tasks`
  - `POST /api/records/save-draft`
  - `POST /api/records/submit`
- 前端已完成 Task 6 的基础工作：
  - 登录页
  - 主布局 `MainLayout.vue`
  - 认证 store
  - 路由守卫
  - HTTP 客户端
  - 主题变量 `src/styles/ui-theme.css`

Task 7 应在这个现有基础上增量扩展，不替换 Task 6 已经建立的视觉风格、认证方式或主布局结构。

## 已确认的产品决策

- 范围以 MVP 计划文档中的 Task 7 为准，不延伸到 Task 8。
- 设计深度采用“可直接实施”标准，可以补全接口字段、交互状态、空态和异常态，但不改架构和后端契约。
- `LeaderTaskManageView` 采用“同页管理 + 抽屉创建任务”的方案。
- 成员端采用“任务列表页 + 独立记录填写页”的方案，不使用弹窗承载记录表单。
- 设计文档使用中文书写。

## 设计方案对比

### 方案 A：成员列表页内弹窗填写记录，组长页抽屉创建

优点：

- 路径最短
- 所有任务操作都停留在列表页

缺点：

- 听课记录表单字段较多，包含五维评分和三段文本，弹窗承载会明显拥挤
- 移动端体验较差
- 草稿与提交状态提示容易与列表上下文混在一起

### 方案 B：成员任务列表页 + 独立记录填写页，组长页抽屉创建

优点：

- 信息层级清晰，符合当前 MVP 复杂度
- 听课记录页可以完整展示任务摘要、评分区和文本区
- 桌面端和移动端都更容易做好可读性和可操作性
- 组长创建任务继续保持高效的同页抽屉流程

缺点：

- 成员填写记录需要多一次页面跳转

### 方案 C：统一任务中心页，按角色切换内容

优点：

- 路由数量少
- 看起来功能集中

缺点：

- 角色判断复杂
- 页面分支和条件渲染明显增多
- 不适合当前 MVP 的单任务渐进实现节奏

### 选型结论

采用方案 B：

- 成员端使用任务列表页和独立记录填写页
- 组长端使用任务管理页和抽屉创建

这个方案最符合当前 Task 7 的实际边界，也与现有主布局、路由结构和后端能力匹配。

## 视觉与交互方向

Task 7 延续 Task 6 已确认的前端设计基线：

- 主色使用 `#409EFF`
- 内容区背景使用 `#f5f7fa`
- 卡片、按钮、输入框等组件保持 `8px` 圆角基线
- 字体沿用系统默认无衬线栈
- 标题约 `18px`
- 正文约 `14px`
- 保持轻量、通透、蓝色主导的后台风格
- 复用 `t-observer-web/src/styles/ui-theme.css` 中已有设计变量

Task 7 页面新增的视觉要求如下：

- 成员任务列表优先使用卡片而不是密集表格，保证移动端可读性
- 组长任务管理桌面端使用轻表格，移动端退化为卡片
- 状态筛选区要足够清晰，但不使用厚重的分段控件边框
- 记录填写页要突出任务摘要和评分结构，避免长表单造成阅读疲劳

## 页面信息架构

### 1. 成员任务列表页 `MemberTaskListView`

页面目标：

- 展示当前成员自己的听课任务
- 提供状态筛选
- 提供进入记录填写页的入口

页面结构：

- 顶部标题区
  - 页面标题
  - 简短副标题
  - 当前角色提示
- 状态筛选区
  - `全部`
  - `待填写`
  - `进行中`
  - `已完成`
- 列表区
  - 任务卡片列表
- 空态区
  - 当前筛选结果为空时展示
- 错误态区
  - 首次加载失败时展示重试入口

任务卡片字段：

- `title`
- `teacherName`
- `courseName`
- `lessonTime`
- `deadline`
- `remark`
- `status`

任务卡片主操作：

- `PENDING`：显示“填写记录”
- `IN_PROGRESS`：显示“继续填写”
- `COMPLETED`：显示“查看记录”

交互规则：

- 成员端不显示 `observerId` 筛选能力
- 切换状态筛选时重新请求任务列表
- 点击任务操作按钮时跳转到 `/member/tasks/:taskId/record`
- 路由跳转时带上任务摘要信息，减少记录页首屏依赖

### 2. 听课记录填写页 `RecordFormView`

页面目标：

- 承载一次完整的听课记录填写
- 支持草稿保存
- 支持正式提交

页面结构：

- 顶部返回区
  - 返回任务列表按钮
  - 页面标题
  - 当前任务状态摘要
- 任务信息卡
  - 任务标题
  - 授课教师
  - 课程名称
  - 听课时间
  - 截止时间
  - 备注
- 五维评分区
  - 复用 `DimensionScorePanel`
- 文本填写区
  - 优点分析
  - 待改进项
  - 改进建议
- 底部操作区
  - 保存草稿
  - 提交记录
  - 提示文案

字段组织规则：

- 五个维度评分固定展示在文本区前面
- `teacherName` 在页面中只展示，不允许用户修改
- 三个文本域都使用较高的输入框，便于中等长度内容录入
- 每个文本域提供简洁的中文占位提示

### 3. 组长任务管理页 `LeaderTaskManageView`

页面目标：

- 展示当前组长创建的任务
- 在同一页面通过抽屉创建新任务

页面结构：

- 顶部标题区
  - 页面标题
  - 简短副标题
  - “新建任务”主按钮
- 筛选区
  - 状态筛选
- 列表区
  - 桌面端轻表格
  - 移动端卡片
- 新建任务抽屉
  - 任务标题
  - 听课成员 ID
  - 授课教师
  - 课程名称
  - 听课时间
  - 截止时间
  - 备注
  - 取消与提交按钮

交互规则：

- 点击“新建任务”后从右侧打开抽屉
- 创建成功后关闭抽屉并刷新列表
- 创建失败时保留用户输入内容

## 共用组件设计

### `src/components/common/StatusTag.vue`

职责：

- 接收任务状态码
- 输出统一的中文文案和视觉样式

覆盖状态：

- `PENDING`
- `IN_PROGRESS`
- `COMPLETED`

中文映射：

- `PENDING` -> `待填写`
- `IN_PROGRESS` -> `进行中`
- `COMPLETED` -> `已完成`

视觉建议：

- `PENDING`：浅橙底色
- `IN_PROGRESS`：浅蓝底色
- `COMPLETED`：浅绿底色

组件边界：

- 只负责状态展示
- 不承担业务判断和跳转逻辑

### `src/components/record/DimensionScorePanel.vue`

职责：

- 承载五个固定维度的评分输入
- 通过 `v-model` 与页面双向绑定

固定维度顺序：

- `TEACHING_DESIGN`
- `CLASSROOM_ORGANIZATION`
- `TEACHING_CONTENT`
- `INTERACTION_FEEDBACK`
- `TEACHING_EFFECTIVENESS`

中文名称映射：

- `TEACHING_DESIGN` -> `教学设计`
- `CLASSROOM_ORGANIZATION` -> `课堂组织`
- `TEACHING_CONTENT` -> `教学内容`
- `INTERACTION_FEEDBACK` -> `互动反馈`
- `TEACHING_EFFECTIVENESS` -> `教学效果`

交互要求：

- 支持首次空值初始化
- 支持草稿回填
- 每一行展示维度名称、评分控件、当前分值
- 组件内部不直接提交校验错误，只提供数据结构和基础输入约束

推荐控件：

- 使用 Element Plus `el-input-number` 或 `el-slider`
- 评分粒度保留一位小数
- 最小值 `1.0`
- 最大值 `5.0`
- 步长 `0.5`

说明：

后端只校验分数位于 `1.0` 到 `5.0` 范围内，没有规定必须是 `0.5` 的倍数。前端使用 `0.5` 步长是为了简化 MVP 交互，并保持评分选择稳定。

## 路由与导航设计

建议新增和调整以下路由：

- `/member/tasks`
- `/member/tasks/:taskId/record`
- `/leader/tasks`

导航策略：

- 侧边栏继续保留一个统一入口“任务”
- 成员登录后默认进入 `/member/tasks`
- 组长登录后默认进入 `/leader/tasks`

兼容策略：

- 当前存在的 `/tasks`、`/records` 预览路由不应继续作为真实业务入口
- 可以移除占位组件，或将其重定向到角色对应的任务页

推荐做法：

- `/tasks` 根据当前角色重定向：
  - `MEMBER` -> `/member/tasks`
  - `LEADER` -> `/leader/tasks`
- `/records` 不再作为一级导航入口，避免与记录填写详情页的概念冲突

## 前端类型设计

### `src/types/task.ts`

建议定义：

- `TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'`
- `TaskListItem`
  - `id: number`
  - `title: string`
  - `observerId: number`
  - `observerName: string`
  - `teacherName: string`
  - `courseName: string`
  - `lessonTime: string`
  - `deadline: string`
  - `status: TaskStatus`
  - `remark: string | null`
- `TaskQueryParams`
  - `status?: TaskStatus`
- `TaskCreatePayload`
  - `title: string`
  - `observerId: number`
  - `teacherName: string`
  - `courseName: string`
  - `lessonTime: string`
  - `deadline: string`
  - `remark?: string`

说明：

- 后端返回时间字段为 `LocalDateTime`，前端统一以字符串处理，并在页面层格式化展示
- 成员端不需要暴露 `observerId` 查询参数
- 组长创建任务仍使用 `observerId` 数值输入，因为当前后端未提供成员列表接口

### `src/types/record.ts`

建议定义：

- `DimensionCode`
  - `TEACHING_DESIGN`
  - `CLASSROOM_ORGANIZATION`
  - `TEACHING_CONTENT`
  - `INTERACTION_FEEDBACK`
  - `TEACHING_EFFECTIVENESS`
- `ScoreItem`
  - `dimensionCode: DimensionCode`
  - `dimensionName: string`
  - `scoreValue: number | null`
- `RecordDraftPayload`
  - `taskId: number`
  - `teacherName: string`
  - `strengths: string`
  - `weaknesses: string`
  - `suggestions: string`
  - `scores: ScoreItem[]`
- `RecordSubmitPayload`
  - 与 `RecordDraftPayload` 相同
- `ObservationRecord`
  - `id: number`
  - `taskId: number`
  - `observerId: number`
  - `teacherName: string`
  - `strengths: string`
  - `weaknesses: string`
  - `suggestions: string`
  - `status: string`
  - `rejectReason: string | null`
  - `submittedAt: string | null`
  - `approvedAt: string | null`
  - `scores: ScoreItem[]`

说明：

- 草稿结构与提交结构保持一致，降低页面层分叉
- 草稿允许文本为空和评分不完整
- 提交前由前端执行完整性校验

## API 契约设计

前端继续沿用当前 `ApiEnvelope<T>` 解包模式，页面层只接收 `data.data`。

### `src/api/tasks.ts`

提供：

- `fetchTasks(params?: TaskQueryParams)`
  - 请求 `GET /tasks`
  - 返回 `TaskListItem[]`
- `createTask(payload: TaskCreatePayload)`
  - 请求 `POST /tasks`
  - 返回创建后的任务对象

接口约束：

- `fetchTasks` 只接受可选状态筛选
- `createTask` 不封装额外转换逻辑，直接按后端契约提交

### `src/api/records.ts`

提供：

- `saveRecordDraft(payload: RecordDraftPayload)`
  - 请求 `POST /records/save-draft`
  - 返回 `ObservationRecord`
- `submitRecord(payload: RecordSubmitPayload)`
  - 请求 `POST /records/submit`
  - 返回 `ObservationRecord`

接口约束：

- 不新增记录详情查询函数
- 记录页的首屏数据依赖路由上下文而不是额外接口

## 页面状态机与交互细节

### 成员任务列表页状态机

状态：

- 初始态
- 加载态
- 成功态
- 空态
- 失败态

流程：

1. 页面进入后立即拉取任务列表
2. 请求中展示骨架卡片
3. 请求成功：
   - 有数据则展示任务卡片
   - 无数据则展示空态
4. 请求失败展示错误态和重试按钮
5. 切换状态筛选时重新拉取列表

成员页约束：

- 前端不显示他人任务筛选
- 不显示新建任务按钮
- 只保留和自身任务填写相关的主操作

### 记录填写页状态机

本地状态：

- `taskInfo`
- `scores`
- `strengths`
- `weaknesses`
- `suggestions`
- `isSavingDraft`
- `isSubmitting`

进入方式：

- 从成员任务列表点击进入
- 通过路由参数携带 `taskId`
- 通过路由 `query` 或导航状态携带任务摘要

如果缺少任务上下文：

- 页面展示错误态卡片
- 引导用户返回任务列表重新进入

草稿保存流程：

1. 用户点击“保存草稿”
2. 不做完整性强校验
3. 调用 `saveRecordDraft`
4. 成功后提示“草稿已保存”
5. 页面保持停留，继续编辑

正式提交流程：

1. 用户点击“提交记录”
2. 前端执行完整性校验
3. 弹出二次确认
4. 调用 `submitRecord`
5. 成功后提示“记录已提交”
6. 返回成员任务列表并刷新

提交前校验规则：

- `strengths` 非空
- `weaknesses` 非空
- `suggestions` 非空
- 评分数量为 5
- 五个维度必须齐全且不能重复
- 每项评分必须处于 `1.0 ~ 5.0`

已完成任务策略：

- 当任务状态为 `COMPLETED` 时，记录页进入只读展示模式
- 隐藏或禁用“提交记录”
- 隐藏或禁用“保存草稿”

原因：

- 当前后端允许更新已存在记录
- 如果前端继续放开编辑，会与“已完成”的用户认知冲突
- MVP 阶段以前端只读收口更稳妥

### 组长任务管理页状态机

状态：

- 列表初始态
- 列表加载态
- 列表成功态
- 列表空态
- 列表失败态
- 抽屉关闭态
- 抽屉打开态
- 创建中

创建流程：

1. 点击“新建任务”
2. 打开抽屉并初始化空表单
3. 用户填写表单
4. 前端做字段必填校验
5. 前端做时间顺序校验
6. 调用 `createTask`
7. 成功后关闭抽屉、重置表单并刷新列表
8. 失败时保留用户输入并展示错误提示

时间校验规则：

- 截止时间不能早于听课时间

## 文案与异常态设计

### 成员任务列表空态

- 标题：`暂无听课任务`
- 说明：`当前筛选条件下没有待处理任务，可稍后刷新查看。`

### 组长任务列表空态

- 标题：`暂无已创建任务`
- 说明：`可以先创建一条听课任务，分配给成员开始填写记录。`

### 记录页上下文缺失异常态

- 标题：`未找到任务信息`
- 说明：`请从任务列表重新进入该记录页面。`
- 操作按钮：`返回任务列表`

### 统一错误提示策略

- 草稿保存失败：
  - `草稿保存失败，请稍后重试`
- 提交失败：
  - 优先展示后端 message
  - 否则兜底为 `提交失败，请检查填写内容后重试`
- 创建任务失败：
  - 优先展示后端 message
  - 否则兜底为 `任务创建失败，请稍后重试`

### 状态文案统一

- `PENDING` -> `待填写`
- `IN_PROGRESS` -> `进行中`
- `COMPLETED` -> `已完成`

## 表单与校验设计

### 组长创建任务表单

字段：

- 任务标题
- 听课成员 ID
- 授课教师
- 课程名称
- 听课时间
- 截止时间
- 备注

校验：

- 标题必填
- 听课成员 ID 必填且必须是数字
- 授课教师必填
- 课程名称必填
- 听课时间必填
- 截止时间必填
- 截止时间不能早于听课时间

因为当前没有成员列表接口，`observerId` 使用数字输入框或普通文本框加数字校验，并在表单提示中说明示例格式。

### 听课记录表单

字段：

- 五个维度评分
- 优点分析
- 待改进项
- 改进建议

草稿保存：

- 允许为空
- 允许评分不完整

正式提交：

- 三段文本均必填
- 五个维度评分必须完整
- 每项评分必须合法

## 文件职责划分

### 新增文件

- `t-observer-web/src/types/task.ts`
- `t-observer-web/src/types/record.ts`
- `t-observer-web/src/api/tasks.ts`
- `t-observer-web/src/api/records.ts`
- `t-observer-web/src/components/common/StatusTag.vue`
- `t-observer-web/src/components/record/DimensionScorePanel.vue`
- `t-observer-web/src/views/member/MemberTaskListView.vue`
- `t-observer-web/src/views/member/RecordFormView.vue`
- `t-observer-web/src/views/leader/LeaderTaskManageView.vue`
- `t-observer-web/src/views/member/MemberTaskListView.spec.ts`

### 修改文件

- `t-observer-web/src/router/index.ts`

### 文件职责说明

- `task.ts`
  - 承载任务相关类型
- `record.ts`
  - 承载记录、评分与维度相关类型
- `tasks.ts`
  - 封装任务接口请求
- `records.ts`
  - 封装记录接口请求
- `StatusTag.vue`
  - 统一状态文案与色彩表达
- `DimensionScorePanel.vue`
  - 统一五维评分录入方式
- `MemberTaskListView.vue`
  - 成员任务筛选与列表展示
- `RecordFormView.vue`
  - 成员听课记录填写和提交
- `LeaderTaskManageView.vue`
  - 组长任务列表与创建抽屉
- `router/index.ts`
  - 角色对应路由和导航重定向逻辑

## 测试与验收策略

Task 7 计划文档要求的最小验证集为：

- `npx vitest run src/views/member/MemberTaskListView.spec.ts`
- `npm run type-check`
- `npm run build`

为了把实现风险降到可接受范围，建议按以下层级补充前端验证。

### 必做测试

`src/views/member/MemberTaskListView.spec.ts`

至少覆盖：

- 能渲染状态筛选项
- 能渲染任务卡片关键信息
- 不同状态展示正确操作文案
- 无数据时显示空态

### 建议补充测试

- `StatusTag.spec.ts`
  - 不同状态映射到正确中文文案
- `DimensionScorePanel.spec.ts`
  - 初始化五个维度
  - 修改评分时触发 `update:modelValue`
- `LeaderTaskManageView.spec.ts`
  - 点击按钮打开抽屉
  - 校验失败时不提交
- `RecordFormView.spec.ts`
  - 提交时缺少内容会提示校验错误
  - 草稿保存入口能触发对应调用

### 构建验收建议

- `cd t-observer-web`
- `npx vitest run src/views/member/MemberTaskListView.spec.ts`
- 如有新增 spec，再按文件补跑
- `npm run type-check`
- `npm run build`

## 风险与约束

- 不新增后端接口，避免偏离 Task 7 范围
- 不改动现有登录、鉴权和主布局主结构
- 不引入成员下拉选择数据源，组长创建任务先使用 `observerId`
- 不把 `/records` 继续保留为和记录填写页并列的一级业务入口
- 不在不同页面重复维护状态中文映射和维度中文映射，必须集中复用
- 不在已完成任务上继续开放可编辑提交，避免与状态认知冲突

## 实施结论

Task 7 的推荐实施方向如下：

- 成员端：任务列表页 + 独立记录填写页
- 组长端：任务管理页 + 同页抽屉创建
- 共用层：状态标签与五维评分组件
- 路由层：按角色收敛到真实任务页，移除占位页歧义
- 数据层：严格复用现有任务与记录接口，不扩后端

这个设计能够在不改变既有架构的前提下，把 Task 7 收敛成一组可直接实施、可测试、可 review 的前端改动。

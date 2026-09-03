---
version: 1
slug: "knowledge-hub-web-src-views-filesview-vue"
primary_target: "knowledge-hub-web/src/views/FilesView.vue"
related_targets: []
---

# Surface Brief — 文件库主界面（knowledge-hub-web）

## Scope & Mode

- Surface：登录/注册页 + 主布局（文件库/收藏/最近/回收站/标签）
- Mode：**Operate**（完成任务；可扫读、状态可见、熟悉的心智模型优先于表达）
- 受众：作者本人（开发者/学生），桌面 1280–1600 宽，中文界面

## Job / Task

上传资料 → 在目录树里组织 → 双模式浏览（表格/网格）→ 收藏/检索/下载/预览 → 回收站兜底。高频操作：上传、找文件、归类。

## Direction Contract

THESIS: 整个界面是清晨书桌上的一张白纸——窗边晨光均匀铺开，白色为主、墨色文字；拒绝暗色沉浸，也拒绝"又一个白底蓝网盘"的品类默认。
OWN-WORLD: 纯白内容面 + 浅暖灰骨架（#F7F6F1）做侧栏分隔；黄铜（#8F7038，白底加深的古铜）是唯一金属件与主行动色；印章红（#8C3B2E）只用于危险与失败。状态是"标记"而非颜色：斜盖小章（已就绪/编目中）、黄铜折角收藏标、回收站倒计时签、渐短的配额墨线。正文墨色 #26251F，次级 #6E6A5C（暖灰非纯灰）。
STORY: 访客立刻明白"我的文件在这里被认真编目"——状态可见、危险有门控、整理有秩序，且白天长时间使用不疲劳。
FIRST VIEWPORT: 左 264px 浅灰侧栏（导航 + 目录树 + 标签 + 配额墨线），60px 白顶栏（面包屑、全局搜索、黄铜上传主按钮、头像）；主区纯白光池内是双模式文件列表（表格默认），行=折角标+文件名+标签+大小+时间+状态章+操作。
FORM: 晨光书房（由「深夜阅览室」按用户要求翻转为浅色系，标记语言全部保留），seed key 6723f03d；code-led 构建，无 comp——雄心由本契约 FIRST VIEWPORT 与签名交互承载。
SIGNATURE: "盖章"——文件行操作完成（收藏/删除）时状态章以一次短促的 scale+rotate 落下；目录树切换时光池轻微呼吸（背景 2% 过渡）。
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance.

## Constraints

- Vue3 + TS + Vite + Pinia + Element Plus（已脚手架）；EP 主题变量需重映射到本世界（primary=黄铜）
- 后端 R<T>：HTTP 200 + code；1010 → 静默刷新 → 失败跳登录
- 后端 folder/file/tag/storage 接口未实现：UI 先行，空态/错误态必须优雅

## Unresolved

- 头像暂用首字符圆章；MinIO 头像上传后端就绪后接入

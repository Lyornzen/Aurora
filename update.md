# Aurora 更新日志

## 2026-06-12 代码清理与冗余移除

### 一、删除的文件（4个）

| 文件 | 原因 |
|------|------|
| `viewmodel/ChatViewModel.kt` | 完全未使用 — 没有任何代码引用它，ChatScreen 直接管理自己的状态 |
| `data/LocaleManager.kt` | 完全未使用 — 没有任何代码调用其方法 |
| `data/UserProfile.kt` | 用户档案/登录系统 — 应用不需要登录功能 |

### 二、清理的冗余代码

#### ChatScreen.kt
- 提取 `buildApiContent()` 辅助函数，消除 `send` 和 `resendFrom` 中 3 处完全相同的附件内容构建逻辑（每处约 15 行）
- 移除首次启动欢迎对话框（要求输入昵称）及相关状态变量
- 简化 HeroCard，移除 `displayName` 参数
- 移除 12 个无用导入（`AnimatedVisibility`、`expandVertically`、`fadeIn`、`fadeOut`、`scaleIn`、`scaleOut`、`shrinkVertically`、`slideInVertically`、`spring`、`tween`、`CircularProgressIndicator`、`Icons.Rounded.Stop`）

#### ProfileScreen.kt
- 移除用户头像卡片（昵称首字母、"AI Assistant" 标签）
- 移除 "Sign Out" 按钮（空操作，无实际功能）
- 移除重复导入：`rememberScrollState`（×2→1）、`verticalScroll`（×2→1）、`items`（×2→1）
- 移除 7 个无用动画导入

#### ChatSession.kt
- 移除未使用的 `mutableStateOf` 导入

#### MainActivity.kt
- 移除 `UserProfile.init(this)` 初始化调用及导入

#### Color.kt
- 移除 30+ 个未使用的颜色常量（`Secondary*`、`Tertiary*`、`Error*`、`Background*`、`OnSurface`、`Outline*`、`Dark*` 系列）
- 保留 10 个实际被引用的颜色

#### strings.xml
- 移除 13 个未使用的字符串资源：
  - 登录相关：`profile_sign_out`、`welcome_title`、`welcome_hint`、`welcome_name_placeholder`、`profile_api_keys_count`
  - 未引用：`profile_no_models`、`profile_no_models_hint`、`profile_version`、`profile_language`、`voice_listening`、`voice_error`、`voice_no_result`、`file_attached`、`file_too_large`、`branch_created`、`token_usage`、`label_more`

### 三、清理效果

- 删除 3 个冗余文件，精简 6 个源文件
- 消除 ChatScreen 中的重复代码（附件构建逻辑）
- 移除所有登录/用户档案相关功能
- 清理 30+ 个无用颜色常量和 13 个无用字符串资源
- 清理 20+ 个无用 import 声明

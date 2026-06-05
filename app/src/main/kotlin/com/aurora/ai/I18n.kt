package com.aurora.ai

import androidx.compose.runtime.compositionLocalOf

// ============================================================
// i18n — String resources for all supported languages
// ============================================================

data class I18nStrings(
    // Nav
    val navChat: String,
    val navTasks: String,
    val navLinks: String,
    val navHistory: String,
    val navProfile: String,
    // Chat
    val chatGreeting: String,
    val chatHello: String,
    val chatAskPlaceholder: String,
    val chatTryThese: String,
    val chatSelectModel: String,
    // Tasks
    val tasksTitle: String,
    val tasksNewTask: String,
    val tasksRunning: String,
    val tasksCompleted: String,
    val tasksPaused: String,
    val tasksFailed: String,
    val tasksPause: String,
    val tasksCancel: String,
    val tasksTimeline: String,
    // Links
    val linksTitle: String,
    val linksOnline: String,
    val linksOffline: String,
    val linksConnected: String,
    val linksDisconnected: String,
    val linksResources: String,
    val linksActions: String,
    // History
    val historyTitle: String,
    val historySearch: String,
    val historyToday: String,
    val historyYesterday: String,
    val historyThisWeek: String,
    // Profile
    val profileTitle: String,
    val profileEdit: String,
    val profileSettings: String,
    val profileAppearance: String,
    val profileLanguage: String,
    val profileApiSettings: String,
    val profileModelSettings: String,
    val profileAgentSettings: String,
    val profileAbout: String,
    val profileLogout: String,
    val profileLight: String,
    val profileDark: String,
    val profileSystem: String,
    val profileRequests: String,
    val profileTokens: String,
    val profileModels: String,
    // API
    val apiTitle: String,
    val apiDescription: String,
    val apiProvider: String,
    val apiKey: String,
    val apiEndpoint: String,
    val apiOptional: String,
    val apiSave: String,
    val apiAddKey: String,
    // Common
    val commonCopy: String,
    val commonShare: String,
    val commonRegenerate: String,
    val commonSearch: String,
    val commonDelete: String,
    val commonFavorite: String,
    val commonExport: String,
)

val enStrings = I18nStrings(
    navChat = "Chat", navTasks = "Tasks", navLinks = "Links", navHistory = "History", navProfile = "Profile",
    chatGreeting = "", chatHello = "Hello, %s", chatAskPlaceholder = "Ask anything...", chatTryThese = "Try these", chatSelectModel = "Select Model",
    tasksTitle = "Tasks", tasksNewTask = "New Task", tasksRunning = "Running", tasksCompleted = "Completed", tasksPaused = "Paused", tasksFailed = "Failed",
    tasksPause = "Pause", tasksCancel = "Cancel", tasksTimeline = "Execution Timeline",
    linksTitle = "Links", linksOnline = "Online", linksOffline = "Offline", linksConnected = "Connected", linksDisconnected = "Disconnected",
    linksResources = "System Resources", linksActions = "Remote Actions",
    historyTitle = "History", historySearch = "Search conversations & tasks...", historyToday = "Today", historyYesterday = "Yesterday", historyThisWeek = "This Week",
    profileTitle = "Profile", profileEdit = "Edit", profileSettings = "Settings", profileAppearance = "Appearance", profileLanguage = "Language",
    profileApiSettings = "API Settings", profileModelSettings = "Model Settings", profileAgentSettings = "Agent Settings", profileAbout = "About",
    profileLogout = "Log Out", profileLight = "Light", profileDark = "Dark", profileSystem = "System",
    profileRequests = "Requests", profileTokens = "Tokens", profileModels = "Models",
    apiTitle = "API Settings", apiDescription = "Manage your API keys and endpoints.", apiProvider = "Provider Name",
    apiKey = "API Key", apiEndpoint = "API Endpoint (optional)", apiOptional = "https://api.example.com/v1", apiSave = "Save API Key", apiAddKey = "Add API Key",
    commonCopy = "Copy", commonShare = "Share", commonRegenerate = "Regenerate", commonSearch = "Search",
    commonDelete = "Delete", commonFavorite = "Favorite", commonExport = "Export",
)

val zhStrings = I18nStrings(
    navChat = "对话", navTasks = "任务", navLinks = "连接", navHistory = "历史", navProfile = "我的",
    chatGreeting = "", chatHello = "你好，%s", chatAskPlaceholder = "想问什么...", chatTryThese = "试试这些", chatSelectModel = "选择模型",
    tasksTitle = "任务", tasksNewTask = "新建任务", tasksRunning = "运行中", tasksCompleted = "已完成", tasksPaused = "已暂停", tasksFailed = "失败",
    tasksPause = "暂停", tasksCancel = "取消", tasksTimeline = "执行时间线",
    linksTitle = "连接", linksOnline = "在线", linksOffline = "离线", linksConnected = "已连接", linksDisconnected = "未连接",
    linksResources = "系统资源", linksActions = "远程操作",
    historyTitle = "历史", historySearch = "搜索对话和任务...", historyToday = "今天", historyYesterday = "昨天", historyThisWeek = "本周",
    profileTitle = "我的", profileEdit = "编辑", profileSettings = "设置", profileAppearance = "外观", profileLanguage = "语言",
    profileApiSettings = "API 设置", profileModelSettings = "模型设置", profileAgentSettings = "智能体设置", profileAbout = "关于",
    profileLogout = "退出登录", profileLight = "浅色", profileDark = "深色", profileSystem = "跟随系统",
    profileRequests = "请求数", profileTokens = "Token", profileModels = "模型",
    apiTitle = "API 设置", apiDescription = "管理你的 API 密钥和端点。", apiProvider = "服务商名称",
    apiKey = "API 密钥", apiEndpoint = "API 端点（可选）", apiOptional = "https://api.example.com/v1", apiSave = "保存 API 密钥", apiAddKey = "添加 API 密钥",
    commonCopy = "复制", commonShare = "分享", commonRegenerate = "重新生成", commonSearch = "搜索",
    commonDelete = "删除", commonFavorite = "收藏", commonExport = "导出",
)

val jaStrings = I18nStrings(
    navChat = "チャット", navTasks = "タスク", navLinks = "リンク", navHistory = "履歴", navProfile = "プロフィール",
    chatGreeting = "", chatHello = "こんにちは、%s", chatAskPlaceholder = "何でも聞いて...", chatTryThese = "お試し", chatSelectModel = "モデル選択",
    tasksTitle = "タスク", tasksNewTask = "新規タスク", tasksRunning = "実行中", tasksCompleted = "完了", tasksPaused = "一時停止", tasksFailed = "失敗",
    tasksPause = "一時停止", tasksCancel = "キャンセル", tasksTimeline = "実行タイムライン",
    linksTitle = "リンク", linksOnline = "オンライン", linksOffline = "オフライン", linksConnected = "接続済", linksDisconnected = "未接続",
    linksResources = "システムリソース", linksActions = "リモート操作",
    historyTitle = "履歴", historySearch = "会話とタスクを検索...", historyToday = "今日", historyYesterday = "昨日", historyThisWeek = "今週",
    profileTitle = "プロフィール", profileEdit = "編集", profileSettings = "設定", profileAppearance = "外観", profileLanguage = "言語",
    profileApiSettings = "API設定", profileModelSettings = "モデル設定", profileAgentSettings = "エージェント設定", profileAbout = "情報",
    profileLogout = "ログアウト", profileLight = "ライト", profileDark = "ダーク", profileSystem = "システム",
    profileRequests = "リクエスト", profileTokens = "トークン", profileModels = "モデル",
    apiTitle = "API設定", apiDescription = "APIキーとエンドポイントを管理します。", apiProvider = "プロバイダ名",
    apiKey = "APIキー", apiEndpoint = "APIエンドポイント（任意）", apiOptional = "https://api.example.com/v1", apiSave = "APIキーを保存", apiAddKey = "APIキーを追加",
    commonCopy = "コピー", commonShare = "共有", commonRegenerate = "再生成", commonSearch = "検索",
    commonDelete = "削除", commonFavorite = "お気に入り", commonExport = "エクスポート",
)

val koStrings = I18nStrings(
    navChat = "채팅", navTasks = "작업", navLinks = "링크", navHistory = "기록", navProfile = "프로필",
    chatGreeting = "", chatHello = "안녕하세요, %s", chatAskPlaceholder = "무엇이든 물어보세요...", chatTryThese = "추천", chatSelectModel = "모델 선택",
    tasksTitle = "작업", tasksNewTask = "새 작업", tasksRunning = "실행 중", tasksCompleted = "완료됨", tasksPaused = "일시중지", tasksFailed = "실패",
    tasksPause = "일시중지", tasksCancel = "취소", tasksTimeline = "실행 타임라인",
    linksTitle = "링크", linksOnline = "온라인", linksOffline = "오프라인", linksConnected = "연결됨", linksDisconnected = "연결 안 됨",
    linksResources = "시스템 리소스", linksActions = "원격 작업",
    historyTitle = "기록", historySearch = "대화 및 작업 검색...", historyToday = "오늘", historyYesterday = "어제", historyThisWeek = "이번 주",
    profileTitle = "프로필", profileEdit = "편집", profileSettings = "설정", profileAppearance = "화면", profileLanguage = "언어",
    profileApiSettings = "API 설정", profileModelSettings = "모델 설정", profileAgentSettings = "에이전트 설정", profileAbout = "정보",
    profileLogout = "로그아웃", profileLight = "라이트", profileDark = "다크", profileSystem = "시스템",
    profileRequests = "요청", profileTokens = "토큰", profileModels = "모델",
    apiTitle = "API 설정", apiDescription = "API 키와 엔드포인트를 관리합니다.", apiProvider = "제공업체 이름",
    apiKey = "API 키", apiEndpoint = "API 엔드포인트 (선택)", apiOptional = "https://api.example.com/v1", apiSave = "API 키 저장", apiAddKey = "API 키 추가",
    commonCopy = "복사", commonShare = "공유", commonRegenerate = "다시 생성", commonSearch = "검색",
    commonDelete = "삭제", commonFavorite = "즐겨찾기", commonExport = "내보내기",
)

fun stringsFor(langIndex: Int): I18nStrings = when (langIndex) {
    0 -> enStrings
    1 -> zhStrings
    2 -> jaStrings
    3 -> koStrings
    else -> enStrings
}

val LocalI18n = compositionLocalOf { enStrings }

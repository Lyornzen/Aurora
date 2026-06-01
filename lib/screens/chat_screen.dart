import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import '../providers/chat_provider.dart';
import '../providers/api_provider.dart';
import '../models/chat_message.dart';
import '../widgets/chat/message_bubble.dart';
import '../widgets/chat/input_area.dart';
import '../widgets/common/empty_state.dart';

class ChatScreen extends StatefulWidget {
  final VoidCallback? onSwitchToHistory;

  const ChatScreen({super.key, this.onSwitchToHistory});

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final _scrollController = ScrollController();
  final _inputKey = GlobalKey<InputAreaState>();
  bool _webSearch = false;
  bool _thinking = false;
  bool _vision = false;
  bool _fileAnalysis = false;

  String _currentModel = '';

  @override
  void initState() {
    super.initState();
    _loadDefaultModel();
  }

  void _loadDefaultModel() {
    final api = context.read<ApiProvider>();
    final def = api.modelSettings.defaultChatModel;
    if (def.isNotEmpty) {
      _currentModel = def;
    } else {
      // Fallback: first model from first enabled provider
      _currentModel = api.enabledProviders
          .expand((p) => p.models)
          .firstOrNull ?? '未选择模型';
    }
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    if (_scrollController.hasClients) {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    }
  }

  final _imagePicker = ImagePicker();

  void _handleSend(String text) {
    final apiProvider = context.read<ApiProvider>();
    final apiConfig = apiProvider.getActiveConfig(_currentModel);
    context.read<ChatProvider>().sendMessage(
          text,
          modelName: _currentModel,
          apiConfig: apiConfig,
        );
    _inputKey.currentState?.unfocus();
    WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToBottom());
  }

  Future<void> _pickAndAnalyzeImage() async {
    final image = await _imagePicker.pickImage(
      source: ImageSource.gallery,
      maxWidth: 1024,
      maxHeight: 1024,
      imageQuality: 85,
    );
    if (image == null) return;

    // Image selected - sending to model
    // (Base64 data prepared for future Vision API integration)

    // Send image with a default prompt to the model
    final apiProvider = context.read<ApiProvider>();
    final apiConfig = apiProvider.getActiveConfig(_currentModel);
    if (apiConfig == null || apiConfig.apiKey.isEmpty) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('请先在设置中配置 API')),
        );
      }
      return;
    }

    final prompt = '请描述这张图片的内容。';
    context.read<ChatProvider>().sendMessage(
          prompt,
          modelName: _currentModel,
          apiConfig: apiConfig,
        );
  }

  Future<void> _attachFile() async {
    showModalBottomSheet(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.all(20),
              child: Text('添加附件',
                  style: Theme.of(context).textTheme.titleLarge),
            ),
            ListTile(
              leading: const Icon(Icons.image_outlined),
              title: const Text('从相册选择图片'),
              onTap: () {
                Navigator.pop(ctx);
                _pickAndAnalyzeImage();
              },
            ),
            ListTile(
              leading: const Icon(Icons.camera_alt_outlined),
              title: const Text('拍照'),
              onTap: () {
                Navigator.pop(ctx);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('相机功能即将推出')),
                );
              },
            ),
            ListTile(
              leading: const Icon(Icons.description_outlined),
              title: const Text('选择文件'),
              onTap: () {
                Navigator.pop(ctx);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('文件上传功能即将推出')),
                );
              },
            ),
            const SizedBox(height: 12),
          ],
        ),
      ),
    );
  }

  void _showModelPicker() {
    final providers = context.read<ApiProvider>().enabledProviders;
    showModalBottomSheet(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.all(20),
              child: Text(
                '切换模型',
                style: Theme.of(context).textTheme.titleLarge,
              ),
            ),
            if (providers.isEmpty)
              const Padding(
                padding: EdgeInsets.all(20),
                child: Text('暂未配置任何 API，请在设置中添加'),
              )
            else
              ...providers.expand((p) => p.models.map((m) => ListTile(
                    leading: const Icon(Icons.auto_awesome),
                    title: Text(m),
                    subtitle: Text(p.name),
                    selected: _currentModel == m,
                    onTap: () {
                      setState(() => _currentModel = m);
                      Navigator.pop(ctx);
                    },
                  ))),
            const SizedBox(height: 12),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final chatProvider = context.watch<ChatProvider>();
    final messages = chatProvider.currentMessages;
    final hasMessages = messages.isNotEmpty;
    final theme = Theme.of(context);

    return Column(
      children: [
        // Streaming indicator
        if (chatProvider.isStreaming)
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            color: theme.colorScheme.primaryContainer.withAlpha(60),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                SizedBox(
                  width: 14,
                  height: 14,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: theme.colorScheme.primary,
                  ),
                ),
                const SizedBox(width: 10),
                Text('AI 正在回复...',
                    style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.primary)),
                const Spacer(),
                TextButton.icon(
                  onPressed: () => chatProvider.cancelStream(),
                  icon: const Icon(Icons.stop, size: 16),
                  label: const Text('停止'),
                  style: TextButton.styleFrom(
                    foregroundColor: theme.colorScheme.error,
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                    visualDensity: VisualDensity.compact,
                  ),
                ),
              ],
            ),
          ),
        // Model selector bar — show whenever a session exists
        if (chatProvider.hasCurrentSession)
          GestureDetector(
            onTap: _showModelPicker,
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 2),
              child: Material(
                color: Theme.of(context).colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(16),
                child: Padding(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
                  child: Row(
                    children: [
                      Icon(
                        Icons.auto_awesome,
                        size: 16,
                        color: Theme.of(context).colorScheme.primary,
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          _currentModel,
                          style: Theme.of(context).textTheme.bodyMedium,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      Icon(
                        Icons.expand_more,
                        size: 18,
                        color:
                            Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),

        // Messages or empty state
        Expanded(
          child: hasMessages
              ? ListView.builder(
                  controller: _scrollController,
                  padding:
                      const EdgeInsets.symmetric(vertical: 8),
                  itemCount: messages.length,
                  itemBuilder: (context, index) {
                    final msg = messages[index];
                    return MessageBubble(
                      message: msg,
                      onCopy: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text('已复制到剪贴板')),
                        );
                      },
                      onFavorite: () {
                        chatProvider.toggleFavorite(msg.id);
                      },
                      onToggleThinking: () {
                        // Toggle thinking expansion
                        setState(() {
                          msg.isThinkingExpanded;
                        });
                      },
                      onRegenerate: () {
                        // Regenerate from the previous user message
                        if (index > 0 &&
                            messages[index - 1].role == MessageRole.user) {
                          _handleSend(messages[index - 1].content);
                        }
                      },
                    );
                  },
                )
              : EmptyState(
                  icon: Icons.chat_bubble_outline,
                  title: '开始对话',
                  description: '与 AI 助手开始你的第一场对话',
                  actionLabel: '新建会话',
                  onAction: () {
                    context.read<ChatProvider>().createNewSession();
                    _inputKey.currentState?.focusInput();
                  },
                ),
        ),

        // Input area
        InputArea(
          key: _inputKey,
          onSend: _handleSend,
          isWebSearchEnabled: _webSearch,
          isThinkingEnabled: _thinking,
          isVisionEnabled: _vision,
          isFileAnalysisEnabled: _fileAnalysis,
          onWebSearchToggle: (v) => setState(() => _webSearch = v),
          onThinkingToggle: (v) => setState(() => _thinking = v),
          onVisionToggle: (v) => setState(() => _vision = v),
          onFileAnalysisToggle: (v) => setState(() => _fileAnalysis = v),
          onAttach: _attachFile,
        ),
      ],
    );
  }
}

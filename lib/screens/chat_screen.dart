import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import '../providers/chat_provider.dart';
import '../providers/api_provider.dart';
import '../models/chat_message.dart';
import '../widgets/chat/message_bubble.dart';
import '../widgets/chat/input_area.dart';
import '../theme/app_theme.dart';
import '../theme/app_motion.dart';

class ChatScreen extends StatefulWidget {
  final ValueChanged<int>? onSwitchTab;
  const ChatScreen({super.key, this.onSwitchTab});

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final _scrollCtrl = ScrollController();
  final _inputKey = GlobalKey<InputAreaState>();
  bool _thinking = false, _maxReasoning = false, _isNearBottom = true, _showJump = false;
  String _currentModel = '';
  final _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _loadModel();
    _scrollCtrl.addListener(_onScroll);
  }

  void _loadModel() {
    final api = context.read<ApiProvider>();
    final def = api.modelSettings.defaultChatModel;
    _currentModel = def.isNotEmpty ? def : api.enabledProviders.expand((p) => p.models).firstOrNull ?? '未选择模型';
  }

  void _onScroll() {
    if (!_scrollCtrl.hasClients) return;
    final near = _scrollCtrl.position.pixels >= _scrollCtrl.position.maxScrollExtent - 100;
    if (near != _isNearBottom) setState(() => _isNearBottom = near);
    final streaming = context.read<ChatProvider>().isStreaming;
    if (streaming && !near && !_showJump) setState(() => _showJump = true);
    if (!streaming && _showJump) setState(() => _showJump = false);
  }

  void _scrollBottom() {
    if (_scrollCtrl.hasClients && _isNearBottom) {
      _scrollCtrl.animateTo(_scrollCtrl.position.maxScrollExtent, duration: const Duration(milliseconds: 200), curve: Curves.easeOut);
    }
  }

  void _jumpBottom() {
    if (_scrollCtrl.hasClients) _scrollCtrl.animateTo(_scrollCtrl.position.maxScrollExtent, duration: const Duration(milliseconds: 200), curve: Curves.easeOut);
    setState(() => _showJump = false);
  }

  void _send(String text) {
    final api = context.read<ApiProvider>();
    context.read<ChatProvider>().sendMessage(text, modelName: _currentModel, apiConfig: api.getActiveConfig(_currentModel), enableThinking: _thinking, reasoningEffort: _maxReasoning ? 'max' : 'high');
    _inputKey.currentState?.unfocus();
    WidgetsBinding.instance.addPostFrameCallback((_) => _scrollBottom());
  }

  void _toggleMax() {
    setState(() => _maxReasoning = !_maxReasoning);
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(_maxReasoning ? 'Max 推理强度' : '默认推理强度 (High)'), duration: const Duration(seconds: 1)));
  }

  void _showModelPicker() {
    final providers = context.read<ApiProvider>().enabledProviders;
    showModalBottomSheet(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Padding(padding: EdgeInsets.all(20), child: Text('切换模型', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w600))),
          if (providers.isEmpty)
            const Padding(padding: EdgeInsets.all(20), child: Text('暂未配置 API，请在设置中添加'))
          else
            ...providers.expand((p) => p.models.map((m) => ListTile(
                  leading: const Icon(Icons.auto_awesome),
                  title: Text(m),
                  subtitle: Text(p.name),
                  selected: _currentModel == m,
                  onTap: () { setState(() => _currentModel = m); Navigator.pop(ctx); },
                ))),
          const SizedBox(height: 12),
        ]),
      ),
    );
  }

  @override
  void dispose() { _scrollCtrl.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    final chat = context.watch<ChatProvider>();
    final messages = chat.currentMessages;
    final hasMsgs = messages.isNotEmpty;
    final theme = Theme.of(context);

    if (chat.isStreaming && _isNearBottom) {
      WidgetsBinding.instance.addPostFrameCallback((_) => _scrollBottom());
    }

    return Scaffold(
      body: Column(children: [
        if (!hasMsgs) _greetingCard(theme),
        if (chat.isStreaming) _streamingBar(theme, chat),
        _modelBar(theme),
        Expanded(
          child: hasMsgs
              ? Stack(children: [
                  ListView.builder(
                    controller: _scrollCtrl,
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    itemCount: messages.length,
                    itemBuilder: (ctx, i) => TweenAnimationBuilder<double>(
                      key: ValueKey(messages[i].id),
                      tween: Tween(begin: 0.0, end: 1.0),
                      duration: AppMotion.messageDuration,
                      curve: AppMotion.emphasizedDecelerate,
                      builder: (c, v, child) => Opacity(opacity: v, child: Transform.translate(offset: Offset(0, (1 - v) * 16), child: child)),
                      child: MessageBubble(
                        message: messages[i],
                        onCopy: () => ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('已复制'))),
                        onFavorite: () => chat.toggleFavorite(messages[i].id),
                        onRegenerate: () { if (i > 0 && messages[i - 1].role == MessageRole.user) _send(messages[i - 1].content); },
                      ),
                    ),
                  ),
                  if (_showJump) Positioned(bottom: 8, right: 8, child: FloatingActionButton.small(heroTag: 'jump', onPressed: _jumpBottom, backgroundColor: theme.colorScheme.primaryContainer, child: Icon(Icons.keyboard_arrow_down, color: theme.colorScheme.onPrimaryContainer))),
                ])
              : _emptyState(theme),
        ),
        _suggestedChips(theme, !hasMsgs),
        InputArea(key: _inputKey, onSend: _send, isThinkingEnabled: _thinking, reasoningEffort: _maxReasoning ? 'max' : 'high',
          onThinkingToggle: (v) => setState(() => _thinking = v), onReasoningPick: _thinking ? _toggleMax : null, onAttach: _attachFile),
      ]),
    );
  }

  Widget _greetingCard(ThemeData theme) {
    final hour = DateTime.now().hour;
    final greeting = hour < 12 ? '上午好' : hour < 18 ? '下午好' : '晚上好';
    return Container(
      width: double.infinity,
      height: 180,
      padding: const EdgeInsets.all(AuroraSpacing.xl),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: theme.brightness == Brightness.light
              ? [AuroraColors.gradientStart, AuroraColors.gradientEnd]
              : [AuroraColors.darkGradientStart, AuroraColors.darkGradientEnd],
          begin: Alignment.topLeft, end: Alignment.bottomRight,
        ),
        borderRadius: const BorderRadius.vertical(bottom: Radius.circular(AuroraRadius.large)),
      ),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisAlignment: MainAxisAlignment.end, children: [
        Text(greeting, style: theme.textTheme.headlineSmall?.copyWith(color: theme.brightness == Brightness.light ? const Color(0xFF3D2A6B) : Colors.white70)),
        const SizedBox(height: 4),
        Text('Aurora AI', style: theme.textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.bold, color: theme.brightness == Brightness.light ? const Color(0xFF2A1050) : Colors.white)),
      ]),
    );
  }

  Widget _streamingBar(ThemeData theme, ChatProvider chat) => Container(
    width: double.infinity, padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
    color: theme.colorScheme.primaryContainer.withAlpha(60),
    child: Row(mainAxisAlignment: MainAxisAlignment.center, children: [
      SizedBox(width: 14, height: 14, child: CircularProgressIndicator(strokeWidth: 2, color: theme.colorScheme.primary)),
      const SizedBox(width: 10),
      Text('AI 回复中...', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.primary)),
      const Spacer(),
      TextButton.icon(onPressed: chat.cancelStream, icon: const Icon(Icons.stop, size: 16), label: const Text('停止'),
          style: TextButton.styleFrom(foregroundColor: theme.colorScheme.error, padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4), visualDensity: VisualDensity.compact)),
    ]),
  );

  Widget _modelBar(ThemeData theme) => AnimatedSize(
    duration: AppMotion.fast, curve: AppMotion.emphasizedDecelerate, alignment: Alignment.topCenter,
    child: GestureDetector(
      onTap: _showModelPicker,
      child: Container(width: double.infinity, padding: const EdgeInsets.fromLTRB(16, 50, 16, 8),
        child: Material(color: theme.colorScheme.surfaceContainerHighest, borderRadius: BorderRadius.circular(AuroraRadius.small),
          child: Padding(padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12), child: Row(children: [
            Icon(Icons.auto_awesome, size: 18, color: theme.colorScheme.primary), const SizedBox(width: 10),
            Expanded(child: Text(_currentModel, style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w500), overflow: TextOverflow.ellipsis)),
            const SizedBox(width: 4), Text('Fast · Vision', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
            const SizedBox(width: 8), Icon(Icons.expand_more, size: 18, color: theme.colorScheme.onSurfaceVariant),
          ])),
        ),
      ),
    ),
  );

  Widget _suggestedChips(ThemeData theme, bool show) => AnimatedSize(
    duration: AppMotion.fast, curve: AppMotion.emphasizedDecelerate, alignment: Alignment.bottomCenter,
    child: show ? SingleChildScrollView(scrollDirection: Axis.horizontal, padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(children: [
        _chip('写代码', Icons.code, () => _send('请帮我写一段代码来实现以下功能：')),
        const SizedBox(width: 8),
        _chip('翻译文本', Icons.translate, () => _send('请将以下文本翻译为英文：')),
        const SizedBox(width: 8),
        _chip('总结 PDF', Icons.description_outlined, () => _send('请总结以下内容的关键要点：')),
        const SizedBox(width: 8),
        _chip('生成报告', Icons.assessment_outlined, () => _send('请生成一份关于以下内容的报告：')),
        const SizedBox(width: 8),
        _chip('分析图片', Icons.image_outlined, () => _pickAndSendImage()),
      ]),
    ) : const SizedBox.shrink(),
  );

  Widget _chip(String label, IconData icon, VoidCallback onTap) => ActionChip(
    avatar: Icon(icon, size: 18), label: Text(label), onPressed: onTap,
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AuroraRadius.chip)),
    side: BorderSide.none, backgroundColor: Theme.of(context).colorScheme.surfaceContainerHighest,
    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
  );

  Widget _emptyState(ThemeData theme) => Center(
    child: Padding(padding: const EdgeInsets.all(32), child: Column(mainAxisSize: MainAxisSize.min, children: [
      Icon(Icons.auto_awesome, size: 56, color: theme.colorScheme.primary.withAlpha(80)),
      const SizedBox(height: 16),
      Text('开始对话', style: theme.textTheme.titleLarge),
      const SizedBox(height: 8),
      Text('选择建议或输入消息开始', style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
    ])),
  );

  Future<void> _pickAndSendImage() async {
    final img = await _picker.pickImage(source: ImageSource.gallery, maxWidth: 1024, imageQuality: 85);
    if (img != null) _send('请描述这张图片的内容。');
  }

  Future<void> _attachFile() async {
    showModalBottomSheet(context: context, builder: (ctx) => SafeArea(child: Column(mainAxisSize: MainAxisSize.min, children: [
      const Padding(padding: EdgeInsets.all(20), child: Text('添加附件', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w600))),
      ListTile(leading: const Icon(Icons.image_outlined), title: const Text('从相册选择图片'), onTap: () { Navigator.pop(ctx); _pickAndSendImage(); }),
      ListTile(leading: const Icon(Icons.camera_alt_outlined), title: const Text('拍照'), onTap: () { Navigator.pop(ctx); }),
      const SizedBox(height: 12),
    ])));
  }
}

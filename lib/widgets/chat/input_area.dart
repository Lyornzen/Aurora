import 'package:flutter/material.dart';
import 'package:speech_to_text/speech_to_text.dart' as stt;

class InputArea extends StatefulWidget {
  final Function(String) onSend;
  final VoidCallback? onAttach;
  final VoidCallback? onVoice;
  final bool isWebSearchEnabled;
  final bool isThinkingEnabled;
  final bool isVisionEnabled;
  final bool isFileAnalysisEnabled;
  final ValueChanged<bool>? onWebSearchToggle;
  final ValueChanged<bool>? onThinkingToggle;
  final ValueChanged<bool>? onVisionToggle;
  final ValueChanged<bool>? onFileAnalysisToggle;
  final bool autoFocus;

  const InputArea({
    super.key,
    required this.onSend,
    this.onAttach,
    this.onVoice,
    this.isWebSearchEnabled = false,
    this.isThinkingEnabled = false,
    this.isVisionEnabled = false,
    this.isFileAnalysisEnabled = false,
    this.onWebSearchToggle,
    this.onThinkingToggle,
    this.onVisionToggle,
    this.onFileAnalysisToggle,
    this.autoFocus = false,
  });

  void focusInput() {
    // Accessed via GlobalKey<InputAreaState>
  }

  @override
  State<InputArea> createState() => InputAreaState();
}

class InputAreaState extends State<InputArea> {
  final _controller = TextEditingController();
  final _focusNode = FocusNode();
  late stt.SpeechToText _speech;
  bool _isListening = false;
  bool _speechAvailable = false;

  @override
  void initState() {
    super.initState();
    _speech = stt.SpeechToText();
    _initSpeech();
    if (widget.autoFocus) {
      WidgetsBinding.instance.addPostFrameCallback((_) => _focusNode.requestFocus());
    }
  }

  void focusInput() {
    _focusNode.requestFocus();
  }

  void unfocus() {
    _focusNode.unfocus();
  }

  void _initSpeech() async {
    final available = await _speech.initialize(
      onStatus: (status) {
        if (status == 'done' || status == 'notListening') {
          if (mounted) setState(() => _isListening = false);
        }
      },
    );
    if (mounted) setState(() => _speechAvailable = available);
  }

  void _toggleListening() async {
    if (_isListening) {
      _speech.stop();
      setState(() => _isListening = false);
      return;
    }

    setState(() => _isListening = true);
    await _speech.listen(
      onResult: (result) {
        if (mounted) {
          setState(() {
            _controller.text = result.recognizedWords;
            _controller.selection = TextSelection.fromPosition(
              TextPosition(offset: _controller.text.length),
            );
          });
        }
      },
      localeId: 'zh_CN',
    );
  }

  @override
  void dispose() {
    _speech.stop();
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _send() {
    final text = _controller.text.trim();
    if (text.isEmpty) return;
    widget.onSend(text);
    _controller.clear();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        boxShadow: [
          BoxShadow(
            color: theme.shadowColor.withAlpha(20),
            blurRadius: 8,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(6, 8, 6, 6),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Quick action chips
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                child: Row(
                  children: [
                    _QuickChip(
                      icon: Icons.search,
                      label: '联网搜索',
                      selected: widget.isWebSearchEnabled,
                      onSelected: (v) => widget.onWebSearchToggle?.call(v),
                    ),
                    const SizedBox(width: 4),
                    _QuickChip(
                      icon: Icons.psychology_outlined,
                      label: '深度思考',
                      selected: widget.isThinkingEnabled,
                      onSelected: (v) => widget.onThinkingToggle?.call(v),
                    ),
                    const SizedBox(width: 4),
                    _QuickChip(
                      icon: Icons.image_outlined,
                      label: '图片识别',
                      selected: widget.isVisionEnabled,
                      onSelected: (v) => widget.onVisionToggle?.call(v),
                    ),
                    const SizedBox(width: 4),
                    _QuickChip(
                      icon: Icons.description_outlined,
                      label: '文件分析',
                      selected: widget.isFileAnalysisEnabled,
                      onSelected: (v) => widget.onFileAnalysisToggle?.call(v),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 8),

              // Input row
              Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  IconButton(
                    icon: const Icon(Icons.attach_file_outlined),
                    onPressed: widget.onAttach,
                    tooltip: '附件',
                  ),
                  Expanded(
                    child: ConstrainedBox(
                      constraints: BoxConstraints(
                        maxHeight: 160,
                        minHeight: 44,
                      ),
                      child: TextField(
                        controller: _controller,
                        focusNode: _focusNode,
                        maxLines: null,
                        minLines: 1,
                        textInputAction: TextInputAction.newline,
                        onSubmitted: (_) => _send(),
                        decoration: InputDecoration(
                          hintText: '输入消息...',
                          isDense: true,
                          filled: true,
                          fillColor: theme.colorScheme.surfaceContainerHighest,
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(22),
                            borderSide: BorderSide.none,
                          ),
                          contentPadding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 10,
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 4),
                  IconButton(
                    icon: Icon(_isListening
                        ? Icons.mic
                        : Icons.mic_outlined),
                    color: _isListening ? Colors.red : null,
                    onPressed: _speechAvailable ? _toggleListening : null,
                    tooltip: _speechAvailable
                        ? (_isListening ? '停止录音' : '语音输入')
                        : '语音输入不可用',
                  ),
                  const SizedBox(width: 2),
                  Material(
                    color: theme.colorScheme.primary,
                    borderRadius: BorderRadius.circular(20),
                    child: InkWell(
                      borderRadius: BorderRadius.circular(20),
                      onTap: _send,
                      child: Padding(
                        padding: const EdgeInsets.all(10),
                        child: Icon(
                          Icons.send_rounded,
                          color: theme.colorScheme.onPrimary,
                          size: 22,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _QuickChip extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool selected;
  final ValueChanged<bool>? onSelected;

  const _QuickChip({
    required this.icon,
    required this.label,
    required this.selected,
    this.onSelected,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return FilterChip.elevated(
      selected: selected,
      onSelected: onSelected,
      avatar: Icon(icon, size: 16),
      label: Text(label),
      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
      visualDensity: VisualDensity.compact,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
      ),
      selectedColor: theme.colorScheme.secondaryContainer,
    );
  }
}

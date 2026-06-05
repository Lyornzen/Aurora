import 'package:flutter/material.dart';
import 'package:speech_to_text/speech_to_text.dart' as stt;
import '../../theme/app_theme.dart';

class InputArea extends StatefulWidget {
  final Function(String) onSend;
  final VoidCallback? onAttach;
  final bool isThinkingEnabled;
  final String reasoningEffort;
  final ValueChanged<bool>? onThinkingToggle;
  final VoidCallback? onReasoningPick;

  const InputArea({super.key, required this.onSend, this.onAttach, this.isThinkingEnabled = false,
    this.reasoningEffort = 'high', this.onThinkingToggle, this.onReasoningPick});

  @override
  State<InputArea> createState() => InputAreaState();
}

class InputAreaState extends State<InputArea> {
  final _ctrl = TextEditingController();
  final _focus = FocusNode();
  late stt.SpeechToText _speech;
  bool _listening = false, _speechOK = false;

  @override
  void initState() {
    super.initState();
    _speech = stt.SpeechToText();
    _speech.initialize(onStatus: (s) { if (s == 'done' || s == 'notListening') { if (mounted) setState(() => _listening = false); } }).then((ok) { if (mounted) setState(() => _speechOK = ok); });
  }

  void _toggleMic() async {
    if (_listening) { _speech.stop(); setState(() => _listening = false); return; }
    setState(() => _listening = true);
    await _speech.listen(onResult: (r) { if (mounted) { setState(() { _ctrl.text = r.recognizedWords; _ctrl.selection = TextSelection.fromPosition(TextPosition(offset: _ctrl.text.length)); }); } }, localeId: 'zh_CN');
  }

  void _send() { final t = _ctrl.text.trim(); if (t.isNotEmpty) { widget.onSend(t); _ctrl.clear(); } }

  void focusInput() => _focus.requestFocus();
  void unfocus() => _focus.unfocus();

  @override
  void dispose() { _speech.stop(); _ctrl.dispose(); _focus.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      decoration: BoxDecoration(color: theme.colorScheme.surface, boxShadow: [BoxShadow(color: theme.shadowColor.withAlpha(15), blurRadius: 8, offset: const Offset(0, -2))]),
      child: SafeArea(child: Padding(padding: const EdgeInsets.fromLTRB(8, 8, 8, 8), child: Column(mainAxisSize: MainAxisSize.min, children: [
        // Input row
        Row(crossAxisAlignment: CrossAxisAlignment.center, children: [
          IconButton(icon: const Icon(Icons.attach_file, size: 22), onPressed: widget.onAttach, visualDensity: VisualDensity.compact),
          Expanded(child: TextField(controller: _ctrl, focusNode: _focus, maxLines: null, minLines: 1,
            textInputAction: TextInputAction.newline, onSubmitted: (_) => _send(),
            decoration: InputDecoration(hintText: '输入消息...', isDense: true, filled: true,
              fillColor: theme.colorScheme.surfaceContainerHighest,
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(AuroraRadius.input), borderSide: BorderSide.none),
              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10)))),
          const SizedBox(width: 4),
          IconButton(icon: Icon(_listening ? Icons.mic : Icons.mic_none, size: 22, color: _listening ? Colors.red : null), onPressed: _speechOK ? _toggleMic : null, visualDensity: VisualDensity.compact),
          IconButton(icon: const Icon(Icons.image, size: 22), onPressed: widget.onAttach, visualDensity: VisualDensity.compact),
          const SizedBox(width: 4),
          Material(color: theme.colorScheme.primary, shape: const CircleBorder(),
            child: InkWell(customBorder: const CircleBorder(), onTap: _send,
              child: Padding(padding: const EdgeInsets.all(10), child: Icon(Icons.send_rounded, color: theme.colorScheme.onPrimary, size: 20)))),
        ]),
      ]))),
    );
  }
}

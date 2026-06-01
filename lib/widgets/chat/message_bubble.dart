import 'package:flutter/material.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import 'package:markdown/markdown.dart' as md;
import '../../models/chat_message.dart';

class MessageBubble extends StatelessWidget {
  final ChatMessage message;
  final VoidCallback? onCopy;
  final VoidCallback? onRegenerate;
  final VoidCallback? onEdit;
  final VoidCallback? onFavorite;
  final VoidCallback? onShare;
  final VoidCallback? onSpeak;
  final VoidCallback? onToggleThinking;

  const MessageBubble({
    super.key,
    required this.message,
    this.onCopy,
    this.onRegenerate,
    this.onEdit,
    this.onFavorite,
    this.onShare,
    this.onSpeak,
    this.onToggleThinking,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isUser = message.role == MessageRole.user;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 3),
      child: Row(
        mainAxisAlignment:
            isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Flexible(
            child: ConstrainedBox(
              constraints: BoxConstraints(
                maxWidth: MediaQuery.of(context).size.width * 0.88,
              ),
              child: Column(
                crossAxisAlignment:
                    isUser ? CrossAxisAlignment.end : CrossAxisAlignment.start,
                children: [
                  // Thinking section
                  if (message.thinkingSteps != null &&
                      message.thinkingSteps!.isNotEmpty)
                    _ThinkingSection(
                      steps: message.thinkingSteps!,
                      isExpanded: message.isThinkingExpanded,
                      onToggle: onToggleThinking,
                    ),

                  // Message content card
                  Card(
                    elevation: 0,
                    color: isUser
                        ? theme.colorScheme.primaryContainer
                        : theme.colorScheme.surfaceContainerHighest,
                    margin: EdgeInsets.zero,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.only(
                        topLeft: const Radius.circular(14),
                        topRight: const Radius.circular(14),
                        bottomLeft: Radius.circular(isUser ? 14 : 4),
                        bottomRight: Radius.circular(isUser ? 4 : 14),
                      ),
                    ),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 12, vertical: 8),
                      child: isUser
                          ? Text(
                              message.content,
                              style: theme.textTheme.bodyMedium?.copyWith(
                                color: theme.colorScheme.onPrimaryContainer,
                                height: 1.4,
                              ),
                            )
                          : MarkdownBody(
                              data: message.content,
                              selectable: true,
                              inlineSyntaxes: [
                                _LatexInlineSyntax(),
                              ],
                              styleSheet: MarkdownStyleSheet(
                                p: theme.textTheme.bodyMedium!.copyWith(
                                  height: 1.4,
                                ),
                                h1: theme.textTheme.bodyMedium!.copyWith(
                                  fontWeight: FontWeight.bold,
                                  height: 1.3,
                                ),
                                h2: theme.textTheme.bodyMedium!.copyWith(
                                  fontWeight: FontWeight.bold,
                                  height: 1.3,
                                ),
                                code: theme.textTheme.bodySmall!.copyWith(
                                  fontFamily: 'monospace',
                                  fontSize: 12,
                                  backgroundColor:
                                      theme.colorScheme.surfaceContainerHighest,
                                ),
                                codeblockDecoration: BoxDecoration(
                                  color: theme.colorScheme.surfaceContainer,
                                  borderRadius: BorderRadius.circular(10),
                                ),
                                blockquoteDecoration: BoxDecoration(
                                  border: Border(
                                    left: BorderSide(
                                      color: theme.colorScheme.primary,
                                      width: 3,
                                    ),
                                  ),
                                ),
                              ),
                            ),
                    ),
                  ),

                  // Time & model
                  Padding(
                    padding: const EdgeInsets.only(top: 2, left: 8, right: 8),
                    child: Text(
                      _formatTime(message.createdAt) +
                          (message.modelName != null
                              ? ' · ${message.modelName}'
                              : ''),
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                        fontSize: 11,
                      ),
                    ),
                  ),

                  // Toolbar (AI messages only)
                  if (!isUser) _buildToolbar(context, theme),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildToolbar(BuildContext context, ThemeData theme) {
    return Padding(
      padding: const EdgeInsets.only(top: 4),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _ToolbarButton(
            icon: Icons.copy,
            tooltip: '复制',
            onTap: onCopy,
          ),
          _ToolbarButton(
            icon: Icons.refresh,
            tooltip: '重新生成',
            onTap: onRegenerate,
          ),
          _ToolbarButton(
            icon: Icons.edit_outlined,
            tooltip: '编辑问题',
            onTap: onEdit,
          ),
          _ToolbarButton(
            icon: message.isFavorited
                ? Icons.star
                : Icons.star_outline,
            tooltip: '收藏',
            onTap: onFavorite,
          ),
          _ToolbarButton(
            icon: Icons.share_outlined,
            tooltip: '分享',
            onTap: onShare,
          ),
          _ToolbarButton(
            icon: Icons.volume_up_outlined,
            tooltip: '朗读',
            onTap: onSpeak,
          ),
        ],
      ),
    );
  }

  String _formatTime(DateTime time) {
    final now = DateTime.now();
    if (time.day == now.day &&
        time.month == now.month &&
        time.year == now.year) {
      return '${time.hour.toString().padLeft(2, '0')}:${time.minute.toString().padLeft(2, '0')}';
    }
    return '${time.month}/${time.day} ${time.hour.toString().padLeft(2, '0')}:${time.minute.toString().padLeft(2, '0')}';
  }
}

class _ToolbarButton extends StatelessWidget {
  final IconData icon;
  final String tooltip;
  final VoidCallback? onTap;

  const _ToolbarButton({
    required this.icon,
    required this.tooltip,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 1),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          borderRadius: BorderRadius.circular(12),
          onTap: onTap,
          child: Tooltip(
            message: tooltip,
            child: Padding(
              padding: const EdgeInsets.all(4),
              child: Icon(
                icon,
                size: 14,
                color: Theme.of(context).colorScheme.onSurfaceVariant,
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/// Custom inline syntax for LaTeX math ($...$) in Markdown
class _LatexInlineSyntax extends md.InlineSyntax {
  _LatexInlineSyntax() : super(r'\$([^$]+)\$');

  @override
  bool onMatch(md.InlineParser parser, Match match) {
    final tex = match.group(1)!;
    final element = md.Element.text('el', ' $tex ');
    element.attributes['class'] = 'latex';
    parser.addNode(element);
    return true;
  }
}

class _ThinkingSection extends StatelessWidget {
  final List<ThinkingStep> steps;
  final bool isExpanded;
  final VoidCallback? onToggle;

  const _ThinkingSection({
    required this.steps,
    required this.isExpanded,
    this.onToggle,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final totalMs =
        steps.fold<int>(0, (sum, s) => sum + (s.duration?.inMilliseconds ?? 0));

    return Card(
      elevation: 0,
      color: theme.colorScheme.tertiaryContainer.withAlpha(80),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(10),
        onTap: onToggle,
        child: Padding(
          padding: const EdgeInsets.all(10),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    Icons.psychology_outlined,
                    size: 16,
                    color: theme.colorScheme.onTertiaryContainer,
                  ),
                  const SizedBox(width: 6),
                  Text(
                    'Thinking...',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onTertiaryContainer,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const Spacer(),
                  Text(
                    '${(totalMs / 1000).toStringAsFixed(1)}s',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onTertiaryContainer,
                    ),
                  ),
                  const SizedBox(width: 2),
                  Icon(
                    isExpanded ? Icons.expand_less : Icons.expand_more,
                    size: 16,
                    color: theme.colorScheme.onTertiaryContainer,
                  ),
                ],
              ),
              if (isExpanded) ...[
                const Divider(height: 12),
                ...steps.map((step) => Padding(
                      padding: const EdgeInsets.only(bottom: 6),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Icon(
                            Icons.check_circle_outline,
                            size: 12,
                            color: theme.colorScheme.onTertiaryContainer,
                          ),
                          const SizedBox(width: 6),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  step.description,
                                  style: theme.textTheme.bodySmall?.copyWith(
                                    color: theme.colorScheme.onTertiaryContainer,
                                  ),
                                ),
                                if (step.duration != null)
                                  Text(
                                    '${step.duration!.inMilliseconds}ms',
                                    style: theme.textTheme.bodySmall?.copyWith(
                                      color: theme.colorScheme
                                          .onTertiaryContainer
                                          .withAlpha(150),
                                    ),
                                  ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    )),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

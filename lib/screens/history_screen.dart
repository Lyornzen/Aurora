import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../providers/chat_provider.dart';
import '../models/chat_session.dart';
import '../widgets/common/empty_state.dart';

class HistoryScreen extends StatefulWidget {
  final VoidCallback? onSwitchToChat;

  const HistoryScreen({super.key, this.onSwitchToChat});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> {
  String _searchQuery = '';
  final _searchController = TextEditingController();
  String _filter = 'all'; // 'all', 'favorites', 'archived'

  List<ChatSession> _filterSessions(List<ChatSession> sessions) {
    var filtered = sessions;

    // Filter by type
    if (_filter == 'favorites') {
      filtered = filtered.where((s) => s.isPinned).toList();
    } else if (_filter == 'archived') {
      filtered = filtered.where((s) => s.isArchived).toList();
    }

    // Filter by search
    if (_searchQuery.isNotEmpty) {
      filtered = filtered
          .where((s) =>
              s.title.toLowerCase().contains(_searchQuery.toLowerCase()) ||
              (s.modelName?.toLowerCase().contains(_searchQuery.toLowerCase()) ??
                  false))
          .toList();
    }

    return filtered;
  }

  Map<String, List<ChatSession>> _groupByDate(List<ChatSession> sessions) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final yesterday = today.subtract(const Duration(days: 1));
    final lastWeek = today.subtract(const Duration(days: 7));
    final last30 = today.subtract(const Duration(days: 30));

    final Map<String, List<ChatSession>> groups = {
      'Today': [],
      'Yesterday': [],
      'Last 7 Days': [],
      'Last 30 Days': [],
      'Earlier': [],
    };

    for (final session in sessions) {
      final date = DateTime(
        session.updatedAt.year,
        session.updatedAt.month,
        session.updatedAt.day,
      );

      if (date == today) {
        groups['Today']!.add(session);
      } else if (date == yesterday) {
        groups['Yesterday']!.add(session);
      } else if (date.isAfter(lastWeek)) {
        groups['Last 7 Days']!.add(session);
      } else if (date.isAfter(last30)) {
        groups['Last 30 Days']!.add(session);
      } else {
        groups['Earlier']!.add(session);
      }
    }

    groups.removeWhere((_, v) => v.isEmpty);
    return groups;
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final chatProvider = context.watch<ChatProvider>();
    final theme = Theme.of(context);

    final sessions = chatProvider.sessions;
    if (sessions.isEmpty) {
      return const EmptyState(
        icon: Icons.history,
        title: '无聊天记录',
        description: '开始第一场对话吧',
        actionLabel: '开始对话',
      );
    }

    final filtered = _filterSessions(sessions);
    final grouped = _groupByDate(filtered);

    return Column(
      children: [
        // Search bar
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _searchController,
                  onChanged: (v) => setState(() => _searchQuery = v),
                  decoration: InputDecoration(
                    hintText: '搜索对话、模型...',
                    prefixIcon: const Icon(Icons.search),
                    suffixIcon: _searchQuery.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear),
                            onPressed: () {
                              _searchController.clear();
                              setState(() => _searchQuery = '');
                            },
                          )
                        : null,
                    filled: true,
                    fillColor: theme.colorScheme.surfaceContainerHighest,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(24),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(vertical: 10),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              PopupMenuButton<String>(
                icon: const Icon(Icons.filter_list),
                onSelected: (v) => setState(() => _filter = v),
                itemBuilder: (_) => [
                  const PopupMenuItem(
                    value: 'all',
                    child: Text('全部'),
                  ),
                  const PopupMenuItem(
                    value: 'favorites',
                    child: Text('已收藏'),
                  ),
                  const PopupMenuItem(
                    value: 'archived',
                    child: Text('已归档'),
                  ),
                ],
              ),
            ],
          ),
        ),

        // Session list
        Expanded(
          child: filtered.isEmpty
              ? EmptyState(
                  icon: Icons.search_off,
                  title: '未找到匹配结果',
                  description: '尝试其他关键词',
                )
              : ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  itemCount: grouped.entries.length,
                  itemBuilder: (context, groupIndex) {
                    final entry = grouped.entries.elementAt(groupIndex);
                    return Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Padding(
                          padding: const EdgeInsets.fromLTRB(12, 12, 12, 4),
                          child: Text(
                            entry.key,
                            style: theme.textTheme.titleSmall?.copyWith(
                              color: theme.colorScheme.primary,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                        ...entry.value.map((session) => _SessionTile(
                              session: session,
                              onTap: () {
                                chatProvider.selectSession(session.id);
                                widget.onSwitchToChat?.call();
                              },
                              onDelete: () =>
                                  chatProvider.deleteSession(session.id),
                              onArchive: () =>
                                  chatProvider.archiveSession(session.id),
                              onPin: () =>
                                  chatProvider.togglePinSession(session.id),
                            )),
                      ],
                    );
                  },
                ),
        ),
      ],
    );
  }
}

class _SessionTile extends StatelessWidget {
  final ChatSession session;
  final VoidCallback onTap;
  final VoidCallback onDelete;
  final VoidCallback onArchive;
  final VoidCallback onPin;

  const _SessionTile({
    required this.session,
    required this.onTap,
    required this.onDelete,
    required this.onArchive,
    required this.onPin,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final timeFormat = DateFormat('HH:mm');
    final dateFormat = DateFormat('MM/dd');

    final timeStr = session.updatedAt.day == DateTime.now().day
        ? timeFormat.format(session.updatedAt)
        : dateFormat.format(session.updatedAt);

    return Dismissible(
      key: Key('session_${session.id}'),
      direction: DismissDirection.horizontal,
      confirmDismiss: (direction) async {
        if (direction == DismissDirection.startToEnd) {
          final confirmed = await showDialog<bool>(
            context: context,
            builder: (ctx) => AlertDialog(
              title: const Text('置顶会话'),
              content: Text('确定要${session.isPinned ? '取消置顶' : '置顶'}"${session.title}"吗？'),
              actions: [
                TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('取消')),
                FilledButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('确定')),
              ],
            ),
          );
          if (confirmed == true) onPin();
          return false;
        } else {
          final confirmed = await showDialog<bool>(
            context: context,
            builder: (ctx) => AlertDialog(
              title: const Text('删除会话'),
              content: Text('确定要删除"${session.title}"吗？\n此操作不可撤销。'),
              actions: [
                TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('取消')),
                FilledButton(
                  onPressed: () => Navigator.pop(ctx, true),
                  style: FilledButton.styleFrom(backgroundColor: Theme.of(context).colorScheme.error),
                  child: const Text('删除'),
                ),
              ],
            ),
          );
          if (confirmed == true) onDelete();
          return false;
        }
      },
      background: Container(
        alignment: Alignment.centerLeft,
        padding: const EdgeInsets.only(left: 20),
        color: theme.colorScheme.tertiary,
        child: Icon(Icons.push_pin, color: theme.colorScheme.onTertiary),
      ),
      secondaryBackground: Container(
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 20),
        color: theme.colorScheme.error,
        child: Icon(Icons.delete, color: theme.colorScheme.onError),
      ),
      child: Card(
        elevation: 0,
        color: theme.colorScheme.surfaceContainerLow,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
        child: InkWell(
          borderRadius: BorderRadius.circular(12),
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          if (session.isPinned)
                            Padding(
                              padding: const EdgeInsets.only(right: 4),
                              child: Icon(
                                Icons.push_pin,
                                size: 14,
                                color: theme.colorScheme.primary,
                              ),
                            ),
                          Expanded(
                            child: Text(
                              session.title,
                              style: theme.textTheme.bodyLarge?.copyWith(
                                fontWeight: FontWeight.w500,
                              ),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 6),
                      Row(
                        children: [
                          if (session.modelName != null) ...[
                            Icon(
                              Icons.auto_awesome,
                              size: 14,
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                            const SizedBox(width: 4),
                            Text(
                              session.modelName!,
                              style: theme.textTheme.bodySmall?.copyWith(
                                color: theme.colorScheme.onSurfaceVariant,
                              ),
                            ),
                            const SizedBox(width: 12),
                          ],
                          Icon(
                            Icons.chat_bubble_outline,
                            size: 14,
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                          const SizedBox(width: 4),
                          Text(
                            '${session.messageCount}',
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 12),
                Text(
                  timeStr,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
                const SizedBox(width: 4),
                Icon(
                  Icons.chevron_right,
                  size: 20,
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

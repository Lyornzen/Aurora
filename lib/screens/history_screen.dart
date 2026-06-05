import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../providers/chat_provider.dart';
import '../models/chat_session.dart';
import '../theme/app_theme.dart';

class HistoryScreen extends StatefulWidget {
  final ValueChanged<int>? onSwitchTab;
  const HistoryScreen({super.key, this.onSwitchTab});
  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> {
  String _query = '';
  final _ctrl = TextEditingController();

  Map<String, List<ChatSession>> _group(List<ChatSession> sessions) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final yesterday = today.subtract(const Duration(days: 1));
    final week = today.subtract(const Duration(days: 7));
    final Map<String, List<ChatSession>> g = {'Today': [], 'Yesterday': [], 'This Week': [], 'Earlier': []};
    for (final s in sessions) {
      final d = DateTime(s.updatedAt.year, s.updatedAt.month, s.updatedAt.day);
      if (d == today) g['Today']!.add(s);
      else if (d == yesterday) g['Yesterday']!.add(s);
      else if (d.isAfter(week)) g['This Week']!.add(s);
      else g['Earlier']!.add(s);
    }
    g.removeWhere((_, v) => v.isEmpty);
    return g;
  }

  @override
  void dispose() { _ctrl.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    final chat = context.watch<ChatProvider>();
    final theme = Theme.of(context);
    final sessions = _query.isEmpty ? chat.sessions : chat.sessions.where((s) => s.title.toLowerCase().contains(_query.toLowerCase()) || (s.modelName?.toLowerCase().contains(_query.toLowerCase()) ?? false)).toList();
    final grouped = _group(sessions);

    return Scaffold(
      appBar: AppBar(title: const Text('History')),
      body: Column(children: [
        Padding(padding: const EdgeInsets.all(12), child: TextField(
          controller: _ctrl, onChanged: (v) => setState(() => _query = v),
          decoration: InputDecoration(hintText: 'Search conversations...', prefixIcon: const Icon(Icons.search),
            suffixIcon: _query.isNotEmpty ? IconButton(icon: const Icon(Icons.clear), onPressed: () { _ctrl.clear(); setState(() => _query = ''); }) : null),
        )),
        Expanded(child: sessions.isEmpty
            ? Center(child: Column(mainAxisSize: MainAxisSize.min, children: [Icon(Icons.history, size: 48, color: theme.colorScheme.onSurfaceVariant.withAlpha(80)), const SizedBox(height: 12), Text('No history', style: theme.textTheme.bodyLarge)]))
            : ListView.builder(padding: const EdgeInsets.symmetric(horizontal: 16), itemCount: grouped.entries.length,
                itemBuilder: (ctx, gi) {
                  final e = grouped.entries.elementAt(gi);
                  return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    Padding(padding: const EdgeInsets.fromLTRB(4, 16, 4, 8), child: Text(e.key, style: theme.textTheme.titleSmall?.copyWith(color: theme.colorScheme.primary))),
                    ...e.value.map((s) => _sessionTile(theme, s, chat)),
                  ]);
                })),
      ]),
    );
  }

  Widget _sessionTile(ThemeData theme, ChatSession s, ChatProvider chat) {
    final fmt = DateFormat('HH:mm');
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Dismissible(
        key: Key(s.id), direction: DismissDirection.horizontal,
        confirmDismiss: (dir) async {
          if (dir == DismissDirection.startToEnd) {
            final ok = await showDialog<bool>(context: context, builder: (c) => AlertDialog(
              title: const Text('Pin'), content: Text('${s.isPinned ? 'Unpin' : 'Pin'} "${s.title}"?'),
              actions: [TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('Cancel')), FilledButton(onPressed: () => Navigator.pop(c, true), child: const Text('OK'))],
            ));
            if (ok == true) chat.togglePinSession(s.id);
          } else {
            final ok = await showDialog<bool>(context: context, builder: (c) => AlertDialog(
              title: const Text('Delete'), content: Text('Delete "${s.title}"?'),
              actions: [TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('Cancel')), FilledButton(onPressed: () => Navigator.pop(c, true), style: FilledButton.styleFrom(backgroundColor: AuroraColors.error), child: const Text('Delete'))],
            ));
            if (ok == true) chat.deleteSession(s.id);
          }
          return false;
        },
        background: Container(alignment: Alignment.centerLeft, padding: const EdgeInsets.only(left: 20), color: theme.colorScheme.tertiary, child: const Icon(Icons.push_pin)),
        secondaryBackground: Container(alignment: Alignment.centerRight, padding: const EdgeInsets.only(right: 20), color: AuroraColors.error, child: const Icon(Icons.delete, color: Colors.white)),
        child: ListTile(
          contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
          title: Text(s.title, maxLines: 1, overflow: TextOverflow.ellipsis, style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w500)),
          subtitle: Text('${s.modelName ?? ""}  ${s.messageCount} msgs  ${fmt.format(s.updatedAt)}', style: theme.textTheme.bodySmall),
          trailing: Row(mainAxisSize: MainAxisSize.min, children: [
            if (s.isPinned) Icon(Icons.push_pin, size: 16, color: theme.colorScheme.primary),
            IconButton(icon: const Icon(Icons.chevron_right, size: 20), onPressed: () { chat.selectSession(s.id); widget.onSwitchTab?.call(0); }),
          ]),
          onTap: () { chat.selectSession(s.id); widget.onSwitchTab?.call(0); },
        ),
      ),
    );
  }
}

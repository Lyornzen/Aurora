import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

class TasksScreen extends StatefulWidget {
  const TasksScreen({super.key});
  @override
  State<TasksScreen> createState() => _TasksScreenState();
}

class _TasksScreenState extends State<TasksScreen> {
  final List<_Task> _tasks = [];

  Color _color(String s) => switch (s) {
    'Running' => AuroraColors.primary, 'Completed' => AuroraColors.success,
    'Paused' => AuroraColors.tertiary, _ => AuroraColors.error,
  };

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: const Text('Tasks'), actions: [
        IconButton(icon: const Icon(Icons.search), onPressed: () {}),
        IconButton(icon: const Icon(Icons.more_vert), onPressed: () {}),
      ]),
      body: ListView(padding: const EdgeInsets.all(16), children: [
        _newCard(theme),
        const SizedBox(height: 16),
        ..._tasks.map((t) => _taskCard(theme, t)),
      ]),
    );
  }

  Widget _newCard(ThemeData theme) => Material(
    color: theme.colorScheme.secondaryContainer ?? AuroraColors.secondary.withAlpha(40),
    borderRadius: BorderRadius.circular(20),
    child: InkWell(borderRadius: BorderRadius.circular(20), onTap: () {}, child: const Padding(
      padding: EdgeInsets.all(24),
      child: Row(children: [
        Icon(Icons.add_circle_outline, size: 28),
        SizedBox(width: 12),
        Text('+ New Task', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
      ]),
    )),
  );

  Widget _taskCard(ThemeData theme, _Task t) {
    final c = _color(t.status);
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Padding(padding: const EdgeInsets.all(16), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [
          Expanded(child: Text(t.name, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600))),
          Container(padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4), decoration: BoxDecoration(color: c.withAlpha(30), borderRadius: BorderRadius.circular(12)),
            child: Text(t.status, style: TextStyle(color: c, fontSize: 12, fontWeight: FontWeight.w500))),
        ]),
        const SizedBox(height: 12),
        ClipRRect(borderRadius: BorderRadius.circular(4), child: LinearProgressIndicator(value: t.progress, minHeight: 6, backgroundColor: c.withAlpha(30), valueColor: AlwaysStoppedAnimation(c))),
        const SizedBox(height: 8),
        Text('${(t.progress * 100).round()}%', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
        const SizedBox(height: 4),
        Text(t.desc, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
        const SizedBox(height: 12),
        Row(mainAxisAlignment: MainAxisAlignment.end, children: [
          TextButton.icon(onPressed: () {}, icon: const Icon(Icons.pause, size: 16), label: const Text('Pause')),
          const SizedBox(width: 8),
          TextButton.icon(onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => TaskDetailScreen(task: t))), icon: const Icon(Icons.visibility, size: 16), label: const Text('Details')),
        ]),
      ])),
    );
  }
}

class _Task {
  final String name, status, desc;
  final double progress;
  const _Task(this.name, this.status, this.progress, this.desc);
}

class TaskDetailScreen extends StatelessWidget {
  final _Task task;
  const TaskDetailScreen({super.key, required this.task});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(task.name)),
      body: ListView(padding: const EdgeInsets.all(16), children: [
        Card(shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)), child: Padding(padding: const EdgeInsets.all(16), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(task.name, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          Text(task.desc, style: theme.textTheme.bodyMedium),
          const SizedBox(height: 12),
          ClipRRect(borderRadius: BorderRadius.circular(4), child: LinearProgressIndicator(value: task.progress, minHeight: 8, backgroundColor: AuroraColors.primary.withAlpha(30), valueColor: const AlwaysStoppedAnimation(AuroraColors.primary))),
          const SizedBox(height: 8),
          Text('${(task.progress * 100).round()}%'),
        ]))),
        const SizedBox(height: 24),
        Text('Execution Timeline', style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
        const SizedBox(height: 12),
        ...[
          _Step(true, 'Start task'),
          _Step(true, 'Search data'),
          _Step(true, 'Analyze data'),
          _Step(false, 'Generate report'),
          _Step(false, 'Send email', pending: true),
        ].map((s) => Padding(padding: const EdgeInsets.only(bottom: 12), child: Row(children: [
          Container(width: 24, height: 24, decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: s.done ? AuroraColors.success : s.pending ? Colors.grey.shade300 : AuroraColors.primary),
            child: Center(child: Text(s.done ? '\u2713' : s.pending ? '\u25CB' : '\u25CF', style: const TextStyle(color: Colors.white, fontSize: 14))),
          ),
          const SizedBox(width: 12),
          Text(s.label, style: theme.textTheme.bodyMedium?.copyWith(fontWeight: s.pending ? FontWeight.normal : FontWeight.w600)),
        ]))),
        const SizedBox(height: 24),
        Row(children: [
          Expanded(child: OutlinedButton(onPressed: () {}, child: const Text('Pause'))),
          const SizedBox(width: 12),
          Expanded(child: FilledButton(onPressed: () {}, style: FilledButton.styleFrom(backgroundColor: AuroraColors.error), child: const Text('Cancel'))),
        ]),
      ]),
    );
  }
}

class _Step { final bool done; final String label; final bool pending; const _Step(this.done, this.label, {this.pending = false}); }

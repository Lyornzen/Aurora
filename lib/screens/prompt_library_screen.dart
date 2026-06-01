import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';
import '../services/database_service.dart';
import '../widgets/common/empty_state.dart';

const _uuid = Uuid();

class PromptLibraryScreen extends StatefulWidget {
  const PromptLibraryScreen({super.key});

  @override
  State<PromptLibraryScreen> createState() => _PromptLibraryScreenState();
}

class _PromptLibraryScreenState extends State<PromptLibraryScreen> {
  List<Map<String, dynamic>> _prompts = [];
  String _selectedCategory = '全部';
  bool _loaded = false;

  final List<String> _categories = ['全部', '语言', '写作', '编程', '学习', '创意', '自定义'];

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    _prompts = await DatabaseService.loadPromptTemplates();
    setState(() => _loaded = true);
  }

  List<Map<String, dynamic>> get _filtered =>
      _selectedCategory == '全部'
          ? _prompts
          : _prompts.where((p) => p['category'] == _selectedCategory).toList();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Prompt Library'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            tooltip: '新建 Prompt',
            onPressed: () => _showEditor(context),
          ),
        ],
      ),
      body: Column(
        children: [
          // Category filter chips
          SizedBox(
            height: 48,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
              itemCount: _categories.length,
              itemBuilder: (context, index) {
                final cat = _categories[index];
                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 4),
                  child: FilterChip(
                    label: Text(cat),
                    selected: _selectedCategory == cat,
                    onSelected: (_) =>
                        setState(() => _selectedCategory = cat),
                  ),
                );
              },
            ),
          ),
          const Divider(height: 1),

          // Prompt list
          Expanded(
            child: !_loaded
                ? const Center(child: CircularProgressIndicator())
                : _filtered.isEmpty
                    ? EmptyState(
                        icon: Icons.library_books_outlined,
                        title: _selectedCategory == '全部'
                            ? '暂无 Prompt'
                            : '该分类暂无 Prompt',
                        description: '点击右上角 + 创建自定义 Prompt',
                        actionLabel: '新建 Prompt',
                        onAction: () => _showEditor(context),
                      )
                    : ListView.builder(
                        padding: const EdgeInsets.all(12),
                        itemCount: _filtered.length,
                        itemBuilder: (context, index) {
                          final prompt = _filtered[index];
                          return _PromptCard(
                            prompt: prompt,
                            onTap: () => _usePrompt(context, prompt),
                            onEdit: () => _showEditor(context, existing: prompt),
                            onDelete: () => _deletePrompt(prompt),
                          );
                        },
                      ),
          ),
        ],
      ),
    );
  }

  void _usePrompt(BuildContext context, Map<String, dynamic> prompt) {
    Navigator.pop(context, prompt['content'] as String);
  }

  void _showEditor(BuildContext context,
      {Map<String, dynamic>? existing}) {
    final titleCtrl =
        TextEditingController(text: existing?['title'] as String? ?? '');
    final contentCtrl =
        TextEditingController(text: existing?['content'] as String? ?? '');
    final catCtrl =
        TextEditingController(text: existing?['category'] as String? ?? '自定义');
    final isBuiltin = (existing?['is_builtin'] as int?) == 1;

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(isBuiltin ? '查看 Prompt' : (existing != null ? '编辑 Prompt' : '新建 Prompt')),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: titleCtrl,
                decoration: const InputDecoration(
                  labelText: '标题',
                  hintText: '给这个 Prompt 起个名字',
                ),
                readOnly: isBuiltin,
              ),
              const SizedBox(height: 16),
              TextField(
                controller: catCtrl,
                decoration: const InputDecoration(
                  labelText: '分类',
                  hintText: '自定义',
                ),
                readOnly: isBuiltin,
              ),
              const SizedBox(height: 16),
              TextField(
                controller: contentCtrl,
                maxLines: 8,
                decoration: const InputDecoration(
                  labelText: 'Prompt 内容',
                  hintText:
                      '使用 {{变量名}} 作为占位符，例如：\n请将以下内容翻译为{{语言}}：{{内容}}',
                ),
                readOnly: isBuiltin,
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('取消'),
          ),
          if (!isBuiltin)
            FilledButton(
              onPressed: () async {
                if (titleCtrl.text.isEmpty) return;
                final prompt = {
                  'id': existing?['id'] ?? _uuid.v4(),
                  'title': titleCtrl.text,
                  'content': contentCtrl.text,
                  'category': catCtrl.text.isNotEmpty ? catCtrl.text : '自定义',
                  'created_at': DateTime.now().toIso8601String(),
                  'is_builtin': 0,
                };
                await DatabaseService.savePromptTemplate(prompt);
                Navigator.pop(ctx);
                _load();
              },
              child: const Text('保存'),
            ),
          if (isBuiltin)
            FilledButton(
              onPressed: () => Navigator.pop(ctx, contentCtrl.text),
              child: const Text('使用'),
            ),
        ],
      ),
    );
  }

  void _deletePrompt(Map<String, dynamic> prompt) async {
    final isBuiltin = (prompt['is_builtin'] as int?) == 1;
    if (isBuiltin) return; // Can't delete built-in

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('删除 Prompt'),
        content: Text('确定要删除 "${prompt['title']}" 吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () async {
              await DatabaseService.deletePromptTemplate(prompt['id']);
              Navigator.pop(ctx);
              _load();
            },
            style: FilledButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            child: const Text('删除'),
          ),
        ],
      ),
    );
  }
}

class _PromptCard extends StatelessWidget {
  final Map<String, dynamic> prompt;
  final VoidCallback onTap;
  final VoidCallback onEdit;
  final VoidCallback onDelete;

  const _PromptCard({
    required this.prompt,
    required this.onTap,
    required this.onEdit,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isBuiltin = (prompt['is_builtin'] as int?) == 1;
    final content = prompt['content'] as String? ?? '';

    return Card(
      elevation: 0,
      color: theme.colorScheme.surfaceContainerLow,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      margin: const EdgeInsets.only(bottom: 8),
      child: InkWell(
        borderRadius: BorderRadius.circular(14),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    isBuiltin
                        ? Icons.auto_awesome
                        : Icons.edit_note,
                    size: 20,
                    color: theme.colorScheme.primary,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      prompt['title'] as String? ?? '',
                      style: theme.textTheme.titleSmall,
                    ),
                  ),
                  if (isBuiltin)
                    Chip(
                      label: const Text('内置', style: TextStyle(fontSize: 11)),
                      visualDensity: VisualDensity.compact,
                      padding: EdgeInsets.zero,
                      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                      backgroundColor:
                          theme.colorScheme.tertiaryContainer.withAlpha(100),
                    ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                content.length > 100
                    ? '${content.substring(0, 100)}...'
                    : content,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                maxLines: 3,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 8),
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  if (!isBuiltin) ...[
                    _SmallIconButton(
                      icon: Icons.edit_outlined,
                      tooltip: '编辑',
                      onTap: onEdit,
                    ),
                    const SizedBox(width: 8),
                    _SmallIconButton(
                      icon: Icons.delete_outline,
                      tooltip: '删除',
                      onTap: onDelete,
                    ),
                  ],
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SmallIconButton extends StatelessWidget {
  final IconData icon;
  final String tooltip;
  final VoidCallback onTap;

  const _SmallIconButton({
    required this.icon,
    required this.tooltip,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(12),
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.all(6),
        child: Icon(icon, size: 18,
            color: Theme.of(context).colorScheme.onSurfaceVariant),
      ),
    );
  }
}

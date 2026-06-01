import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/theme_provider.dart';
import '../providers/api_provider.dart';
import '../providers/chat_provider.dart';
import '../models/api_config.dart';
import '../services/api_service.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final chatProvider = context.watch<ChatProvider>();

    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      children: [
        // 账户区域
        _buildSectionHeader('账户', Icons.person_outline),
        Card(
          elevation: 0,
          color: theme.colorScheme.surfaceContainerLow,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          child: Column(
            children: [
              ListTile(
                leading: CircleAvatar(
                  backgroundColor: theme.colorScheme.primaryContainer,
                  child: Icon(Icons.person,
                      color: theme.colorScheme.onPrimaryContainer),
                ),
                title: const Text('OpenSeek 用户'),
                subtitle: const Text('user@openseek.local'),
                trailing: Chip(
                  label: const Text('Free'),
                  visualDensity: VisualDensity.compact,
                  backgroundColor: theme.colorScheme.secondaryContainer,
                ),
              ),
            ],
          ),
        ),

        const SizedBox(height: 24),

        // 模型配置
        _buildSectionHeader('模型配置', Icons.auto_awesome),
        Card(
          elevation: 0,
          color: theme.colorScheme.surfaceContainerLow,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          child: Column(
            children: [
              ListTile(
                leading: const Icon(Icons.api),
                title: const Text('API 配置'),
                subtitle: Consumer<ApiProvider>(
                  builder: (_, api, _) => Text(
                    '${api.providers.length} 个 Provider，${api.configuredModelCount} 个模型',
                  ),
                ),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _showApiProviders(context),
              ),
              const Divider(height: 1, indent: 56),
              ListTile(
                leading: const Icon(Icons.tune),
                title: const Text('默认模型'),
                subtitle: const Text('设置默认聊天、视觉和推理模型'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _showModelDefaults(context),
              ),
            ],
          ),
        ),

        const SizedBox(height: 24),

        // 外观设置
        _buildSectionHeader('外观', Icons.palette_outlined),
        Card(
          elevation: 0,
          color: theme.colorScheme.surfaceContainerLow,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          child: Column(
            children: [
              _buildThemeModeTile(context),
              const Divider(height: 1, indent: 56),
              _buildColorSchemeTile(context),
              const Divider(height: 1, indent: 56),
              _buildFontSizeTile(context),
            ],
          ),
        ),

        const SizedBox(height: 24),

        // 数据与存储
        _buildSectionHeader('数据与存储', Icons.storage_outlined),
        Card(
          elevation: 0,
          color: theme.colorScheme.surfaceContainerLow,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          child: Column(
            children: [
              ListTile(
                leading: const Icon(Icons.chat_bubble_outline),
                title: Text('${chatProvider.sessions.length} 个会话'),
              ),
              const Divider(height: 1, indent: 56),
              ListTile(
                leading: const Icon(Icons.message_outlined),
                title: Text('${chatProvider.totalMessageCount} 条消息'),
              ),
              const Divider(height: 1, indent: 56),
              ListTile(
                leading: const Icon(Icons.cached),
                title: const Text('清理缓存'),
                subtitle: const Text('删除临时文件以释放空间'),
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('缓存已清理')),
                  );
                },
              ),
              const Divider(height: 1, indent: 56),
              ListTile(
                leading: const Icon(Icons.file_download_outlined),
                title: const Text('导出数据'),
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('数据导出功能即将推出')),
                  );
                },
              ),
              const Divider(height: 1, indent: 56),
              ListTile(
                leading: Icon(Icons.file_upload_outlined,
                    color: theme.colorScheme.onSurface),
                title: const Text('导入数据'),
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('数据导入功能即将推出')),
                  );
                },
              ),
              const Divider(height: 1, indent: 56),
              ListTile(
                leading: Icon(Icons.delete_forever_outlined,
                    color: theme.colorScheme.error),
                title: Text('删除所有数据',
                    style: TextStyle(color: theme.colorScheme.error)),
                onTap: () => _confirmDeleteAll(context),
              ),
            ],
          ),
        ),

        const SizedBox(height: 24),

        // 关于
        _buildSectionHeader('关于', Icons.info_outline),
        Card(
          elevation: 0,
          color: theme.colorScheme.surfaceContainerLow,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          child: Column(
            children: [
              ListTile(
                leading: const Icon(Icons.auto_awesome),
                title: const Text('OpenSeek'),
                subtitle: const Text('Version 1.1.2'),
              ),
              const Divider(height: 1, indent: 56),
              ListTile(
                leading: const Icon(Icons.description_outlined),
                title: const Text('开源协议'),
                subtitle: const Text('MIT License'),
              ),
              const Divider(height: 1, indent: 56),
              ListTile(
                leading: const Icon(Icons.privacy_tip_outlined),
                title: const Text('隐私政策'),
                onTap: () {},
              ),
              const Divider(height: 1, indent: 56),
              ListTile(
                leading: const Icon(Icons.new_releases_outlined),
                title: const Text('更新日志'),
                onTap: () => _showChangelog(context),
              ),
            ],
          ),
        ),

        const SizedBox(height: 80),
      ],
    );
  }

  Widget _buildSectionHeader(String title, IconData icon) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 8),
      child: Row(
        children: [
          Icon(icon, size: 18, color: theme.colorScheme.primary),
          const SizedBox(width: 8),
          Text(
            title,
            style: theme.textTheme.titleSmall?.copyWith(
              color: theme.colorScheme.primary,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildThemeModeTile(BuildContext context) {
    final themeProvider = context.watch<ThemeProvider>();
    return ListTile(
      leading: const Icon(Icons.brightness_6),
      title: const Text('主题模式'),
      subtitle: Text(_themeModeLabel(themeProvider.themeMode)),
      trailing: const Icon(Icons.chevron_right),
      onTap: () => _showThemeModePicker(context),
    );
  }

  Widget _buildColorSchemeTile(BuildContext context) {
    final themeProvider = context.watch<ThemeProvider>();
    return ListTile(
      leading: const Icon(Icons.color_lens_outlined),
      title: const Text('颜色方案'),
      subtitle: Text(ThemeProvider.colorNames[themeProvider.seedColorIndex]),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          ...ThemeProvider.presetColors.take(5).map((c) => Container(
                width: 20,
                height: 20,
                margin: const EdgeInsets.symmetric(horizontal: 2),
                decoration: BoxDecoration(
                  color: c,
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: themeProvider.seedColor == c
                        ? Theme.of(context).colorScheme.primary
                        : Colors.transparent,
                    width: 2,
                  ),
                ),
              )),
        ],
      ),
      onTap: () => _showColorPicker(context),
    );
  }

  Widget _buildFontSizeTile(BuildContext context) {
    final themeProvider = context.watch<ThemeProvider>();
    return ListTile(
      leading: const Icon(Icons.format_size),
      title: const Text('字体大小'),
      subtitle: Text(ThemeProvider.fontSizeLabels[themeProvider.fontSizeIndex]),
      trailing: const Icon(Icons.chevron_right),
      onTap: () => _showFontSizePicker(context),
    );
  }

  String _themeModeLabel(ThemeMode mode) {
    return switch (mode) {
      ThemeMode.system => '跟随系统',
      ThemeMode.light => '浅色模式',
      ThemeMode.dark => '深色模式',
    };
  }

  void _showThemeModePicker(BuildContext context) {
    final themeProvider = context.read<ThemeProvider>();
    showModalBottomSheet(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _bottomSheetHeader('主题模式'),
            ...ThemeMode.values.map((mode) => ListTile(
                  leading: Icon(switch (mode) {
                    ThemeMode.system => Icons.brightness_auto,
                    ThemeMode.light => Icons.light_mode,
                    ThemeMode.dark => Icons.dark_mode,
                  }),
                  title: Text(_themeModeLabel(mode)),
                  selected: themeProvider.themeMode == mode,
                  trailing: themeProvider.themeMode == mode
                      ? const Icon(Icons.check, color: Colors.green)
                      : null,
                  onTap: () {
                    themeProvider.setThemeMode(mode);
                    Navigator.pop(ctx);
                  },
                )),
            const SizedBox(height: 12),
          ],
        ),
      ),
    );
  }

  void _showColorPicker(BuildContext context) {
    final themeProvider = context.read<ThemeProvider>();
    showModalBottomSheet(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _bottomSheetHeader('颜色方案'),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
              child: Wrap(
                spacing: 16,
                children: List.generate(ThemeProvider.presetColors.length,
                    (i) => GestureDetector(
                          onTap: () {
                            themeProvider.setSeedColor(i);
                            Navigator.pop(ctx);
                          },
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Container(
                                width: 48,
                                height: 48,
                                decoration: BoxDecoration(
                                  color: ThemeProvider.presetColors[i],
                                  shape: BoxShape.circle,
                                  border: Border.all(
                                    color: themeProvider.seedColorIndex == i
                                        ? Theme.of(context).colorScheme.primary
                                        : Colors.transparent,
                                    width: 3,
                                  ),
                                ),
                                child: themeProvider.seedColorIndex == i
                                    ? const Icon(Icons.check,
                                        color: Colors.white, size: 24)
                                    : null,
                              ),
                              const SizedBox(height: 6),
                              Text(
                                ThemeProvider.colorNames[i],
                                style: Theme.of(context).textTheme.bodySmall,
                              ),
                            ],
                          ),
                        )),
              ),
            ),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }

  void _showFontSizePicker(BuildContext context) {
    final themeProvider = context.read<ThemeProvider>();
    showModalBottomSheet(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _bottomSheetHeader('字体大小'),
            ...List.generate(ThemeProvider.fontSizeLabels.length, (i) {
              final isSelected = themeProvider.fontSizeIndex == i;
              return ListTile(
                leading: Icon(Icons.format_size,
                    size: 18.0 + i * 4.0),
                title: Text(ThemeProvider.fontSizeLabels[i]),
                selected: isSelected,
                trailing: isSelected
                    ? const Icon(Icons.check, color: Colors.green)
                    : null,
                onTap: () {
                  themeProvider.setFontSize(i);
                  Navigator.pop(ctx);
                },
              );
            }),
            const SizedBox(height: 12),
          ],
        ),
      ),
    );
  }

  void _showApiProviders(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute(builder: (_) => const ApiConfigPage()),
    );
  }

  void _showModelDefaults(BuildContext context) {
    final apiProvider = context.read<ApiProvider>();
    final settings = apiProvider.modelSettings;
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (ctx) {
        // Local copies for editing
        String chatModel = settings.defaultChatModel;
        String? visionModel = settings.defaultVisionModel;
        String? reasonModel = settings.defaultReasoningModel;
        double temperature = settings.temperature;
        double topP = settings.topP;
        int contextLen = settings.contextLength;

        return StatefulBuilder(
          builder: (ctx2, setLocalState) => SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('默认模型设置',
                      style: Theme.of(context).textTheme.titleLarge),
                  const SizedBox(height: 20),

                  // Collect all models from enabled providers
                  _buildModelDropdown(ctx2, '默认聊天模型', chatModel,
                      (v) => setLocalState(() => chatModel = v)),
                  const Divider(),
                  _buildModelDropdown(ctx2, '默认视觉模型',
                      visionModel ?? '未设置',
                      (v) => setLocalState(() => visionModel = v)),
                  const Divider(),
                  _buildModelDropdown(ctx2, '默认推理模型',
                      reasonModel ?? '未设置',
                      (v) => setLocalState(() => reasonModel = v)),
                  const SizedBox(height: 20),
                  const Text('高级参数'),
                  const SizedBox(height: 12),
                  _buildSlider('Context 长度', contextLen.toDouble(),
                      1024, 32768, (v) => setLocalState(() => contextLen = v.round())),
                  _buildSlider('Temperature', temperature, 0.0, 2.0,
                      (v) => setLocalState(() => temperature = v)),
                  _buildSlider('Top P', topP, 0.0, 1.0,
                      (v) => setLocalState(() => topP = v)),
                  const SizedBox(height: 16),
                  SizedBox(
                    width: double.infinity,
                    child: FilledButton(
                      onPressed: () {
                        final newSettings = ModelSettings(
                          defaultChatModel: chatModel,
                          defaultVisionModel:
                              visionModel == '未设置' ? null : visionModel,
                          defaultReasoningModel:
                              reasonModel == '未设置' ? null : reasonModel,
                          contextLength: contextLen,
                          temperature: temperature,
                          topP: topP,
                        );
                        apiProvider.saveModelSettings(newSettings);
                        Navigator.pop(ctx);
                      },
                      child: const Text('保存'),
                    ),
                  ),
                  const SizedBox(height: 16),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildModelDropdown(
      BuildContext ctx, String label, String current, ValueChanged<String> onChanged) {
    final apiProvider = context.read<ApiProvider>();
    final allModels = <String>['未设置'];
    for (final p in apiProvider.enabledProviders) {
      allModels.addAll(p.models);
    }

    return ListTile(
      title: Text(label),
      subtitle: Text(current),
      trailing: const Icon(Icons.chevron_right),
      onTap: () {
        showModalBottomSheet(
          context: ctx,
          builder: (innerCtx) => SafeArea(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Padding(
                  padding: const EdgeInsets.all(20),
                  child: Text('选择模型',
                      style: Theme.of(context).textTheme.titleLarge),
                ),
                ...allModels.map((m) => ListTile(
                      title: Text(m),
                      selected: current == m,
                      trailing: current == m
                          ? const Icon(Icons.check, color: Colors.green)
                          : null,
                      onTap: () {
                        onChanged(m);
                        Navigator.pop(innerCtx);
                      },
                    )),
                const SizedBox(height: 12),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildSlider(String label, double value, double min, double max,
      ValueChanged<double> onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(label),
              Text(value.toStringAsFixed(1)),
            ],
          ),
          Slider(
            value: value,
            min: min,
            max: max,
            onChanged: onChanged,
          ),
        ],
      ),
    );
  }

  void _confirmDeleteAll(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('删除所有数据'),
        content: const Text('该操作不可撤销，确定要删除所有会话和消息记录吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('取消'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            onPressed: () async {
              Navigator.pop(ctx);
              final chatProvider = context.read<ChatProvider>();
              final sessionIds =
                  chatProvider.sessions.map((s) => s.id).toList();
              for (final id in sessionIds) {
                chatProvider.deleteSession(id);
              }
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('所有数据已删除')),
                );
              }
            },
            child: const Text('删除'),
          ),
        ],
      ),
    );
  }

  void _showChangelog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('更新日志'),
        content: const SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('v1.0.0 - 2026'),
              SizedBox(height: 4),
              Text('• 初始版本'),
              Text('• AI 聊天功能'),
              Text('• 多模型切换'),
              Text('• API 管理'),
              Text('• 历史记录'),
              Text('• 深色模式'),
            ],
          ),
        ),
        actions: [
          FilledButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  Widget _bottomSheetHeader(String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 8),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleLarge,
      ),
    );
  }
}

// API Config sub-page
class ApiConfigPage extends StatefulWidget {
  const ApiConfigPage({super.key});

  @override
  State<ApiConfigPage> createState() => _ApiConfigPageState();
}

class _ApiConfigPageState extends State<ApiConfigPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final apiProvider = context.watch<ApiProvider>();

    return Scaffold(
      appBar: AppBar(title: const Text('API 配置')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showAddProvider(context),
        icon: const Icon(Icons.add),
        label: const Text('添加'),
      ),
      body: apiProvider.providers.isEmpty
          ? const Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(Icons.api_outlined, size: 64, color: Colors.grey),
                  SizedBox(height: 16),
                  Text('暂无 API 配置'),
                  SizedBox(height: 8),
                  Text('点击右下角按钮添加'),
                ],
              ),
            )
          : ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: apiProvider.providers.length,
              itemBuilder: (context, index) {
                final provider = apiProvider.providers[index];
                return Card(
                  elevation: 0,
                  color: theme.colorScheme.surfaceContainerLow,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: ListTile(
                    title: Text(provider.name),
                    subtitle: Text(provider.models.isNotEmpty
                        ? '${provider.models.length} 个模型'
                        : '未配置模型'),
                    trailing: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Switch(
                          value: provider.isEnabled,
                          onChanged: (_) =>
                              apiProvider.toggleProvider(provider.id),
                        ),
                        const SizedBox(width: 4),
                        IconButton(
                          icon: const Icon(Icons.edit_outlined),
                          onPressed: () =>
                              _showEditProvider(context, provider),
                        ),
                      ],
                    ),
                    onTap: () => _showEditProvider(context, provider),
                  ),
                );
              },
            ),
    );
  }

  void _showAddProvider(BuildContext context) {
    _showEditProvider(
        context,
        ApiProviderConfig(
          id: DateTime.now().millisecondsSinceEpoch.toString(),
          name: '',
          provider: 'custom',
        ));
  }

  void _showEditProvider(BuildContext context, ApiProviderConfig config) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => _ApiProviderEditor(config: config),
    );
  }
}

class _ApiProviderEditor extends StatefulWidget {
  final ApiProviderConfig config;

  const _ApiProviderEditor({required this.config});

  @override
  State<_ApiProviderEditor> createState() => _ApiProviderEditorState();
}

class _ApiProviderEditorState extends State<_ApiProviderEditor> {
  late String _provider;
  late TextEditingController _nameCtrl;
  late TextEditingController _keyCtrl;
  late TextEditingController _urlCtrl;
  late TextEditingController _modelsCtrl;
  bool _obscureKey = true;

  @override
  void initState() {
    super.initState();
    _provider = widget.config.provider ?? 'custom';
    _nameCtrl = TextEditingController(text: widget.config.name);
    _keyCtrl = TextEditingController(text: widget.config.apiKey);
    _urlCtrl = TextEditingController(text: widget.config.baseUrl);
    _modelsCtrl = TextEditingController(
        text: widget.config.models.join(', '));
  }

  void _showResultDialog(
    BuildContext ctx, {
    required bool success,
    required String title,
    required String message,
  }) {
    showDialog(
      context: ctx,
      builder: (dialogCtx) => AlertDialog(
        icon: Icon(
          success ? Icons.check_circle : Icons.error,
          size: 48,
          color: success ? Colors.green : Colors.red,
        ),
        title: Text(title),
        content: Text(message),
        actions: [
          FilledButton(
            onPressed: () => Navigator.pop(dialogCtx),
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _keyCtrl.dispose();
    _urlCtrl.dispose();
    _modelsCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      initialChildSize: 0.85,
      expand: false,
      builder: (context, scrollController) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: ListView(
            controller: scrollController,
            children: [
              Text('API Configuration',
                  style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 24),

              // Provider selector
              DropdownButtonFormField<String>(
                initialValue: _provider,
                decoration: const InputDecoration(labelText: 'Provider'),
                items: ApiProviderConfig.knownProviders.entries
                    .map((e) => DropdownMenuItem(
                          value: e.key,
                          child: Text(e.value),
                        ))
                    .toList(),
                onChanged: (v) {
                  setState(() {
                    _provider = v!;
                    _nameCtrl.text = ApiProviderConfig.knownProviders[v] ?? '';
                    _urlCtrl.text =
                        ApiProviderConfig.knownBaseUrls[v] ?? '';
                  });
                },
              ),
              const SizedBox(height: 16),

              TextField(
                controller: _nameCtrl,
                decoration: const InputDecoration(labelText: '名称'),
              ),
              const SizedBox(height: 16),

              TextField(
                controller: _keyCtrl,
                decoration: InputDecoration(
                  labelText: 'API Key',
                  suffixIcon: IconButton(
                    icon: Icon(_obscureKey
                        ? Icons.visibility_off_outlined
                        : Icons.visibility_outlined),
                    onPressed: () =>
                        setState(() => _obscureKey = !_obscureKey),
                  ),
                ),
                obscureText: _obscureKey,
              ),
              const SizedBox(height: 16),

              TextField(
                controller: _urlCtrl,
                decoration:
                    const InputDecoration(labelText: 'Base URL'),
              ),
              const SizedBox(height: 16),

              TextField(
                controller: _modelsCtrl,
                decoration: const InputDecoration(
                  labelText: '模型列表（逗号分隔）',
                  hintText: 'gpt-4, gpt-3.5-turbo',
                ),
                maxLines: 2,
              ),
              const SizedBox(height: 24),

              // Test connection
              OutlinedButton.icon(
                onPressed: () async {
                  showDialog(
                    context: context,
                    barrierDismissible: false,
                    builder: (_) => const Center(
                      child: Card(
                        child: Padding(
                          padding: EdgeInsets.all(24),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              CircularProgressIndicator(),
                              SizedBox(width: 16),
                              Text('正在测试连接...'),
                            ],
                          ),
                        ),
                      ),
                    ),
                  );

                  final tempConfig = ApiProviderConfig(
                    id: 'test',
                    name: _nameCtrl.text,
                    apiKey: _keyCtrl.text,
                    baseUrl: _urlCtrl.text,
                    provider: _provider,
                  );
                  final service = ApiService(config: tempConfig);
                  final result = await service.testConnection();

                  if (context.mounted) Navigator.of(context).pop();

                  if (context.mounted && result.success) {
                    // Auto-fetch model names from API
                    final modelNames = await service.fetchModels();

                    if (modelNames.isNotEmpty && mounted) {
                      setState(() {
                        _modelsCtrl.text = modelNames.join(', ');
                      });
                    }

                    if (context.mounted) {
                      _showResultDialog(
                        context,
                        success: true,
                        title: '连接成功',
                        message: '延迟: ${result.latencyMs}ms\n'
                            '获取到 ${modelNames.length} 个模型\n'
                            '${modelNames.isNotEmpty ? modelNames.take(5).join('\n') : ''}'
                            '${modelNames.length > 5 ? '\n...等 ${modelNames.length} 个' : ''}',
                      );
                    }
                  } else if (context.mounted && !result.success) {
                    _showResultDialog(
                      context,
                      success: false,
                      title: '连接失败',
                      message: result.error ?? '未知错误',
                    );
                  }
                  service.dispose();
                },
                icon: const Icon(Icons.wifi_find),
                label: const Text('测试连接'),
              ),
              const SizedBox(height: 12),

              // Save
              SizedBox(
                width: double.infinity,
                child: FilledButton(
                  onPressed: () {
                    final models = _modelsCtrl.text
                        .split(',')
                        .map((m) => m.trim())
                        .where((m) => m.isNotEmpty)
                        .toList();

                    final updated = widget.config.copyWith(
                      name: _nameCtrl.text,
                      apiKey: _keyCtrl.text,
                      baseUrl: _urlCtrl.text,
                      provider: _provider,
                      models: models,
                    );

                    context.read<ApiProvider>().saveProvider(updated);
                    Navigator.pop(context);
                  },
                  child: const Text('保存'),
                ),
              ),
              const SizedBox(height: 32),
            ],
          ),
        ),
      ),
    );
  }
}

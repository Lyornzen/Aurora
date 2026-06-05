import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/theme_provider.dart';
import '../theme/app_theme.dart';
import 'settings_screen.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final tp = context.watch<ThemeProvider>();

    return Scaffold(
      appBar: AppBar(title: const Text('Profile')),
      body: ListView(padding: const EdgeInsets.all(16), children: [
        const SizedBox(height: 8),
        Center(child: Column(children: [
          CircleAvatar(radius: 40, backgroundColor: theme.colorScheme.primaryContainer, child: Icon(Icons.person, size: 40, color: theme.colorScheme.onPrimaryContainer)),
          const SizedBox(height: 12),
          Text('Aurora User', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600)),
          Text('user@aurora.ai', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
        ])),
        const SizedBox(height: 24),
        _section(theme, 'Appearance', Icons.palette_outlined, [
          _modeTile(context, tp),
          _colorTile(context, tp),
          _fontSizeTile(context, tp),
        ]),
        _section(theme, 'Settings', Icons.settings_outlined, [
          _linkTile(context, 'API Settings', Icons.api, const ApiConfigPage()),
          _linkTile(context, 'Model Settings', Icons.tune, _buildModelPage(context)),
          _langTile(context),
        ]),
        _section(theme, 'About', Icons.info_outlined, [
          ListTile(leading: const Icon(Icons.auto_awesome), title: const Text('Aurora AI'), subtitle: const Text('Version 1.1.2')),
          ListTile(leading: const Icon(Icons.description_outlined), title: const Text('License'), subtitle: const Text('MIT')),
        ]),
        const SizedBox(height: 80),
      ]),
    );
  }

  Widget _buildModelPage(BuildContext context) {
    return const ApiConfigPage(); // reuse as model defaults page
  }

  Widget _section(ThemeData theme, String title, IconData icon, List<Widget> children) => Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Padding(padding: const EdgeInsets.only(left: 4, bottom: 8, top: 16), child: Row(children: [
        Icon(icon, size: 18, color: theme.colorScheme.primary), const SizedBox(width: 8),
        Text(title, style: theme.textTheme.titleSmall?.copyWith(color: theme.colorScheme.primary, fontWeight: FontWeight.w600)),
      ])),
      Card(shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)), child: Column(children: _div(children))),
    ],
  );

  List<Widget> _div(List<Widget> items) {
    final r = <Widget>[];
    for (var i = 0; i < items.length; i++) { r.add(items[i]); if (i < items.length - 1) r.add(const Divider(indent: 56)); }
    return r;
  }

  Widget _modeTile(BuildContext c, ThemeProvider tp) => ListTile(
    leading: const Icon(Icons.brightness_6), title: const Text('Theme Mode'),
    subtitle: Text(tp.themeMode == ThemeMode.system ? 'System' : tp.themeMode == ThemeMode.light ? 'Light' : 'Dark'),
    trailing: const Icon(Icons.chevron_right),
    onTap: () => _picker(c, 'Theme Mode', ThemeMode.values.map((m) => (
      m == ThemeMode.system ? Icons.brightness_auto : m == ThemeMode.light ? Icons.light_mode : Icons.dark_mode,
      m == ThemeMode.system ? 'System' : m == ThemeMode.light ? 'Light' : 'Dark',
      m, tp.themeMode == m
    )).toList(), (v) => tp.setThemeMode(v as ThemeMode)),
  );

  Widget _colorTile(BuildContext c, ThemeProvider tp) => ListTile(
    leading: const Icon(Icons.color_lens_outlined), title: const Text('Theme Color'),
    subtitle: Text(ThemeProvider.colorNames[tp.seedColorIndex]),
    trailing: Row(mainAxisSize: MainAxisSize.min, children: List.generate(ThemeProvider.presetColors.length, (i) => Container(
      width: 20, height: 20, margin: const EdgeInsets.symmetric(horizontal: 2),
      decoration: BoxDecoration(color: ThemeProvider.presetColors[i], shape: BoxShape.circle,
        border: Border.all(color: tp.seedColorIndex == i ? Theme.of(c).colorScheme.primary : Colors.transparent, width: 2)),
    ))),
    onTap: () => _picker(c, 'Theme Color', List.generate(ThemeProvider.presetColors.length, (i) => (
      Icons.circle, ThemeProvider.colorNames[i], i, tp.seedColorIndex == i
    )), (v) => tp.setSeedColor(v as int)),
  );

  Widget _fontSizeTile(BuildContext c, ThemeProvider tp) => ListTile(
    leading: const Icon(Icons.format_size), title: const Text('Font Size'),
    subtitle: Text(ThemeProvider.fontSizeLabels[tp.fontSizeIndex]),
    trailing: const Icon(Icons.chevron_right),
    onTap: () => _picker(c, 'Font Size', List.generate(ThemeProvider.fontSizeLabels.length, (i) => (
      Icons.format_size, ThemeProvider.fontSizeLabels[i], i, tp.fontSizeIndex == i
    )), (v) => tp.setFontSize(v as int)),
  );

  Widget _langTile(BuildContext c) => ListTile(
    leading: const Icon(Icons.language), title: const Text('Language'),
    subtitle: const Text('English'),
    trailing: const Icon(Icons.chevron_right),
    onTap: () => _picker(c, 'Language', [
      (Icons.language, 'English', 'en', false),
      (Icons.language, 'Chinese', 'zh', false),
    ], (_) {}),
  );

  Widget _linkTile(BuildContext c, String title, IconData icon, Widget? page) => ListTile(
    leading: Icon(icon), title: Text(title), trailing: const Icon(Icons.chevron_right),
    onTap: page != null ? () => Navigator.of(c).push(MaterialPageRoute(builder: (_) => page)) : null,
  );

  void _picker(BuildContext c, String title, List<(IconData, String, Object, bool)> items, ValueChanged<Object> onPick) {
    showModalBottomSheet(context: c, builder: (ctx) => SafeArea(child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.start, children: [
      Padding(padding: const EdgeInsets.all(20), child: Text(title, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w600))),
      for (final item in items) ListTile(
        leading: Icon(item.$1, size: 24), title: Text(item.$2),
        selected: item.$4, trailing: item.$4 ? const Icon(Icons.check, color: Colors.green) : null,
        onTap: () { onPick(item.$3); Navigator.pop(ctx); },
      ),
      const SizedBox(height: 12),
    ])));
  }
}

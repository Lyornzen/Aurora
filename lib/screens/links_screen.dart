import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

class LinksScreen extends StatelessWidget {
  const LinksScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final devices = <Map<String, dynamic>>[]; // Real devices will be populated later

    return Scaffold(
      appBar: AppBar(title: const Text('Links'), actions: [
        IconButton(icon: const Icon(Icons.refresh), onPressed: () {}),
      ]),
      body: ListView(padding: const EdgeInsets.all(16), children: [
        ...devices.map((d) => _deviceCard(theme, d)),
        const SizedBox(height: 24),
        if (devices.any((d) => d['online'] == true)) ...[
          Text('Remote Actions', style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
          const SizedBox(height: 12),
          _actionsGrid(theme),
        ],
      ]),
    );
  }

  Widget _deviceCard(ThemeData theme, Map<String, dynamic> d) {
    final online = d['online'] == true;
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Padding(padding: const EdgeInsets.all(16), child: Column(children: [
        Row(children: [
          Container(width: 48, height: 48, decoration: BoxDecoration(color: online ? AuroraColors.success.withAlpha(30) : Colors.grey.shade200, borderRadius: BorderRadius.circular(12)),
            child: Icon(online ? Icons.desktop_windows : Icons.desktop_windows_outlined, color: online ? AuroraColors.success : Colors.grey)),
          const SizedBox(width: 12),
          Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(d['name'] as String, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
            const SizedBox(height: 2),
            Text(d['ip'] as String, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
          ])),
          Container(padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4), decoration: BoxDecoration(color: online ? AuroraColors.success.withAlpha(30) : Colors.grey.shade100, borderRadius: BorderRadius.circular(12)),
            child: Text(online ? 'Online' : 'Offline', style: TextStyle(color: online ? AuroraColors.success : Colors.grey, fontSize: 12, fontWeight: FontWeight.w500))),
        ]),
        if (online) ...[
          const SizedBox(height: 12),
          Row(children: [
            _usageBar('CPU', (d['cpu'] as int).toDouble(), AuroraColors.primary, theme),
            const SizedBox(width: 8),
            _usageBar('RAM', (d['ram'] as int).toDouble(), AuroraColors.success, theme),
            const SizedBox(width: 8),
            _usageBar('GPU', (d['gpu'] as int).toDouble(), AuroraColors.tertiary, theme),
          ]),
        ],
      ])),
    );
  }

  Widget _usageBar(String label, double value, Color color, ThemeData theme) => Expanded(
    child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Text(label, style: theme.textTheme.bodySmall),
      const SizedBox(height: 4),
      ClipRRect(borderRadius: BorderRadius.circular(2), child: LinearProgressIndicator(value: value / 100, minHeight: 4, backgroundColor: color.withAlpha(30), valueColor: AlwaysStoppedAnimation(color))),
      Text('${value.round()}%', style: theme.textTheme.bodySmall?.copyWith(fontSize: 11)),
    ]),
  );

  Widget _actionsGrid(ThemeData theme) => GridView.count(
    shrinkWrap: true, physics: const NeverScrollableScrollPhysics(), crossAxisCount: 2, mainAxisSpacing: 12, crossAxisSpacing: 12, childAspectRatio: 1.6,
    children: [
      _actionTile(theme, 'Run Script', Icons.terminal),
      _actionTile(theme, 'Open Browser', Icons.language),
      _actionTile(theme, 'Run Agent', Icons.smart_toy),
      _actionTile(theme, 'File Transfer', Icons.file_copy),
    ],
  );

  Widget _actionTile(ThemeData theme, String label, IconData icon) => Material(
    color: theme.colorScheme.surfaceContainerHighest,
    borderRadius: BorderRadius.circular(20),
    child: InkWell(borderRadius: BorderRadius.circular(20), onTap: () {},
      child: Padding(padding: const EdgeInsets.all(16), child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
        Icon(icon, size: 28, color: theme.colorScheme.primary), const SizedBox(height: 8), Text(label, style: theme.textTheme.bodyMedium),
      ])),
    ),
  );
}

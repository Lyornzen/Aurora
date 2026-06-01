import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/theme_provider.dart';
import 'providers/chat_provider.dart';
import 'screens/chat_screen.dart';
import 'screens/link_screen.dart';
import 'screens/history_screen.dart';
import 'screens/settings_screen.dart';
import 'widgets/app_drawer.dart';

class OpenSeekApp extends StatelessWidget {
  const OpenSeekApp({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<ThemeProvider>(
      builder: (context, themeProvider, _) {
        return MediaQuery(
          data: MediaQuery.of(context).copyWith(
            textScaler: TextScaler.linear(themeProvider.textScaleFactor),
          ),
          child: MaterialApp(
            title: 'OpenSeek',
            debugShowCheckedModeBanner: false,
            theme: themeProvider.lightTheme,
            darkTheme: themeProvider.darkTheme,
            themeMode: themeProvider.themeMode,
            home: const MainShell(),
          ),
        );
      },
    );
  }
}

class MainShell extends StatefulWidget {
  const MainShell({super.key});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int _currentIndex = 0;

  static const _titles = ['OpenSeek', 'Link', 'History', 'Settings'];

  void _switchToTab(int index) {
    if (index >= 0 && index < 4) {
      setState(() => _currentIndex = index);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('OpenSeek'),
        actions: _currentIndex == 0
            ? [
                IconButton(
                  icon: const Icon(Icons.add),
                  tooltip: '新建会话',
                  onPressed: () {
                    context.read<ChatProvider>().createNewSession();
                  },
                ),
                IconButton(
                  icon: const Icon(Icons.more_vert),
                  onPressed: () => _showMoreMenu(context),
                ),
              ]
            : null,
      ),
      drawer: AppDrawer(
        currentIndex: _currentIndex,
        onSwitchTab: _switchToTab,
      ),
      body: IndexedStack(
        index: _currentIndex,
        children: [
          ChatScreen(onSwitchToHistory: () => _switchToTab(2)),
          const LinkScreen(),
          HistoryScreen(onSwitchToChat: () => _switchToTab(0)),
          const SettingsScreen(),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: _switchToTab,
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.chat_bubble_outline),
            selectedIcon: Icon(Icons.chat_bubble),
            label: 'Chat',
          ),
          NavigationDestination(
            icon: Icon(Icons.link_outlined),
            selectedIcon: Icon(Icons.link),
            label: 'Link',
          ),
          NavigationDestination(
            icon: Icon(Icons.history_outlined),
            selectedIcon: Icon(Icons.history),
            label: 'History',
          ),
          NavigationDestination(
            icon: Icon(Icons.settings_outlined),
            selectedIcon: Icon(Icons.settings),
            label: 'Settings',
          ),
        ],
      ),
    );
  }

  void _showMoreMenu(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: const Icon(Icons.edit),
              title: const Text('重命名会话'),
              onTap: () {
                Navigator.pop(ctx);
                _showRenameDialog(context);
              },
            ),
            ListTile(
              leading: const Icon(Icons.file_download_outlined),
              title: const Text('导出会话'),
              onTap: () {
                Navigator.pop(ctx);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('导出功能即将推出')),
                );
              },
            ),
            ListTile(
              leading: const Icon(Icons.delete_sweep_outlined),
              title: const Text('清空会话'),
              onTap: () {
                Navigator.pop(ctx);
                context.read<ChatProvider>().clearCurrentSession();
              },
            ),
            ListTile(
              leading: const Icon(Icons.token_outlined),
              title: const Text('查看 Token'),
              onTap: () {
                Navigator.pop(ctx);
                _showTokenDialog(context);
              },
            ),
          ],
        ),
      ),
    );
  }

  void _showRenameDialog(BuildContext context) {
    final controller = TextEditingController();
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('重命名会话'),
        content: TextField(
          controller: controller,
          decoration: const InputDecoration(hintText: '输入新名称'),
          autofocus: true,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () {
              final sessionId = context.read<ChatProvider>().currentSessionId;
              if (sessionId != null && controller.text.isNotEmpty) {
                context.read<ChatProvider>().renameSession(
                      sessionId,
                      controller.text,
                    );
              }
              Navigator.pop(ctx);
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  void _showTokenDialog(BuildContext context) {
    final chat = context.read<ChatProvider>();
    final usage = chat.tokenUsageByModel;
    final total = chat.totalTokens;

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Token 用量统计'),
        content: SizedBox(
          width: double.maxFinite,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (usage.isNotEmpty)
                SizedBox(
                  height: 160,
                  child: CustomPaint(
                    painter: _PieChartPainter(
                      data: usage.entries
                          .map((e) => _ChartEntry(e.key, e.value))
                          .toList(),
                    ),
                    child: Center(
                      child: Text('${(total / 1000).toStringAsFixed(1)}k',
                          style: Theme.of(context)
                              .textTheme
                              .titleLarge
                              ?.copyWith(fontWeight: FontWeight.bold)),
                    ),
                  ),
                ),
              const SizedBox(height: 12),
              if (usage.isNotEmpty)
                const Text('模型占比',
                    style: TextStyle(fontWeight: FontWeight.w600))
              else
                const Text('暂无数据'),
              const SizedBox(height: 8),
              ...usage.entries.map((e) => Padding(
                    padding: const EdgeInsets.symmetric(vertical: 3),
                    child: Row(
                      children: [
                        Container(
                          width: 12,
                          height: 12,
                          decoration: BoxDecoration(
                            color: _pieColor(
                                usage.keys.toList().indexOf(e.key)),
                            shape: BoxShape.circle,
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                            child: Text(e.key,
                                style: const TextStyle(fontSize: 13))),
                        Text('${(e.value / 1000).toStringAsFixed(1)}k',
                            style: TextStyle(
                                fontSize: 13,
                                color: Theme.of(context)
                                    .colorScheme
                                    .onSurfaceVariant)),
                      ],
                    ),
                  )),
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
}

Color _pieColor(int index) {
  const colors = [
    Color(0xFF6750A4),
    Color(0xFF1565C0),
    Color(0xFF006D3B),
    Color(0xFFFF6D00),
    Color(0xFFE91E63),
    Color(0xFF009688),
    Color(0xFF795548),
  ];
  return colors[index % colors.length];
}

class _ChartEntry {
  final String label;
  final int value;
  const _ChartEntry(this.label, this.value);
}

class _PieChartPainter extends CustomPainter {
  final List<_ChartEntry> data;
  _PieChartPainter({required this.data});

  @override
  void paint(Canvas canvas, Size size) {
    final total = data.fold<int>(0, (s, e) => s + e.value);
    if (total == 0) return;
    final center = Offset(size.width / 2, size.height / 2);
    final radius = min(size.width, size.height) / 2 - 8;
    double startAngle = -pi / 2;

    for (var i = 0; i < data.length; i++) {
      final sweep = (data[i].value / total) * 2 * pi;
      final paint = Paint()
        ..color = _pieColor(i)
        ..style = PaintingStyle.fill;
      canvas.drawArc(Rect.fromCircle(center: center, radius: radius),
          startAngle, sweep, true, paint);
      startAngle += sweep;
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/theme_provider.dart';
import 'providers/chat_provider.dart';
import 'screens/chat_screen.dart';
import 'screens/tasks_screen.dart';
import 'screens/links_screen.dart';
import 'screens/history_screen.dart';
import 'screens/profile_screen.dart';

class AuroraApp extends StatelessWidget {
  const AuroraApp({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<ThemeProvider>(
      builder: (context, tp, _) {
        return MaterialApp(
          title: 'Aurora AI',
          debugShowCheckedModeBanner: false,
          theme: tp.lightTheme,
          darkTheme: tp.darkTheme,
          themeMode: tp.themeMode,
          builder: (ctx, child) => MediaQuery(
            data: MediaQuery.of(ctx).copyWith(textScaler: TextScaler.linear(tp.textScaleFactor)),
            child: child!,
          ),
          home: const MainShell(),
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
  int _index = 0;

  static const _titles = ['Chat', 'Tasks', 'Links', 'History', 'Profile'];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _index,
        children: [
          ChatScreen(onSwitchTab: (i) => setState(() => _index = i)),
          const TasksScreen(),
          const LinksScreen(),
          HistoryScreen(onSwitchTab: (i) => setState(() => _index = i)),
          const ProfileScreen(),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.chat_bubble_outlined),
            selectedIcon: Icon(Icons.chat_bubble),
            label: 'Chat',
          ),
          NavigationDestination(
            icon: Icon(Icons.task_alt_outlined),
            selectedIcon: Icon(Icons.task_alt),
            label: 'Tasks',
          ),
          NavigationDestination(
            icon: Icon(Icons.link_outlined),
            selectedIcon: Icon(Icons.link),
            label: 'Links',
          ),
          NavigationDestination(
            icon: Icon(Icons.history_outlined),
            selectedIcon: Icon(Icons.history),
            label: 'History',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outlined),
            selectedIcon: Icon(Icons.person),
            label: 'Profile',
          ),
        ],
      ),
    );
  }
}

// Shared helpers used across screens
void _showTokenDialog(BuildContext context) {
  final chat = context.read<ChatProvider>();
  final usage = chat.tokenUsageByModel;
  final total = chat.totalTokens;

  showDialog(
    context: context,
    builder: (ctx) => AlertDialog(
      title: const Text('Token 用量'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (usage.isNotEmpty)
            SizedBox(
              height: 140,
              child: CustomPaint(
                painter: _PieChartPainter(
                  data: usage.entries.map((e) => _ChartEntry(e.key, e.value)).toList(),
                ),
                child: Center(
                  child: Text('${(total / 1000).toStringAsFixed(1)}k',
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold)),
                ),
              ),
            ),
          const SizedBox(height: 8),
          ...usage.entries.map((e) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 3),
                child: Row(children: [
                  Container(width: 10, height: 10, decoration: BoxDecoration(color: _pieColor(usage.keys.toList().indexOf(e.key)), shape: BoxShape.circle)),
                  const SizedBox(width: 8),
                  Expanded(child: Text(e.key, style: const TextStyle(fontSize: 13))),
                  Text('${(e.value / 1000).toStringAsFixed(1)}k', style: TextStyle(fontSize: 13, color: Theme.of(context).colorScheme.onSurfaceVariant)),
                ]),
              )),
        ],
      ),
      actions: [FilledButton(onPressed: () => Navigator.pop(ctx), child: const Text('确定'))],
    ),
  );
}

Color _pieColor(int i) {
  const c = [Color(0xFF7C4DFF), Color(0xFFB39DDB), Color(0xFF4CAF50), Color(0xFFF8BBD0), Color(0xFFEF5350)];
  return c[i % c.length];
}

class _ChartEntry { final String label; final int value; const _ChartEntry(this.label, this.value); }

class _PieChartPainter extends CustomPainter {
  final List<_ChartEntry> data;
  _PieChartPainter({required this.data});
  @override
  void paint(Canvas canvas, Size size) {
    final total = data.fold<int>(0, (s, e) => s + e.value);
    if (total == 0) return;
    final c = Offset(size.width / 2, size.height / 2);
    final r = min(size.width, size.height) / 2 - 8;
    double sa = -pi / 2;
    for (var i = 0; i < data.length; i++) {
      final sw = (data[i].value / total) * 2 * pi;
      canvas.drawArc(Rect.fromCircle(center: c, radius: r), sa, sw, true, Paint()..color = _pieColor(i)..style = PaintingStyle.fill);
      sa += sw;
    }
  }
  @override
  bool shouldRepaint(covariant CustomPainter o) => false;
}

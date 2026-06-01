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
                final total = context.read<ChatProvider>().totalMessageCount;
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('约 ${total * 500} tokens')),
                );
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
}

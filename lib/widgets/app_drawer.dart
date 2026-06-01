import 'package:flutter/material.dart';
import '../screens/prompt_library_screen.dart';

class AppDrawer extends StatelessWidget {
  final int currentIndex;
  final ValueChanged<int> onSwitchTab;

  const AppDrawer({
    super.key,
    required this.currentIndex,
    required this.onSwitchTab,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Drawer(
      child: SafeArea(
        child: Column(
          children: [
            // 用户信息
            Container(
              padding: const EdgeInsets.all(20),
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 28,
                    backgroundColor: theme.colorScheme.primaryContainer,
                    child: Icon(
                      Icons.person,
                      color: theme.colorScheme.onPrimaryContainer,
                      size: 28,
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'OpenSeek 用户',
                          style: theme.textTheme.titleMedium,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'user@openseek.local',
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const Divider(height: 1),

            // 导航菜单
            _DrawerItem(
              icon: Icons.chat_bubble_outline,
              selectedIcon: Icons.chat_bubble,
              title: 'Chat',
              selected: currentIndex == 0,
              onTap: () {
                Navigator.pop(context);
                onSwitchTab(0);
              },
            ),
            _DrawerItem(
              icon: Icons.link_outlined,
              selectedIcon: Icons.link,
              title: 'Link',
              selected: currentIndex == 1,
              onTap: () {
                Navigator.pop(context);
                onSwitchTab(1);
              },
            ),
            _DrawerItem(
              icon: Icons.history_outlined,
              selectedIcon: Icons.history,
              title: 'History',
              selected: currentIndex == 2,
              onTap: () {
                Navigator.pop(context);
                onSwitchTab(2);
              },
            ),
            _DrawerItem(
              icon: Icons.settings_outlined,
              selectedIcon: Icons.settings,
              title: 'Settings',
              selected: currentIndex == 3,
              onTap: () {
                Navigator.pop(context);
                onSwitchTab(3);
              },
            ),
            _DrawerItem(
              icon: Icons.library_books_outlined,
              title: 'Prompt Library',
              onTap: () {
                Navigator.pop(context);
                Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => const PromptLibraryScreen(),
                  ),
                );
              },
            ),
            _DrawerItem(
              icon: Icons.info_outline,
              title: 'About',
              onTap: () {
                Navigator.pop(context);
                _showAbout(context);
              },
            ),

            const Spacer(),

            // 版本号
            Padding(
              padding: const EdgeInsets.all(16),
              child: Text(
                'Version 1.0.0',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showAbout(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        icon: Image.asset(
          'assets/icon.png',
          width: 48,
          height: 48,
          errorBuilder: (c, e, s) => const Icon(Icons.auto_awesome, size: 48),
        ),
        title: const Text('OpenSeek'),
        content: const Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('版本: 1.0.0'),
            SizedBox(height: 8),
            Text('基于大模型 API 的移动端 AI 助手'),
            SizedBox(height: 8),
            Text('开源协议: MIT'),
            SizedBox(height: 8),
            Text('隐私政策: https://openseek.local/privacy'),
          ],
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

class _DrawerItem extends StatelessWidget {
  final IconData icon;
  final IconData? selectedIcon;
  final String title;
  final VoidCallback onTap;
  final bool selected;

  const _DrawerItem({
    required this.icon,
    this.selectedIcon,
    required this.title,
    required this.onTap,
    this.selected = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return ListTile(
      leading: Icon(selected && selectedIcon != null ? selectedIcon : icon),
      title: Text(title),
      selected: selected,
      selectedTileColor: theme.colorScheme.secondaryContainer.withAlpha(80),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(28),
      ),
      onTap: onTap,
    );
  }
}

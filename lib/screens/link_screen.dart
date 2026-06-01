import 'package:flutter/material.dart';
import '../widgets/common/empty_state.dart';

class LinkScreen extends StatelessWidget {
  const LinkScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      children: [
        // Feature introduction card
        Padding(
          padding: const EdgeInsets.all(16),
          child: Card(
            elevation: 0,
            color: theme.colorScheme.primaryContainer.withAlpha(60),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(
                        Icons.link,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 8),
                      Text(
                        '连接你的电脑',
                        style: theme.textTheme.titleMedium?.copyWith(
                          color: theme.colorScheme.onPrimaryContainer,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(
                    '未来版本中，AI 可以通过本地网络连接电脑并执行任务，'
                    '例如文件操作、脚本执行、软件控制和自动化工作流。',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onPrimaryContainer.withAlpha(180),
                    ),
                  ),
                  const SizedBox(height: 12),
                  ActionChip(
                    avatar: const Icon(Icons.rocket_launch_outlined, size: 16),
                    label: const Text('Coming Soon'),
                    side: BorderSide.none,
                    backgroundColor: theme.colorScheme.primary.withAlpha(30),
                    onPressed: () {},
                  ),
                ],
              ),
            ),
          ),
        ),

        // Device list (empty state)
        Expanded(
          child: EmptyState(
            icon: Icons.computer_outlined,
            title: '暂无已连接设备',
            description:
                '连接同一局域网中的桌面客户端后将在此显示。',
            actionLabel: '了解更多',
            onAction: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Link 功能即将推出，敬请期待')),
              );
            },
            illustration: Icon(
              Icons.devices_other_outlined,
              size: 80,
              color: theme.colorScheme.primary.withAlpha(80),
            ),
          ),
        ),
      ],
    );
  }
}

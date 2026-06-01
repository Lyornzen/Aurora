class LinkedDevice {
  final String id;
  final String name;
  final String ip;
  final bool isOnline;
  final String systemType; // 'windows', 'macos', 'linux'
  final DateTime? lastConnected;

  const LinkedDevice({
    required this.id,
    required this.name,
    required this.ip,
    this.isOnline = false,
    required this.systemType,
    this.lastConnected,
  });

  String get osIcon {
    return switch (systemType) {
      'windows' => '⊞',
      'macos' => '⌘',
      'linux' => '🐧',
      _ => '💻',
    };
  }

  LinkedDevice copyWith({
    String? id,
    String? name,
    String? ip,
    bool? isOnline,
    String? systemType,
    DateTime? lastConnected,
  }) {
    return LinkedDevice(
      id: id ?? this.id,
      name: name ?? this.name,
      ip: ip ?? this.ip,
      isOnline: isOnline ?? this.isOnline,
      systemType: systemType ?? this.systemType,
      lastConnected: lastConnected ?? this.lastConnected,
    );
  }
}

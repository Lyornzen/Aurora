import 'package:uuid/uuid.dart';

const _uuid = Uuid();

class ChatSession {
  final String id;
  final String title;
  final DateTime createdAt;
  final DateTime updatedAt;
  final String? modelName;
  final int messageCount;
  final bool isArchived;
  final bool isPinned;

  ChatSession({
    String? id,
    required this.title,
    DateTime? createdAt,
    DateTime? updatedAt,
    this.modelName,
    this.messageCount = 0,
    this.isArchived = false,
    this.isPinned = false,
  })  : id = id ?? _uuid.v4(),
        createdAt = createdAt ?? DateTime.now(),
        updatedAt = updatedAt ?? DateTime.now();

  ChatSession copyWith({
    String? id,
    String? title,
    DateTime? createdAt,
    DateTime? updatedAt,
    String? modelName,
    int? messageCount,
    bool? isArchived,
    bool? isPinned,
  }) {
    return ChatSession(
      id: id ?? this.id,
      title: title ?? this.title,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      modelName: modelName ?? this.modelName,
      messageCount: messageCount ?? this.messageCount,
      isArchived: isArchived ?? this.isArchived,
      isPinned: isPinned ?? this.isPinned,
    );
  }

  String get summary => title;

  Map<String, dynamic> toJson() => {
        'id': id,
        'title': title,
        'createdAt': createdAt.toIso8601String(),
        'updatedAt': updatedAt.toIso8601String(),
        'modelName': modelName,
        'messageCount': messageCount,
        'isArchived': isArchived,
        'isPinned': isPinned,
      };

  factory ChatSession.fromJson(Map<String, dynamic> json) => ChatSession(
        id: json['id'],
        title: json['title'],
        createdAt: DateTime.parse(json['createdAt']),
        updatedAt: DateTime.parse(json['updatedAt']),
        modelName: json['modelName'],
        messageCount: json['messageCount'] ?? 0,
        isArchived: json['isArchived'] ?? false,
        isPinned: json['isPinned'] ?? false,
      );
}

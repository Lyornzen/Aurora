import 'package:uuid/uuid.dart';

const _uuid = Uuid();

enum MessageRole { user, assistant, system }

class ChatMessage {
  final String id;
  final String sessionId;
  final MessageRole role;
  final String content;
  final DateTime createdAt;
  final String? modelName;
  final List<ThinkingStep>? thinkingSteps;
  final bool isThinkingExpanded;
  final List<String>? filePaths;
  final bool isFavorited;

  ChatMessage({
    String? id,
    required this.sessionId,
    required this.role,
    required this.content,
    DateTime? createdAt,
    this.modelName,
    this.thinkingSteps,
    this.isThinkingExpanded = false,
    this.filePaths,
    this.isFavorited = false,
  })  : id = id ?? _uuid.v4(),
        createdAt = createdAt ?? DateTime.now();

  ChatMessage copyWith({
    String? id,
    String? sessionId,
    MessageRole? role,
    String? content,
    DateTime? createdAt,
    String? modelName,
    List<ThinkingStep>? thinkingSteps,
    bool? isThinkingExpanded,
    List<String>? filePaths,
    bool? isFavorited,
  }) {
    return ChatMessage(
      id: id ?? this.id,
      sessionId: sessionId ?? this.sessionId,
      role: role ?? this.role,
      content: content ?? this.content,
      createdAt: createdAt ?? this.createdAt,
      modelName: modelName ?? this.modelName,
      thinkingSteps: thinkingSteps ?? this.thinkingSteps,
      isThinkingExpanded: isThinkingExpanded ?? this.isThinkingExpanded,
      filePaths: filePaths ?? this.filePaths,
      isFavorited: isFavorited ?? this.isFavorited,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'sessionId': sessionId,
        'role': role.name,
        'content': content,
        'createdAt': createdAt.toIso8601String(),
        'modelName': modelName,
        'isFavorited': isFavorited,
      };

  factory ChatMessage.fromJson(Map<String, dynamic> json) => ChatMessage(
        id: json['id'],
        sessionId: json['sessionId'],
        role: MessageRole.values.firstWhere((e) => e.name == json['role']),
        content: json['content'],
        createdAt: DateTime.parse(json['createdAt']),
        modelName: json['modelName'],
        isFavorited: json['isFavorited'] ?? false,
      );
}

class ThinkingStep {
  final String description;
  final Duration? duration;
  final String? detail;

  const ThinkingStep({
    required this.description,
    this.duration,
    this.detail,
  });
}

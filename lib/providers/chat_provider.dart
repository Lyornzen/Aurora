import 'dart:async';
import 'package:flutter/material.dart';
import '../models/chat_message.dart';
import '../models/chat_session.dart';
import '../services/database_service.dart';
import '../services/api_service.dart';
import '../models/api_config.dart';

class ChatProvider extends ChangeNotifier {
  final List<ChatSession> _sessions = [];
  final Map<String, List<ChatMessage>> _messages = {};
  String? _currentSessionId;
  bool _loaded = false;
  bool _isStreaming = false;

  // Exposed for the streaming indicator
  bool get isStreaming => _isStreaming;
  bool get loaded => _loaded;

  // Stream controller for live updates during streaming
  StreamSubscription? _streamSubscription;

  List<ChatSession> get sessions =>
      _sessions.where((s) => s.messageCount > 0).toList();
  String? get currentSessionId => _currentSessionId;
  bool get hasCurrentSession => _currentSessionId != null;

  List<ChatMessage> get currentMessages =>
      _currentSessionId != null ? (_messages[_currentSessionId] ?? []) : [];

  ChatSession? get currentSession {
    if (_currentSessionId == null) return null;
    return _sessions.firstWhere(
      (s) => s.id == _currentSessionId,
      orElse: () => ChatSession(title: 'New Chat'),
    );
  }

  int get totalMessageCount =>
      _messages.values.fold(0, (sum, msgs) => sum + msgs.length);

  Map<String, int> get tokenUsageByModel {
    final map = <String, int>{};
    for (final msgs in _messages.values) {
      for (final m in msgs) {
        if (m.modelName != null && m.modelName!.isNotEmpty) {
          final tokens = (m.content.length * 0.5).round();
          map[m.modelName!] = (map[m.modelName!] ?? 0) + tokens;
        }
      }
    }
    return map;
  }

  int get totalTokens =>
      tokenUsageByModel.values.fold(0, (sum, v) => sum + v);

  /// Load all data from SQLite
  Future<void> load() async {
    if (_loaded) return;
    try {
      final sessions = await DatabaseService.loadSessions();

      for (final session in sessions) {
        final msgs = await DatabaseService.loadMessages(session.id);
        if (msgs.isEmpty) {
          // Clean up empty sessions from DB
          await DatabaseService.deleteSession(session.id);
        } else {
          _messages[session.id] = msgs;
          _sessions.add(session.copyWith(messageCount: msgs.length));
        }
      }

      if (_sessions.isNotEmpty) {
        _currentSessionId = _sessions.first.id;
      }
    } catch (e) {
      debugPrint('DB load error: $e');
    }
    _loaded = true;
    notifyListeners();
  }

  void createNewSession() {
    final session = ChatSession(title: 'New Chat');
    _sessions.insert(0, session);
    _messages[session.id] = [];
    _currentSessionId = session.id;
    // Don't save to DB until first message
    notifyListeners();
  }

  Future<void> selectSession(String id) async {
    _currentSessionId = id;
    // Load messages from DB if not in memory
    if (_messages[id] == null) {
      _messages[id] = await DatabaseService.loadMessages(id);
    }
    notifyListeners();
  }

  /// Send a message and get AI response (with real API)
  Future<void> sendMessage(
    String content, {
    String? modelName,
    ApiProviderConfig? apiConfig,
    bool enableThinking = true,
    String reasoningEffort = 'high',
  }) async {
    if (_currentSessionId == null) {
      createNewSession();
    }

    final sessionId = _currentSessionId!;

    // Add user message
    final userMsg = ChatMessage(
      sessionId: sessionId,
      role: MessageRole.user,
      content: content,
      modelName: modelName,
    );
    _messages[sessionId]?.add(userMsg);
    DatabaseService.saveMessage(userMsg);

    // Update session
    _updateSessionTitle(sessionId, content, modelName);

    // Create assistant placeholder
    final assistantMsg = ChatMessage(
      sessionId: sessionId,
      role: MessageRole.assistant,
      content: '',
      modelName: modelName,
    );
    _messages[sessionId]?.add(assistantMsg);
    notifyListeners();

    // If no API config, show fallback
    if (apiConfig == null || apiConfig.apiKey.isEmpty) {
      _setAssistantContent(
        sessionId,
        assistantMsg,
        '请在 **设置 → API 配置** 中添加你的 API 密钥以启用真实 AI 对话。\n\n你发送的消息是：\n> $content',
      );
      return;
    }

    // Prepare messages history for API
    final msgsForApi = _buildMessagesList(sessionId);

    // Call real API with streaming
    final apiService = ApiService(config: apiConfig);
    final model = modelName ?? apiConfig.models.firstOrNull ?? 'gpt-3.5-turbo';

    _isStreaming = true;
    notifyListeners();

    try {
      final stream = apiService.chatCompletionStream(
        messages: msgsForApi,
        model: model,
        enableThinking: enableThinking,
        reasoningEffort: reasoningEffort,
      );

      final buffer = StringBuffer();
      _streamSubscription = stream.listen(
        (chunk) {
          if (chunk.isError) {
            buffer.write(chunk.content ?? '');
            _setAssistantContent(sessionId, assistantMsg, buffer.toString());
            _isStreaming = false;
            notifyListeners();
            return;
          }

          if (chunk.isDone) {
            _isStreaming = false;
            // Save final message to DB
            final updated =
                _messages[sessionId]?.lastWhere((m) => m.id == assistantMsg.id);
            if (updated != null) {
              DatabaseService.saveMessage(updated);
            }
            // Update session message count
            _updateSessionCount(sessionId);
            notifyListeners();
            return;
          }

          if (chunk.content != null) {
            buffer.write(chunk.content);
            _setAssistantContent(sessionId, assistantMsg, buffer.toString());
          }
        },
        onError: (e) {
          _setAssistantContent(
              sessionId, assistantMsg, '请求出错: $e');
          _isStreaming = false;
          notifyListeners();
        },
      );
    } catch (e) {
      _setAssistantContent(sessionId, assistantMsg, '请求失败: $e');
      _isStreaming = false;
      notifyListeners();
    }
  }

  void cancelStream() {
    _streamSubscription?.cancel();
    _isStreaming = false;
    notifyListeners();
  }

  void _setAssistantContent(
      String sessionId, ChatMessage msg, String content) {
    final msgs = _messages[sessionId];
    if (msgs == null) return;
    final idx = msgs.indexWhere((m) => m.id == msg.id);
    if (idx >= 0) {
      msgs[idx] = msg.copyWith(content: content);
    }
    notifyListeners();
  }

  List<Map<String, dynamic>> _buildMessagesList(String sessionId) {
    final msgs = _messages[sessionId] ?? [];
    // Filter out empty assistant placeholders and build API format
    return msgs
        .where((m) =>
            m.content.isNotEmpty ||
            m.role == MessageRole.user) // always include user messages
        .map((m) => {
              'role': m.role == MessageRole.user
                  ? 'user'
                  : m.role == MessageRole.assistant
                      ? 'assistant'
                      : 'system',
              'content': m.content,
            })
        .toList();
  }

  void _updateSessionTitle(
      String sessionId, String content, String? modelName) {
    final idx = _sessions.indexWhere((s) => s.id == sessionId);
    if (idx >= 0) {
      final title =
          content.length > 30 ? '${content.substring(0, 30)}...' : content;
      _sessions[idx] = _sessions[idx].copyWith(
        title: title,
        updatedAt: DateTime.now(),
        messageCount: (_messages[sessionId]?.length ?? 0) + 1,
        modelName: modelName,
      );
      DatabaseService.saveSession(_sessions[idx]);
    }
  }

  void _updateSessionCount(String sessionId) {
    final idx = _sessions.indexWhere((s) => s.id == sessionId);
    if (idx >= 0) {
      _sessions[idx] = _sessions[idx].copyWith(
        messageCount: _messages[sessionId]?.length ?? 0,
      );
      DatabaseService.saveSession(_sessions[idx]);
    }
  }

  void deleteSession(String id) {
    _sessions.removeWhere((s) => s.id == id);
    _messages.remove(id);
    if (_currentSessionId == id) {
      _currentSessionId = _sessions.isNotEmpty ? _sessions.first.id : null;
    }
    DatabaseService.deleteSession(id);
    notifyListeners();
  }

  void toggleFavorite(String messageId) async {
    for (final msgs in _messages.values) {
      final idx = msgs.indexWhere((m) => m.id == messageId);
      if (idx >= 0) {
        final newVal = !msgs[idx].isFavorited;
        msgs[idx] = msgs[idx].copyWith(isFavorited: newVal);
        DatabaseService.toggleFavorite(messageId, newVal);
        notifyListeners();
        return;
      }
    }
  }

  void renameSession(String id, String newTitle) {
    final idx = _sessions.indexWhere((s) => s.id == id);
    if (idx >= 0) {
      _sessions[idx] = _sessions[idx].copyWith(title: newTitle);
      DatabaseService.saveSession(_sessions[idx]);
      notifyListeners();
    }
  }

  void clearCurrentSession() {
    if (_currentSessionId != null) {
      _messages[_currentSessionId!]?.clear();
      DatabaseService.deleteMessages(_currentSessionId!);
      notifyListeners();
    }
  }

  void togglePinSession(String id) {
    final idx = _sessions.indexWhere((s) => s.id == id);
    if (idx >= 0) {
      _sessions[idx] =
          _sessions[idx].copyWith(isPinned: !_sessions[idx].isPinned);
      DatabaseService.saveSession(_sessions[idx]);
      notifyListeners();
    }
  }

  void archiveSession(String id) {
    final idx = _sessions.indexWhere((s) => s.id == id);
    if (idx >= 0) {
      _sessions[idx] =
          _sessions[idx].copyWith(isArchived: !_sessions[idx].isArchived);
      DatabaseService.saveSession(_sessions[idx]);
      notifyListeners();
    }
  }

  List<ChatSession> filterSessions(String query) {
    if (query.isEmpty) return _sessions;
    return _sessions
        .where((s) =>
            s.title.toLowerCase().contains(query.toLowerCase()) ||
            (s.modelName?.toLowerCase().contains(query.toLowerCase()) ?? false))
        .toList();
  }

  @override
  void dispose() {
    _streamSubscription?.cancel();
    super.dispose();
  }
}

import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart' as p;
import '../models/chat_message.dart';
import '../models/chat_session.dart';
import '../models/api_config.dart';

class DatabaseService {
  static Database? _database;
  static const String _dbName = 'openseek.db';
  static const int _dbVersion = 1;

  static Future<Database> get database async {
    if (_database != null) return _database!;
    final dbPath = await getDatabasesPath();
    final path = p.join(dbPath, _dbName);
    _database = await openDatabase(
      path,
      version: _dbVersion,
      onCreate: _onCreate,
    );
    return _database!;
  }

  static Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE sessions (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL,
        model_name TEXT,
        message_count INTEGER DEFAULT 0,
        is_archived INTEGER DEFAULT 0,
        is_pinned INTEGER DEFAULT 0
      )
    ''');

    await db.execute('''
      CREATE TABLE messages (
        id TEXT PRIMARY KEY,
        session_id TEXT NOT NULL,
        role TEXT NOT NULL,
        content TEXT NOT NULL,
        created_at TEXT NOT NULL,
        model_name TEXT,
        is_favorited INTEGER DEFAULT 0,
        FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
      )
    ''');

    await db.execute('''
      CREATE TABLE prompt_templates (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        content TEXT NOT NULL,
        category TEXT,
        created_at TEXT NOT NULL,
        is_builtin INTEGER DEFAULT 0
      )
    ''');

    await db.execute('''
      CREATE TABLE api_providers (
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        api_key TEXT DEFAULT '',
        base_url TEXT DEFAULT '',
        models TEXT DEFAULT '',
        provider TEXT,
        is_enabled INTEGER DEFAULT 1
      )
    ''');

    await db.execute('''
      CREATE TABLE model_settings (
        id INTEGER PRIMARY KEY DEFAULT 1,
        default_chat_model TEXT DEFAULT '',
        default_vision_model TEXT,
        default_reasoning_model TEXT,
        context_length INTEGER DEFAULT 4096,
        temperature REAL DEFAULT 0.7,
        top_p REAL DEFAULT 0.9,
        frequency_penalty REAL DEFAULT 0.0
      )
    ''');

    // Insert default model settings
    await db.insert('model_settings', {
      'id': 1,
      'default_chat_model': '',
    });

    // Insert built-in prompts
    await _insertBuiltInPrompts(db);
  }

  static Future<void> _insertBuiltInPrompts(Database db) async {
    final prompts = [
      {
        'id': 'builtin_translate',
        'title': '翻译助手',
        'content': '请将以下内容翻译为{{语言}}，保持原意和语气：\n\n{{内容}}',
        'category': '语言',
      },
      {
        'id': 'builtin_summarize',
        'title': '内容总结',
        'content': '请用简洁的语言总结以下内容，提取关键要点：\n\n{{内容}}',
        'category': '写作',
      },
      {
        'id': 'builtin_code_review',
        'title': '代码审查',
        'content': '请审查以下代码，指出潜在问题、性能瓶颈和最佳实践建议：\n\n```\n{{代码}}\n```',
        'category': '编程',
      },
      {
        'id': 'builtin_explain',
        'title': '概念解释',
        'content': '请用通俗易懂的方式解释以下概念，并给出实际例子：\n\n{{概念}}',
        'category': '学习',
      },
      {
        'id': 'builtin_writing',
        'title': '写作助手',
        'content': '请帮我写一段关于"{{主题}}"的文字，风格为{{风格}}，字数约{{字数}}字。',
        'category': '写作',
      },
      {
        'id': 'builtin_brainstorm',
        'title': '头脑风暴',
        'content': '针对以下主题，请给出 10 个创意想法或方案：\n\n{{主题}}',
        'category': '创意',
      },
    ];

    for (final p in prompts) {
      await db.insert('prompt_templates', {
        'id': p['id'],
        'title': p['title'],
        'content': p['content'],
        'category': p['category'],
        'created_at': DateTime.now().toIso8601String(),
        'is_builtin': 1,
      });
    }
  }

  // ==================== Sessions ====================

  static Future<List<ChatSession>> loadSessions() async {
    final db = await database;
    final rows = await db.query('sessions', orderBy: 'updated_at DESC');
    return rows.map((r) => ChatSession.fromJson(_rowToSession(r))).toList();
  }

  static Future<void> saveSession(ChatSession session) async {
    final db = await database;
    await db.insert(
      'sessions',
      _sessionToRow(session),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  static Future<void> deleteSession(String id) async {
    final db = await database;
    await db.delete('messages', where: 'session_id = ?', whereArgs: [id]);
    await db.delete('sessions', where: 'id = ?', whereArgs: [id]);
  }

  // ==================== Messages ====================

  static Future<List<ChatMessage>> loadMessages(String sessionId) async {
    final db = await database;
    final rows = await db.query(
      'messages',
      where: 'session_id = ?',
      whereArgs: [sessionId],
      orderBy: 'created_at ASC',
    );
    return rows.map((r) => ChatMessage.fromJson(_rowToMessage(r))).toList();
  }

  static Future<void> saveMessage(ChatMessage message) async {
    final db = await database;
    await db.insert(
      'messages',
      _messageToRow(message),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  static Future<void> saveMessages(List<ChatMessage> messages) async {
    final db = await database;
    final batch = db.batch();
    for (final msg in messages) {
      batch.insert(
        'messages',
        _messageToRow(msg),
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    }
    await batch.commit(noResult: true);
  }

  static Future<void> deleteMessages(String sessionId) async {
    final db = await database;
    await db.delete('messages', where: 'session_id = ?', whereArgs: [sessionId]);
  }

  static Future<void> toggleFavorite(String messageId, bool value) async {
    final db = await database;
    await db.update(
      'messages',
      {'is_favorited': value ? 1 : 0},
      where: 'id = ?',
      whereArgs: [messageId],
    );
  }

  // ==================== API Providers ====================

  static Future<List<ApiProviderConfig>> loadProviders() async {
    final db = await database;
    final rows = await db.query('api_providers');
    return rows.map((r) => ApiProviderConfig(
      id: r['id'] as String,
      name: r['name'] as String,
      apiKey: r['api_key'] as String? ?? '',
      baseUrl: r['base_url'] as String? ?? '',
      models: (r['models'] as String? ?? '').split(',').where((m) => m.isNotEmpty).toList(),
      provider: r['provider'] as String?,
      isEnabled: (r['is_enabled'] as int?) == 1,
    )).toList();
  }

  static Future<void> saveProvider(ApiProviderConfig config) async {
    final db = await database;
    await db.insert(
      'api_providers',
      {
        'id': config.id,
        'name': config.name,
        'api_key': config.apiKey,
        'base_url': config.baseUrl,
        'models': config.models.join(','),
        'provider': config.provider,
        'is_enabled': config.isEnabled ? 1 : 0,
      },
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  static Future<void> deleteProvider(String id) async {
    final db = await database;
    await db.delete('api_providers', where: 'id = ?', whereArgs: [id]);
  }

  // ==================== Model Settings ====================

  static Future<ModelSettings?> loadModelSettings() async {
    final db = await database;
    final rows = await db.query('model_settings', limit: 1);
    if (rows.isEmpty) return null;
    final r = rows.first;
    return ModelSettings(
      defaultChatModel: r['default_chat_model'] as String? ?? '',
      defaultVisionModel: r['default_vision_model'] as String?,
      defaultReasoningModel: r['default_reasoning_model'] as String?,
      contextLength: r['context_length'] as int? ?? 4096,
      temperature: (r['temperature'] as num?)?.toDouble() ?? 0.7,
      topP: (r['top_p'] as num?)?.toDouble() ?? 0.9,
      frequencyPenalty: (r['frequency_penalty'] as num?)?.toDouble() ?? 0.0,
    );
  }

  static Future<void> saveModelSettings(ModelSettings settings) async {
    final db = await database;
    await db.update(
      'model_settings',
      {
        'default_chat_model': settings.defaultChatModel,
        'default_vision_model': settings.defaultVisionModel,
        'default_reasoning_model': settings.defaultReasoningModel,
        'context_length': settings.contextLength,
        'temperature': settings.temperature,
        'top_p': settings.topP,
        'frequency_penalty': settings.frequencyPenalty,
      },
      where: 'id = 1',
    );
  }

  // ==================== Prompt Templates ====================

  static Future<List<Map<String, dynamic>>> loadPromptTemplates() async {
    final db = await database;
    return db.query('prompt_templates', orderBy: 'created_at DESC');
  }

  static Future<void> savePromptTemplate(Map<String, dynamic> prompt) async {
    final db = await database;
    await db.insert(
      'prompt_templates',
      prompt,
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  static Future<void> deletePromptTemplate(String id) async {
    final db = await database;
    await db.delete('prompt_templates', where: 'id = ?', whereArgs: [id]);
  }

  // ==================== Row Mappers ====================

  static Map<String, dynamic> _sessionToRow(ChatSession s) => {
        'id': s.id,
        'title': s.title,
        'created_at': s.createdAt.toIso8601String(),
        'updated_at': s.updatedAt.toIso8601String(),
        'model_name': s.modelName,
        'message_count': s.messageCount,
        'is_archived': s.isArchived ? 1 : 0,
        'is_pinned': s.isPinned ? 1 : 0,
      };

  static Map<String, dynamic> _rowToSession(Map<String, dynamic> r) => {
        'id': r['id'],
        'title': r['title'],
        'createdAt': r['created_at'],
        'updatedAt': r['updated_at'],
        'modelName': r['model_name'],
        'messageCount': r['message_count'] ?? 0,
        'isArchived': (r['is_archived'] as int?) == 1,
        'isPinned': (r['is_pinned'] as int?) == 1,
      };

  static Map<String, dynamic> _messageToRow(ChatMessage m) => {
        'id': m.id,
        'session_id': m.sessionId,
        'role': m.role.name,
        'content': m.content,
        'created_at': m.createdAt.toIso8601String(),
        'model_name': m.modelName,
        'is_favorited': m.isFavorited ? 1 : 0,
      };

  static Map<String, dynamic> _rowToMessage(Map<String, dynamic> r) => {
        'id': r['id'],
        'sessionId': r['session_id'],
        'role': r['role'],
        'content': r['content'],
        'createdAt': r['created_at'],
        'modelName': r['model_name'],
        'isFavorited': (r['is_favorited'] as int?) == 1,
      };

  static Future<void> close() async {
    await _database?.close();
    _database = null;
  }
}

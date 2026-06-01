import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/api_config.dart';

class ApiService {
  final ApiProviderConfig config;
  final http.Client _client;

  ApiService({required this.config})
      : _client = http.Client() {
    // Set socket timeout for DNS and connection
  }

  /// Send a chat completion request (non-streaming)
  Future<ChatResponse> chatCompletion({
    required List<Map<String, dynamic>> messages,
    required String model,
    double temperature = 0.7,
    double topP = 0.9,
    int maxTokens = 4096,
  }) async {
    final url = _buildUrl('/chat/completions');
    final body = {
      'model': model,
      'messages': messages,
      'temperature': temperature,
      'top_p': topP,
      'max_tokens': maxTokens,
      'stream': false,
    };

    try {
      final response = await _client.post(
        Uri.parse(url),
        headers: _headers,
        body: jsonEncode(body),
      ).timeout(const Duration(seconds: 60));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final content =
            data['choices']?[0]?['message']?['content'] ?? '';
        final usage = data['usage'];
        return ChatResponse(
          content: content,
          model: data['model'] ?? model,
          promptTokens: usage?['prompt_tokens'] ?? 0,
          completionTokens: usage?['completion_tokens'] ?? 0,
        );
      } else {
        return ChatResponse(
          content: 'API 错误 (${response.statusCode}): ${response.body}',
          model: model,
          isError: true,
        );
      }
    } catch (e) {
      return ChatResponse(
        content: _formatError(e),
        model: model,
        isError: true,
      );
    }
  }

  /// Send a streaming chat completion request
  Stream<ChatStreamChunk> chatCompletionStream({
    required List<Map<String, dynamic>> messages,
    required String model,
    double temperature = 0.7,
    double topP = 0.9,
    int maxTokens = 4096,
    bool enableThinking = false,
  }) async* {
    final url = _buildUrl('/chat/completions');
    final body = <String, dynamic>{
      'model': model,
      'messages': messages,
      'temperature': temperature,
      'top_p': topP,
      'max_tokens': maxTokens,
      'stream': true,
    };

    // DeepSeek-specific parameters
    if (config.provider == 'deepseek' || config.baseUrl.contains('deepseek')) {
      body['thinking'] = {
        'type': enableThinking ? 'enabled' : 'disabled',
      };
      body['response_format'] = {'type': 'text'};
    }

    try {
      final request = http.Request('POST', Uri.parse(url));
      request.headers.addAll(_headers);
      request.body = jsonEncode(body);

      final response = await _client.send(request).timeout(
            const Duration(seconds: 120),
          );

      if (response.statusCode != 200) {
        final errorBody = await response.stream.bytesToString();
        yield ChatStreamChunk(
          content: 'API 错误 (${response.statusCode}): $errorBody',
          isDone: true,
          isError: true,
        );
        return;
      }

      String buffer = '';
      await for (final chunk in response.stream.transform(utf8.decoder)) {
        buffer += chunk;
        while (buffer.contains('\n')) {
          final newlineIdx = buffer.indexOf('\n');
          final line = buffer.substring(0, newlineIdx).trim();
          buffer = buffer.substring(newlineIdx + 1);

          if (line.isEmpty || !line.startsWith('data: ')) continue;
          final data = line.substring(6);

          if (data == '[DONE]') {
            yield ChatStreamChunk(isDone: true);
            return;
          }

          try {
            final json = jsonDecode(data);
            final delta = json['choices']?[0]?['delta'];
            if (delta != null && delta['content'] != null) {
              yield ChatStreamChunk(
                content: delta['content'],
                model: json['model'] ?? model,
              );
            }
          } catch (_) {
            // Skip malformed SSE lines
          }
        }
      }

      yield ChatStreamChunk(isDone: true);
    } catch (e) {
      final msg = '流式请求失败: $e';
      // Provide helpful diagnostics for common errors
      String hint = '';
      if (e.toString().contains('Failed host lookup') ||
          e.toString().contains('No address associated')) {
        hint = '\n\n💡 提示:\n'
            '• 请检查设备网络连接\n'
            '• 确认 Base URL 可访问: ${_buildUrl('/chat/completions')}\n'
            '• 尝试在浏览器中打开该 URL 验证\n'
            '• 如使用模拟器，检查模拟器网络设置';
      } else if (e.toString().contains('Connection refused')) {
        hint = '\n\n💡 提示: 服务器拒绝连接，请检查 Base URL 和端口是否正确';
      } else if (e.toString().contains('timeout')) {
        hint = '\n\n💡 提示: 连接超时，请检查网络或 API 服务状态';
      }
      yield ChatStreamChunk(
        content: '$msg$hint',
        isDone: true,
        isError: true,
      );
    }
  }

  /// Fetch available models from the API
  Future<List<String>> fetchModels() async {
    final url = _buildUrl('/models');
    try {
      final response = await _client
          .get(Uri.parse(url), headers: _headers)
          .timeout(const Duration(seconds: 15));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        // Standard OpenAI format: { object: "list", data: [{ id: "..." }] }
        return (data['data'] as List?)
                ?.map((m) => m['id'] as String)
                .toList() ??
            [];
      }
      // DeepSeek and some providers return 404 on /models but are still working
    } catch (_) {}
    return [];
  }

  /// Test API connection
  Future<ConnectionTest> testConnection() async {
    final url = _buildUrl('/models');
    final sw = Stopwatch()..start();
    try {
      final response = await _client
          .get(Uri.parse(url), headers: _headers)
          .timeout(const Duration(seconds: 15));
      sw.stop();

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final models = (data['data'] as List?)
                ?.map((m) => m['id'] as String)
                .toList() ??
            [];
        return ConnectionTest(
          success: true,
          latencyMs: sw.elapsedMilliseconds,
          modelCount: models.length,
        );
      }

      // Try alternate endpoint for Anthropic
      if (response.statusCode == 404) {
        return ConnectionTest(
          success: true,
          latencyMs: sw.elapsedMilliseconds,
          modelCount: 0,
          note: '连接正常 (404 on /models - some providers limit this endpoint)',
        );
      }

      return ConnectionTest(
        success: false,
        latencyMs: sw.elapsedMilliseconds,
        error: 'HTTP ${response.statusCode}',
      );
    } catch (e) {
      sw.stop();
      return ConnectionTest(
        success: false,
        latencyMs: sw.elapsedMilliseconds,
        error: e.toString(),
      );
    }
  }

  String _buildUrl(String path) {
    String base = config.baseUrl.trim();
    if (base.endsWith('/')) base = base.substring(0, base.length - 1);
    // Auto-append /v1 for OpenAI-compatible APIs that don't include it
    if (!base.endsWith('/v1') &&
        !base.contains('generativelanguage') && // Google: no /v1
        !base.contains('anthropic') &&          // Anthropic: no /v1
        !base.contains('deepseek.com') &&       // DeepSeek: no /v1
        !base.contains('openrouter.ai')) {      // OpenRouter: own path
      base = '$base/v1';
    }
    return '$base$path';
  }

  Map<String, String> get _headers {
    final headers = <String, String>{
      'Content-Type': 'application/json',
    };

    if (config.apiKey.isNotEmpty) {
      // Anthropic uses x-api-key
      if (config.provider == 'anthropic') {
        headers['x-api-key'] = config.apiKey;
        headers['anthropic-version'] = '2023-06-01';
      } else {
        headers['Authorization'] = 'Bearer ${config.apiKey}';
      }
    }

    return headers;
  }

  String _formatError(Object e) {
    final msg = '请求失败: $e';
    String hint = '';
    final errStr = e.toString();
    if (errStr.contains('Failed host lookup') ||
        errStr.contains('No address associated')) {
      hint = '\n\n💡 提示:\n'
          '• 请检查设备网络连接\n'
          '• 确认 Base URL 可访问: ${config.baseUrl}\n'
          '• 尝试在浏览器中打开该 URL 验证\n'
          '• 如使用模拟器，检查模拟器网络设置';
    } else if (errStr.contains('Connection refused')) {
      hint = '\n\n💡 提示: 服务器拒绝连接，请检查 Base URL 和端口是否正确';
    } else if (errStr.contains('timeout')) {
      hint = '\n\n💡 提示: 连接超时，请检查网络或 API 服务状态';
    }
    return '$msg$hint';
  }

  void dispose() {
    _client.close();
  }
}

class ChatResponse {
  final String content;
  final String model;
  final int promptTokens;
  final int completionTokens;
  final bool isError;

  const ChatResponse({
    required this.content,
    required this.model,
    this.promptTokens = 0,
    this.completionTokens = 0,
    this.isError = false,
  });

  int get totalTokens => promptTokens + completionTokens;
}

class ChatStreamChunk {
  final String? content;
  final String? model;
  final bool isDone;
  final bool isError;

  const ChatStreamChunk({
    this.content,
    this.model,
    this.isDone = false,
    this.isError = false,
  });
}

class ConnectionTest {
  final bool success;
  final int latencyMs;
  final int modelCount;
  final String? error;
  final String? note;

  const ConnectionTest({
    required this.success,
    required this.latencyMs,
    this.modelCount = 0,
    this.error,
    this.note,
  });
}

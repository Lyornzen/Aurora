class ApiProviderConfig {
  final String id;
  final String name;
  final String apiKey;
  final String baseUrl;
  final List<String> models;
  final String? provider;
  bool isEnabled;

  ApiProviderConfig({
    required this.id,
    required this.name,
    this.apiKey = '',
    this.baseUrl = '',
    this.models = const [],
    this.provider,
    this.isEnabled = true,
  });

  static const Map<String, String> knownProviders = {
    'openai': 'OpenAI',
    'anthropic': 'Anthropic',
    'google': 'Google',
    'deepseek': 'DeepSeek',
    'openrouter': 'OpenRouter',
    'custom': 'Custom (OpenAI Compatible)',
  };

  static const Map<String, String> knownBaseUrls = {
    'openai': 'https://api.openai.com/v1',
    'anthropic': 'https://api.anthropic.com',
    'google': 'https://generativelanguage.googleapis.com',
    'deepseek': 'https://api.deepseek.com',
    'openrouter': 'https://openrouter.ai/api/v1',
  };

  ApiProviderConfig copyWith({
    String? id,
    String? name,
    String? apiKey,
    String? baseUrl,
    List<String>? models,
    String? provider,
    bool? isEnabled,
  }) {
    return ApiProviderConfig(
      id: id ?? this.id,
      name: name ?? this.name,
      apiKey: apiKey ?? this.apiKey,
      baseUrl: baseUrl ?? this.baseUrl,
      models: models ?? this.models,
      provider: provider ?? this.provider,
      isEnabled: isEnabled ?? this.isEnabled,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'apiKey': apiKey,
        'baseUrl': baseUrl,
        'models': models,
        'provider': provider,
        'isEnabled': isEnabled,
      };

  factory ApiProviderConfig.fromJson(Map<String, dynamic> json) =>
      ApiProviderConfig(
        id: json['id'],
        name: json['name'],
        apiKey: json['apiKey'] ?? '',
        baseUrl: json['baseUrl'] ?? '',
        models: List<String>.from(json['models'] ?? []),
        provider: json['provider'],
        isEnabled: json['isEnabled'] ?? true,
      );
}

class ModelSettings {
  String defaultChatModel;
  String? defaultVisionModel;
  String? defaultReasoningModel;
  int contextLength;
  double temperature;
  double topP;
  double frequencyPenalty;

  ModelSettings({
    this.defaultChatModel = '',
    this.defaultVisionModel,
    this.defaultReasoningModel,
    this.contextLength = 4096,
    this.temperature = 0.7,
    this.topP = 0.9,
    this.frequencyPenalty = 0.0,
  });

  Map<String, dynamic> toJson() => {
        'defaultChatModel': defaultChatModel,
        'defaultVisionModel': defaultVisionModel,
        'defaultReasoningModel': defaultReasoningModel,
        'contextLength': contextLength,
        'temperature': temperature,
        'topP': topP,
        'frequencyPenalty': frequencyPenalty,
      };

  factory ModelSettings.fromJson(Map<String, dynamic> json) => ModelSettings(
        defaultChatModel: json['defaultChatModel'] ?? '',
        defaultVisionModel: json['defaultVisionModel'],
        defaultReasoningModel: json['defaultReasoningModel'],
        contextLength: json['contextLength'] ?? 4096,
        temperature: (json['temperature'] ?? 0.7).toDouble(),
        topP: (json['topP'] ?? 0.9).toDouble(),
        frequencyPenalty: (json['frequencyPenalty'] ?? 0.0).toDouble(),
      );
}

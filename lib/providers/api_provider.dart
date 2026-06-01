import 'package:flutter/material.dart';
import '../models/api_config.dart';
import '../services/database_service.dart';

import 'dart:async';

class ApiProvider extends ChangeNotifier {
  List<ApiProviderConfig> _providers = [];
  ModelSettings _modelSettings = ModelSettings();
  bool _loaded = false;

  List<ApiProviderConfig> get providers => _providers;
  List<ApiProviderConfig> get enabledProviders =>
      _providers.where((p) => p.isEnabled && p.apiKey.isNotEmpty).toList();
  ModelSettings get modelSettings => _modelSettings;
  bool get loaded => _loaded;

  Future<void> load() async {
    if (_loaded) return;
    try {
      _providers = await DatabaseService.loadProviders();

      if (_providers.isEmpty) {
        // Seed default providers
        _providers = [
          ApiProviderConfig(
            id: 'openai',
            name: 'OpenAI',
            provider: 'openai',
            baseUrl: ApiProviderConfig.knownBaseUrls['openai'] ?? '',
          ),
          ApiProviderConfig(
            id: 'deepseek',
            name: 'DeepSeek',
            provider: 'deepseek',
            baseUrl: ApiProviderConfig.knownBaseUrls['deepseek'] ?? '',
          ),
          ApiProviderConfig(
            id: 'anthropic',
            name: 'Anthropic',
            provider: 'anthropic',
            baseUrl: ApiProviderConfig.knownBaseUrls['anthropic'] ?? '',
          ),
          ApiProviderConfig(
            id: 'openrouter',
            name: 'OpenRouter',
            provider: 'openrouter',
            baseUrl: ApiProviderConfig.knownBaseUrls['openrouter'] ?? '',
          ),
        ];
        for (final p in _providers) {
          await DatabaseService.saveProvider(p);
        }
      }

      final settings = await DatabaseService.loadModelSettings();
      if (settings != null) {
        _modelSettings = settings;
      }
    } catch (e) {
      debugPrint('API provider load error: $e');
    }
    _loaded = true;
    notifyListeners();
  }

  Future<void> saveProvider(ApiProviderConfig config) async {
    final idx = _providers.indexWhere((p) => p.id == config.id);
    if (idx >= 0) {
      _providers[idx] = config;
    } else {
      _providers.add(config);
    }
    await DatabaseService.saveProvider(config);
    notifyListeners();
  }

  Future<void> deleteProvider(String id) async {
    _providers.removeWhere((p) => p.id == id);
    await DatabaseService.deleteProvider(id);
    notifyListeners();
  }

  Future<void> toggleProvider(String id) async {
    final idx = _providers.indexWhere((p) => p.id == id);
    if (idx >= 0) {
      _providers[idx].isEnabled = !_providers[idx].isEnabled;
      await DatabaseService.saveProvider(_providers[idx]);
      notifyListeners();
    }
  }

  Future<void> saveModelSettings(ModelSettings settings) async {
    _modelSettings = settings;
    await DatabaseService.saveModelSettings(settings);
    notifyListeners();
  }

  int get configuredModelCount =>
      _providers.fold(0, (sum, p) => sum + p.models.length);

  /// Get the best available API config for chat
  ApiProviderConfig? getActiveConfig(String? modelName) {
    if (modelName != null) {
      // Find the provider that has this model
      for (final p in enabledProviders) {
        if (p.models.contains(modelName)) return p;
      }
    }
    // Fall back to first enabled provider
    return enabledProviders.firstOrNull;
  }
}

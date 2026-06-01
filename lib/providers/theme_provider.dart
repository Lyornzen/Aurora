import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../theme/app_theme.dart';

class ThemeProvider extends ChangeNotifier {
  ThemeMode _themeMode = ThemeMode.system;
  Color _seedColor = const Color(0xFF6750A4);
  int _seedColorIndex = 0;

  ThemeMode get themeMode => _themeMode;
  Color get seedColor => _seedColor;
  int get seedColorIndex => _seedColorIndex;

  static const List<Color> presetColors = [
    Color(0xFF6750A4), // 紫色 (默认)
    Color(0xFF1565C0), // 蓝色
    Color(0xFF006D3B), // 绿色
    Color(0xFFFF6D00), // 橙色
    Color(0xFF616161), // 灰色
  ];

  static const List<String> colorNames = ['紫色', '蓝色', '绿色', '橙色', '灰色'];

  Future<void> load() async {
    final prefs = await SharedPreferences.getInstance();
    final modeStr = prefs.getString('themeMode') ?? 'system';
    _themeMode = switch (modeStr) {
      'light' => ThemeMode.light,
      'dark' => ThemeMode.dark,
      _ => ThemeMode.system,
    };
    _seedColorIndex = prefs.getInt('seedColorIndex') ?? 0;
    _seedColor = presetColors[_seedColorIndex.clamp(0, presetColors.length - 1)];
    notifyListeners();
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    _themeMode = mode;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('themeMode', mode.name);
    notifyListeners();
  }

  Future<void> setSeedColor(int index) async {
    if (index < 0 || index >= presetColors.length) return;
    _seedColorIndex = index;
    _seedColor = presetColors[index];
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt('seedColorIndex', index);
    notifyListeners();
  }

  ThemeData get lightTheme => AppTheme.light(_seedColor);
  ThemeData get darkTheme => AppTheme.dark(_seedColor);
}

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../theme/app_theme.dart';

class ThemeProvider extends ChangeNotifier {
  ThemeMode _themeMode = ThemeMode.system;
  int _seedColorIndex = 0;
  int _fontSizeIndex = 1;

  ThemeMode get themeMode => _themeMode;
  int get seedColorIndex => _seedColorIndex;
  int get fontSizeIndex => _fontSizeIndex;

  static const List<Color> presetColors = [
    Color(0xFF7C4DFF), // Aurora Purple
    Color(0xFF1565C0), // Blue
    Color(0xFF006D3B), // Green
    Color(0xFFFF6D00), // Orange
    Color(0xFFE91E63), // Pink
    Color(0xFF616161), // Gray
  ];
  static const List<String> colorNames = ['Purple', 'Blue', 'Green', 'Orange', 'Pink', 'Gray'];

  static const List<double> fontScaleFactors = [0.75, 0.85, 1.0, 1.15];
  static const List<String> fontSizeLabels = ['Small', 'Standard', 'Large', 'X-Large'];

  double get textScaleFactor => fontScaleFactors[_fontSizeIndex];
  Color get seedColor => presetColors[_seedColorIndex];

  Future<void> load() async {
    final prefs = await SharedPreferences.getInstance();
    _themeMode = switch (prefs.getString('themeMode') ?? 'system') {
      'light' => ThemeMode.light, 'dark' => ThemeMode.dark, _ => ThemeMode.system,
    };
    _seedColorIndex = prefs.getInt('seedColorIndex') ?? 0;
    _fontSizeIndex = prefs.getInt('fontSizeIndex') ?? 1;
    notifyListeners();
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    _themeMode = mode;
    final p = await SharedPreferences.getInstance(); await p.setString('themeMode', mode.name);
    notifyListeners();
  }

  Future<void> setSeedColor(int index) async {
    _seedColorIndex = index.clamp(0, presetColors.length - 1);
    final p = await SharedPreferences.getInstance(); await p.setInt('seedColorIndex', _seedColorIndex);
    notifyListeners();
  }

  Future<void> setFontSize(int index) async {
    _fontSizeIndex = index.clamp(0, fontScaleFactors.length - 1);
    final p = await SharedPreferences.getInstance(); await p.setInt('fontSizeIndex', _fontSizeIndex);
    notifyListeners();
  }

  ThemeData get lightTheme => AppTheme.light(seedColor);
  ThemeData get darkTheme => AppTheme.dark(seedColor);
}

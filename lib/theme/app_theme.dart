import 'package:flutter/material.dart';

class AppTheme {
  static const _lightSchemeSeed = Color(0xFF6750A4);

  static ThemeData light(Color? seedColor) {
    final scheme = ColorScheme.fromSeed(
      seedColor: seedColor ?? _lightSchemeSeed,
      brightness: Brightness.light,
    );
    return _buildTheme(scheme);
  }

  static ThemeData dark(Color? seedColor) {
    final scheme = ColorScheme.fromSeed(
      seedColor: seedColor ?? _lightSchemeSeed,
      brightness: Brightness.dark,
    );
    return _buildTheme(scheme);
  }

  static ThemeData _buildTheme(ColorScheme scheme) {
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      brightness: scheme.brightness,
      appBarTheme: AppBarTheme(
        centerTitle: false,
        elevation: 0,
        backgroundColor: scheme.surface,
        foregroundColor: scheme.onSurface,
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: scheme.surfaceContainerHighest,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(24),
          borderSide: BorderSide.none,
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
      ),
      navigationBarTheme: NavigationBarThemeData(
        elevation: 0,
        backgroundColor: scheme.surface,
        indicatorColor: scheme.secondaryContainer,
      ),
      chipTheme: ChipThemeData(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
      ),
      bottomSheetTheme: BottomSheetThemeData(
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
        ),
      ),
    );
  }

  static const ColorScheme presetBlue = ColorScheme(
    brightness: Brightness.light,
    primary: Color(0xFF1565C0),
    onPrimary: Color(0xFFFFFFFF),
    primaryContainer: Color(0xFFD1E4FF),
    onPrimaryContainer: Color(0xFF001D36),
    secondary: Color(0xFF535F70),
    onSecondary: Color(0xFFFFFFFF),
    secondaryContainer: Color(0xFFD7E3F7),
    onSecondaryContainer: Color(0xFF101C2B),
    surface: Color(0xFFFDFBFF),
    onSurface: Color(0xFF1B1B21),
    surfaceContainerHighest: Color(0xFFF0F0F7),
    error: Color(0xFFBA1A1A),
    onError: Color(0xFFFFFFFF),
  );

  static const ColorScheme presetGreen = ColorScheme(
    brightness: Brightness.light,
    primary: Color(0xFF006D3B),
    onPrimary: Color(0xFFFFFFFF),
    primaryContainer: Color(0xFF93F7B4),
    onPrimaryContainer: Color(0xFF00210E),
    secondary: Color(0xFF4E6355),
    onSecondary: Color(0xFFFFFFFF),
    secondaryContainer: Color(0xFFD1E8D6),
    onSecondaryContainer: Color(0xFF0C1F13),
    surface: Color(0xFFFCFDF7),
    onSurface: Color(0xFF191C1A),
    surfaceContainerHighest: Color(0xFFEDF5EE),
    error: Color(0xFFBA1A1A),
    onError: Color(0xFFFFFFFF),
  );
}

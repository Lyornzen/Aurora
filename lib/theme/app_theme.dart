import 'package:flutter/material.dart';

class AuroraColors {
  static const primary = Color(0xFF7C4DFF);
  static const secondary = Color(0xFFB39DDB);
  static const tertiary = Color(0xFFF8BBD0);
  static const error = Color(0xFFEF5350);
  static const success = Color(0xFF4CAF50);
  static const gradientStart = Color(0xFFC7AFFF);
  static const gradientEnd = Color(0xFFF3C5E7);
  static const darkGradientStart = Color(0xFF3D2A6B);
  static const darkGradientEnd = Color(0xFF4A204B);
}

class AuroraRadius {
  static const small = 12.0;
  static const medium = 20.0;
  static const large = 28.0;
  static const extraLarge = 32.0;
  static const chip = 20.0;
  static const input = 24.0;
}

class AuroraSpacing {
  static const xs = 4.0; static const sm = 8.0; static const md = 12.0;
  static const lg = 16.0; static const xl = 24.0; static const xxl = 32.0;
}

class AppTheme {
  static ThemeData light(Color? seed) => _build(ColorScheme.fromSeed(seedColor: seed ?? AuroraColors.primary, brightness: Brightness.light));
  static ThemeData dark(Color? seed) => _build(ColorScheme.fromSeed(seedColor: seed ?? AuroraColors.primary, brightness: Brightness.dark));

  static ThemeData _build(ColorScheme s) => ThemeData(
    useMaterial3: true, colorScheme: s, brightness: s.brightness, scaffoldBackgroundColor: s.surface,
    appBarTheme: AppBarTheme(centerTitle: false, elevation: 0, scrolledUnderElevation: 1, backgroundColor: s.surface, foregroundColor: s.onSurface, surfaceTintColor: Colors.transparent),
    cardTheme: CardThemeData(elevation: 0, surfaceTintColor: Colors.transparent, color: s.surfaceContainerHigh, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AuroraRadius.medium))),
    inputDecorationTheme: InputDecorationTheme(filled: true, fillColor: s.surfaceContainerHighest, isDense: true, border: OutlineInputBorder(borderRadius: BorderRadius.circular(AuroraRadius.input), borderSide: BorderSide.none), contentPadding: const EdgeInsets.symmetric(horizontal: 18, vertical: 12)),
    navigationBarTheme: NavigationBarThemeData(elevation: 2, surfaceTintColor: Colors.transparent, backgroundColor: s.surface, indicatorColor: s.secondaryContainer ?? s.primary.withAlpha(30), height: 80, labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
      iconTheme: WidgetStateProperty.resolveWith((st) => IconThemeData(color: st.contains(WidgetState.selected) ? s.primary : s.onSurfaceVariant, size: 26))),
    chipTheme: ChipThemeData(shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AuroraRadius.chip)), side: BorderSide.none, padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 2)),
    bottomSheetTheme: const BottomSheetThemeData(shape: RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(AuroraRadius.large))), showDragHandle: true),
    dialogTheme: DialogThemeData(shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AuroraRadius.large))),
    dividerTheme: DividerThemeData(space: 1, thickness: 0.5, color: s.outlineVariant),
    listTileTheme: ListTileThemeData(contentPadding: const EdgeInsets.symmetric(horizontal: AuroraSpacing.lg, vertical: 4), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AuroraRadius.small))),
    floatingActionButtonTheme: FloatingActionButtonThemeData(elevation: 3, backgroundColor: s.primary, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16))),
  );
}

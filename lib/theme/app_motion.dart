import 'package:flutter/material.dart';

/// Animation presets inspired by YumeBox Material Design
class AppMotion {
  // Duration presets
  static const Duration instant = Duration(milliseconds: 120);
  static const Duration fast = Duration(milliseconds: 280);
  static const Duration normal = Duration(milliseconds: 360);
  static const Duration slow = Duration(milliseconds: 500);
  static const Duration pageTransition = Duration(milliseconds: 340);

  // Easing curves
  static const Curve emphasizedDecelerate =
      Cubic(0.05, 0.7, 0.1, 1.0);
  static const Curve emphasizedAccelerate =
      Cubic(0.3, 0.0, 0.8, 0.15);
  static const Curve standard = Curves.fastOutSlowIn;
  static const Curve enter = Curves.easeOutCubic;
  static const Curve exit = Curves.easeInCubic;

  // Press animation
  static final CurveTween pressDown =
      CurveTween(curve: emphasizedAccelerate);
  static final CurveTween pressReturn = CurveTween(
      curve: Curves.elasticOut);

  // Message entrance
  static final CurveTween messageSlide =
      CurveTween(curve: emphasizedDecelerate);
  static const Duration messageDuration = Duration(milliseconds: 320);

  // FAB
  static final CurveTween fabScale =
      CurveTween(curve: emphasizedDecelerate);

  // Sheet
  static const Duration sheetSlideIn = Duration(milliseconds: 340);
  static const Duration sheetSlideOut = Duration(milliseconds: 300);
  static const Duration sheetFadeIn = Duration(milliseconds: 140);
  static const Duration sheetFadeOut = Duration(milliseconds: 140);

  // Card hover
  static final CurveTween cardPress =
      CurveTween(curve: emphasizedAccelerate);
  static const Duration cardPressDuration = Duration(milliseconds: 120);
}

/// Animate a widget with slide-up + fade-in
class SlideFadeTransition extends StatelessWidget {
  final Widget child;
  final Animation<double> animation;

  const SlideFadeTransition({
    super.key,
    required this.child,
    required this.animation,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: animation,
      builder: (context, child) {
        return Opacity(
          opacity: animation.value,
          child: Transform.translate(
            offset: Offset(0, (1 - animation.value) * 20),
            child: child,
          ),
        );
      },
    );
  }
}

/// Staggered list item animation helper
mixin StaggeredItemAnimation<T extends StatefulWidget> on State<T> {
  late final AnimationController _controller;
  late final Animation<double> _animation;
  int _itemCount = 0;

  void initStaggeredAnimation(TickerProvider vsync) {
    _controller = AnimationController(
      duration: AppMotion.messageDuration,
      vsync: vsync,
    );
    _animation = CurvedAnimation(
      parent: _controller,
      curve: AppMotion.emphasizedDecelerate,
    );
  }

  Animation<double> getItemAnimation(int index) {
    if (index >= _itemCount) {
      _itemCount = index + 1;
      _controller.forward(from: 0);
    }
    return Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(
        parent: _controller,
        curve: Interval(
          (index * 0.05).clamp(0.0, 0.8),
          1.0,
          curve: AppMotion.emphasizedDecelerate,
        ),
      ),
    );
  }

  void disposeStaggeredAnimation() {
    _controller.dispose();
  }
}

/// Scale animation on tap — wraps a child with press feedback
class Pressable extends StatefulWidget {
  final Widget child;
  final VoidCallback? onTap;
  final double scaleAmount;

  const Pressable({
    super.key,
    required this.child,
    this.onTap,
    this.scaleAmount = 0.96,
  });

  @override
  State<Pressable> createState() => _PressableState();
}

class _PressableState extends State<Pressable>
    with SingleTickerProviderStateMixin {
  late final AnimationController _ctrl;
  late final Animation<double> _scale;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(
      duration: AppMotion.cardPressDuration,
      vsync: this,
    );
    _scale = Tween<double>(begin: 1, end: widget.scaleAmount).animate(
      CurvedAnimation(parent: _ctrl, curve: AppMotion.emphasizedAccelerate),
    );
    _ctrl.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTapDown: (_) => _ctrl.forward(),
      onTapUp: (_) => _ctrl.reverse(),
      onTapCancel: () => _ctrl.reverse(),
      onTap: widget.onTap,
      child: Transform.scale(
        scale: _scale.value,
        child: widget.child,
      ),
    );
  }
}



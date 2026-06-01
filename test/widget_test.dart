import 'package:flutter_test/flutter_test.dart';

import 'package:openseek/app.dart';

void main() {
  testWidgets('App renders without error', (WidgetTester tester) async {
    await tester.pumpWidget(const OpenSeekApp());
    expect(find.text('OpenSeek'), findsOneWidget);
  });
}

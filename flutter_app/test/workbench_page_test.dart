import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_app/pages/workbench_page.dart';

void main() {
  testWidgets('WorkbenchPage UI Test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MaterialApp(
      home: WorkbenchPage(),
    ));

    // Verify that the app bar title is correct.
    expect(find.text('工作台'), findsOneWidget);

    // Verify that the navigation items are present.
    expect(find.text('订单列表'), findsOneWidget);
    expect(find.text('订单商品统计'), findsOneWidget);
    expect(find.text('账户余额'), findsOneWidget);

    // Verify that the order statistics section is present.
    expect(find.text('今日订单统计'), findsOneWidget);
    expect(find.text('成本'), findsOneWidget);
    expect(find.text('总金额'), findsOneWidget);
    expect(find.text('总数量'), findsOneWidget);
    expect(find.text('原金额'), findsOneWidget);
  });
}

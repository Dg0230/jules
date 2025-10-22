import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_app/pages/order_list_page.dart';

void main() {
  testWidgets('OrderListPage UI Test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MaterialApp(
      home: OrderListPage(),
    ));

    // Verify that the app bar title is correct.
    expect(find.text('订单列表'), findsOneWidget);

    // Verify that the order information is displayed.
    expect(find.text('客户1'), findsOneWidget);
    expect(find.text('订单号: XSDD2023080100001'), findsOneWidget);
    expect(find.text('已付款'), findsOneWidget);

    expect(find.text('客户2'), findsOneWidget);
    expect(find.text('订单号: XSDD2023080100002'), findsOneWidget);
    expect(find.text('已完成'), findsOneWidget);

    expect(find.text('客户3'), findsOneWidget);
    expect(find.text('订单号: XSDD2023080100003'), findsOneWidget);
    expect(find.text('已退款'), findsOneWidget);
  });
}

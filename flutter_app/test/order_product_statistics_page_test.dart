import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_app/pages/order_product_statistics_page.dart';

void main() {
  testWidgets('OrderProductStatisticsPage UI Test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MaterialApp(
      home: OrderProductStatisticsPage(),
    ));

    // Verify that the app bar title is correct.
    expect(find.text('订单商品统计'), findsOneWidget);

    // Verify that the product statistics are displayed.
    expect(find.text('商品A'), findsOneWidget);
    expect(find.text('订单数: 10'), findsOneWidget);

    expect(find.text('商品B'), findsOneWidget);
    expect(find.text('订单数: 5'), findsOneWidget);

    expect(find.text('商品C'), findsOneWidget);
    expect(find.text('订单数: 8'), findsOneWidget);
  });
}

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_app/models/order.dart';
import 'package:flutter_app/pages/order_details_page.dart';

void main() {
  final testOrder = Order(
    customerName: 'Test Customer',
    orderNumber: 'XSDD2023080100001',
    status: '已完成',
    cost: 10.0,
    totalAmount: 20.0,
    quantity: 2,
    originalAmount: 25.0,
  );

  testWidgets('OrderDetailsPage UI Test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(MaterialApp(
      home: OrderDetailsPage(order: testOrder),
    ));

    // Verify that the app bar title is correct.
    expect(find.text('订单详情'), findsOneWidget);

    // Verify that the header displays the correct status and order number.
    expect(find.text('已完成'), findsOneWidget);
    // Verify order number in header
    expect(find.text('订单号: ${testOrder.orderNumber}'), findsOneWidget);
    // Verify order number in details section (as a separate label and value)
    expect(find.text('订单号'), findsOneWidget);
    expect(find.text(testOrder.orderNumber), findsOneWidget);


    // Verify that the amount, payment, and order information is displayed.
    expect(find.text('金额信息'), findsOneWidget);
    expect(find.text('付款信息'), findsOneWidget);
    expect(find.text('订单信息'), findsOneWidget);

    // Verify that the bottom navigation buttons are present.
    expect(find.text('打印'), findsOneWidget);
    expect(find.text('修改'), findsOneWidget);
    expect(find.text('删除'), findsOneWidget);
  });
}

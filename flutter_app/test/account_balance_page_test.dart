import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_app/pages/account_balance_page.dart';

void main() {
  testWidgets('AccountBalancePage UI Test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MaterialApp(
      home: AccountBalancePage(),
    ));

    // Verify that the app bar title is correct.
    expect(find.text('账户余额'), findsOneWidget);

    // Verify that the current balance is displayed.
    expect(find.text('当前余额'), findsOneWidget);
    expect(find.text('¥1000.00'), findsOneWidget);

    // Verify that the transaction headers are present.
    expect(find.text('日期'), findsOneWidget);
    expect(find.text('类型'), findsOneWidget);
    expect(find.text('金额'), findsOneWidget);
    expect(find.text('余额'), findsOneWidget);


    // Verify that the transaction details are displayed.
    expect(find.text('收入'), findsNWidgets(2));
    expect(find.text('支出'), findsOneWidget);
    expect(find.text('500.00'), findsOneWidget);
    expect(find.text('-200.00'), findsOneWidget);
  });
}

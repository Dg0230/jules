import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_app/pages/add_product_page.dart';

void main() {
  testWidgets('AddProductPage UI Test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MaterialApp(
      home: AddProductPage(),
    ));

    // Verify that the app bar title is correct.
    expect(find.text('新增商品'), findsOneWidget);

    // Verify that the text form fields are present.
    expect(find.widgetWithText(TextFormField, '商品名称'), findsOneWidget);
    expect(find.widgetWithText(TextFormField, '商品分类'), findsOneWidget);
    expect(find.widgetWithText(TextFormField, '商品标签'), findsOneWidget);
    expect(find.widgetWithText(TextFormField, '商品价格'), findsOneWidget);
    expect(find.widgetWithText(TextFormField, '商品成本'), findsOneWidget);
    expect(find.widgetWithText(TextFormField, '商品库存'), findsOneWidget);
    expect(find.widgetWithText(TextFormField, '商品排序'), findsOneWidget);
    expect(find.widgetWithText(TextFormField, '商品详情'), findsOneWidget);


    // Verify that the buttons are present.
    expect(find.text('上传图片'), findsOneWidget);
    expect(find.text('添加规格'), findsOneWidget);
    expect(find.text('保存'), findsOneWidget);
  });
}

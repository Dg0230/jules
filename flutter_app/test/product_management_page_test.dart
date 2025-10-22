import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_app/pages/product_management_page.dart';
import 'package:network_image_mock/network_image_mock.dart';

void main() {
  testWidgets('ProductManagementPage UI Test', (WidgetTester tester) async {
    // Mock network images to prevent HTTP errors in tests.
    mockNetworkImagesFor(() async {
      // Build our app and trigger a frame.
      await tester.pumpWidget(const MaterialApp(
        home: ProductManagementPage(),
      ));

      // Verify that the app bar title is correct.
      expect(find.text('商品管理'), findsOneWidget);

      // Verify that the search bar is present.
      expect(find.byType(TextField), findsOneWidget);

      // Verify that the filter buttons are present.
      expect(find.text('全部'), findsOneWidget);
      expect(find.text('已上架'), findsOneWidget);
      expect(find.text('已下架'), findsOneWidget);

      // Verify that the product information is displayed.
      expect(find.text('商品A'), findsOneWidget);
      expect(find.text('成本: ¥10.00'), findsOneWidget);
      expect(find.text('库存: 100'), findsOneWidget);
      expect(find.text('销量: 50'), findsOneWidget);

      // Verify that the bottom action buttons are present.
      expect(find.text('新增商品'), findsOneWidget);
      expect(find.text('导出商品'), findsOneWidget);
    });
  });
}

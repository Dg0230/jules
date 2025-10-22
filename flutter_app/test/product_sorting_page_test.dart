import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_app/pages/product_sorting_page.dart';
import 'package:network_image_mock/network_image_mock.dart';

void main() {
  testWidgets('ProductSortingPage UI Test', (WidgetTester tester) async {
    mockNetworkImagesFor(() async {
      // Build our app and trigger a frame.
      await tester.pumpWidget(const MaterialApp(
        home: ProductSortingPage(),
      ));

      // Verify that the app bar title is correct.
      expect(find.text('商品分拣'), findsOneWidget);

      // Verify that the search bar is present.
      expect(find.byType(TextField), findsOneWidget);

      // Verify that the sorting items are displayed.
      expect(find.text('商品A'), findsOneWidget);
      expect(find.text('订单数量: 1'), findsNWidgets(2));
      expect(find.text('实际数量: 1'), findsOneWidget);
      expect(find.text('库存: 100'), findsOneWidget);
      expect(find.text('正常'), findsNWidgets(2));

      expect(find.text('商品B'), findsOneWidget);
      expect(find.text('实际数量: 0'), findsOneWidget);
      expect(find.text('库存: 0'), findsOneWidget);
      expect(find.text('无货'), findsOneWidget);
    });
  });
}

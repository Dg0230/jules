import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_app/models/sorting_item.dart';
import 'package:flutter_app/pages/sorting_details_page.dart';
import 'package:network_image_mock/network_image_mock.dart';

void main() {
  final testItem = SortingItem(
    imageUrl: 'https://via.placeholder.com/150',
    name: '商品A',
    orderQuantity: 1,
    actualQuantity: 1,
    stock: 100,
    status: '正常',
  );

  testWidgets('SortingDetailsPage UI Test', (WidgetTester tester) async {
    mockNetworkImagesFor(() async {
      // Build our app and trigger a frame.
      await tester.pumpWidget(MaterialApp(
        home: SortingDetailsPage(item: testItem),
      ));

      // Verify that the app bar title is correct.
      expect(find.text('分拣'), findsOneWidget);

      // Verify that the product name is displayed.
      expect(find.text('商品A'), findsOneWidget);

      // Verify that the quantity editor is present.
      expect(find.text('数量'), findsOneWidget);
      expect(find.byType(TextField), findsOneWidget);

      // Verify that the stock information is displayed.
      expect(find.text('库存'), findsOneWidget);
      expect(find.text('100'), findsOneWidget);

      // Verify that the confirm button is present.
      expect(find.text('确认'), findsOneWidget);
    });
  });
}

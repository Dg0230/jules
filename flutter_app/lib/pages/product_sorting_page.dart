import 'package:flutter/material.dart';
import 'package:flutter_app/models/sorting_item.dart';
import 'package:flutter_app/pages/sorting_details_page.dart';

class ProductSortingPage extends StatefulWidget {
  const ProductSortingPage({super.key});

  @override
  State<ProductSortingPage> createState() => _ProductSortingPageState();
}

class _ProductSortingPageState extends State<ProductSortingPage> {
  final List<SortingItem> _sortingItems = [
    SortingItem(
      imageUrl: 'https://via.placeholder.com/150',
      name: '商品A',
      orderQuantity: 1,
      actualQuantity: 1,
      stock: 100,
      status: '正常',
    ),
    SortingItem(
      imageUrl: 'https://via.placeholder.com/150',
      name: '商品B',
      orderQuantity: 1,
      actualQuantity: 0,
      stock: 0,
      status: '无货',
    ),
    SortingItem(
      imageUrl: 'https://via.placeholder.com/150',
      name: '商品C',
      orderQuantity: 2,
      actualQuantity: 2,
      stock: 50,
      status: '正常',
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('商品分拣'),
        backgroundColor: Colors.white,
        elevation: 0,
        titleTextStyle: const TextStyle(
          color: Colors.black,
          fontSize: 18,
          fontWeight: FontWeight.bold,
        ),
        iconTheme: const IconThemeData(color: Colors.black),
      ),
      body: Column(
        children: [
          _buildSearchBar(),
          Expanded(
            child: ListView.builder(
              itemCount: _sortingItems.length,
              itemBuilder: (context, index) {
                final item = _sortingItems[index];
                return GestureDetector(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => SortingDetailsPage(item: item),
                      ),
                    );
                  },
                  child: Card(
                    margin: const EdgeInsets.symmetric(
                        horizontal: 10, vertical: 5),
                    child: Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: Row(
                        children: [
                          Image.network(
                            item.imageUrl,
                            width: 80,
                            height: 80,
                            fit: BoxFit.cover,
                          ),
                          const SizedBox(width: 10),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  item.name,
                                  style: const TextStyle(
                                    fontSize: 16,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                const SizedBox(height: 5),
                                Text('订单数量: ${item.orderQuantity}'),
                                const SizedBox(height: 5),
                                Text('实际数量: ${item.actualQuantity}'),
                                const SizedBox(height: 5),
                                Text('库存: ${item.stock}'),
                              ],
                            ),
                          ),
                          Text(
                            item.status,
                            style: TextStyle(
                              color: item.status == '正常'
                                  ? Colors.green
                                  : Colors.red,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSearchBar() {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: TextField(
        decoration: InputDecoration(
          hintText: '搜索商品',
          prefixIcon: const Icon(Icons.search),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(10),
            borderSide: BorderSide.none,
          ),
          filled: true,
          fillColor: Colors.grey[200],
        ),
      ),
    );
  }
}

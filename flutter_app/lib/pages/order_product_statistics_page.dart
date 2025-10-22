import 'package:flutter/material.dart';
import 'package:flutter_app/models/product_statistic.dart';

class OrderProductStatisticsPage extends StatefulWidget {
  const OrderProductStatisticsPage({super.key});

  @override
  State<OrderProductStatisticsPage> createState() =>
      _OrderProductStatisticsPageState();
}

class _OrderProductStatisticsPageState
    extends State<OrderProductStatisticsPage> {
  final List<ProductStatistic> _statistics = [
    ProductStatistic(productName: '商品A', orderCount: 10),
    ProductStatistic(productName: '商品B', orderCount: 5),
    ProductStatistic(productName: '商品C', orderCount: 8),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('订单商品统计'),
        backgroundColor: Colors.white,
        elevation: 0,
        titleTextStyle: const TextStyle(
          color: Colors.black,
          fontSize: 18,
          fontWeight: FontWeight.bold,
        ),
        iconTheme: const IconThemeData(color: Colors.black),
      ),
      body: ListView.builder(
        itemCount: _statistics.length,
        itemBuilder: (context, index) {
          final statistic = _statistics[index];
          return Card(
            margin: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
            child: ListTile(
              title: Text(statistic.productName),
              trailing: Text('订单数: ${statistic.orderCount}'),
            ),
          );
        },
      ),
    );
  }
}

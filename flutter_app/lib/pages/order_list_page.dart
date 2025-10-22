import 'package:flutter/material.dart';
import 'package:flutter_app/models/order.dart';
import 'package:flutter_app/pages/order_details_page.dart';

class OrderListPage extends StatefulWidget {
  const OrderListPage({super.key});

  @override
  State<OrderListPage> createState() => _OrderListPageState();
}

class _OrderListPageState extends State<OrderListPage> {
  final List<Order> _orders = [
    Order(
      customerName: '客户1',
      orderNumber: 'XSDD2023080100001',
      status: '已付款',
      cost: 8.00,
      totalAmount: 0.00,
      quantity: 1,
      originalAmount: 0.00,
    ),
    Order(
      customerName: '客户2',
      orderNumber: 'XSDD2023080100002',
      status: '已完成',
      cost: 12.00,
      totalAmount: 20.00,
      quantity: 2,
      originalAmount: 20.00,
    ),
    Order(
      customerName: '客户3',
      orderNumber: 'XSDD2023080100003',
      status: '已退款',
      cost: 10.00,
      totalAmount: 15.00,
      quantity: 1,
      originalAmount: 15.00,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('订单列表'),
        backgroundColor: Colors.white,
        elevation: 0,
        titleTextStyle: const TextStyle(
          color: Colors.black,
          fontSize: 18,
          fontWeight: FontWeight.bold,
        ),
      ),
      body: ListView.builder(
        itemCount: _orders.length,
        itemBuilder: (context, index) {
          final order = _orders[index];
          return GestureDetector(
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => OrderDetailsPage(order: order),
                ),
              );
            },
            child: Card(
              margin: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          order.customerName,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Text(
                          order.status,
                          style: TextStyle(
                            color: _getStatusColor(order.status),
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),
                    Text('订单号: ${order.orderNumber}'),
                    const SizedBox(height: 10),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        _buildInfoColumn(
                            '成本', '¥${order.cost.toStringAsFixed(2)}'),
                        _buildInfoColumn(
                            '总金额', '¥${order.totalAmount.toStringAsFixed(2)}'),
                        _buildInfoColumn('总数量', order.quantity.toString()),
                        _buildInfoColumn(
                            '原金额', '¥${order.originalAmount.toStringAsFixed(2)}'),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildInfoColumn(String label, String value) {
    return Column(
      children: [
        Text(value, style: const TextStyle(fontWeight: FontWeight.bold)),
        const SizedBox(height: 5),
        Text(label, style: const TextStyle(color: Colors.grey)),
      ],
    );
  }

  Color _getStatusColor(String status) {
    switch (status) {
      case '已付款':
        return Colors.green;
      case '已完成':
        return Colors.blue;
      case '已退款':
        return Colors.red;
      default:
        return Colors.black;
    }
  }
}

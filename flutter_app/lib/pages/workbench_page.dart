import 'package:flutter/material.dart';
import 'package:flutter_app/pages/order_list_page.dart';
import 'package:flutter_app/pages/order_product_statistics_page.dart';
import 'package:flutter_app/pages/account_balance_page.dart';

class WorkbenchPage extends StatelessWidget {
  const WorkbenchPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('工作台'),
        backgroundColor: Colors.white,
        elevation: 0,
        titleTextStyle: const TextStyle(
          color: Colors.black,
          fontSize: 18,
          fontWeight: FontWeight.bold,
        ),
      ),
      body: Container(
        color: Colors.grey[200],
        child: Column(
          children: [
            _buildNavigationGrid(context),
            const SizedBox(height: 10),
            _buildOrderStatistics(context),
          ],
        ),
      ),
    );
  }

  Widget _buildNavigationGrid(BuildContext context) {
    return Container(
      color: Colors.white,
      padding: const EdgeInsets.symmetric(vertical: 20.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          GestureDetector(
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const OrderListPage()),
              );
            },
            child: _buildNavigationItem(context, Icons.list_alt, '订单列表'),
          ),
          GestureDetector(
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                    builder: (context) => const OrderProductStatisticsPage()),
              );
            },
            child: _buildNavigationItem(context, Icons.bar_chart, '订单商品统计'),
          ),
          GestureDetector(
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                    builder: (context) => const AccountBalancePage()),
              );
            },
            child: _buildNavigationItem(
                context, Icons.account_balance_wallet, '账户余额'),
          ),
        ],
      ),
    );
  }

  Widget _buildNavigationItem(
      BuildContext context, IconData icon, String label) {
    return Column(
      children: [
        Icon(icon, size: 40, color: Theme.of(context).primaryColor),
        const SizedBox(height: 8),
        Text(label),
      ],
    );
  }

  Widget _buildOrderStatistics(BuildContext context) {
    return Container(
      color: Colors.white,
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '今日订单统计',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildStatisticItem(context, '成本', '¥ 8.00'),
              _buildStatisticItem(context, '总金额', '¥ 0.00'),
              _buildStatisticItem(context, '总数量', '1'),
              _buildStatisticItem(context, '原金额', '¥ 0.00'),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatisticItem(
      BuildContext context, String label, String value) {
    return Column(
      children: [
        Text(
          value,
          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 8),
        Text(label, style: TextStyle(color: Colors.grey[600])),
      ],
    );
  }
}

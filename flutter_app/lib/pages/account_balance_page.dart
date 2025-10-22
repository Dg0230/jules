import 'package:flutter/material.dart';
import 'package:flutter_app/models/account_transaction.dart';

class AccountBalancePage extends StatefulWidget {
  const AccountBalancePage({super.key});

  @override
  State<AccountBalancePage> createState() => _AccountBalancePageState();
}

class _AccountBalancePageState extends State<AccountBalancePage> {
  final double _currentBalance = 1000.00;
  final List<AccountTransaction> _transactions = [
    AccountTransaction(
      date: '2023-08-01',
      type: '收入',
      amount: 500.00,
      balance: 1500.00,
    ),
    AccountTransaction(
      date: '2023-08-02',
      type: '支出',
      amount: -200.00,
      balance: 1300.00,
    ),
    AccountTransaction(
      date: '2023-08-03',
      type: '收入',
      amount: 100.00,
      balance: 1400.00,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('账户余额'),
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
          _buildBalanceHeader(),
          _buildTransactionHeader(),
          Expanded(
            child: ListView.builder(
              itemCount: _transactions.length,
              itemBuilder: (context, index) {
                final transaction = _transactions[index];
                return ListTile(
                  title: Text(transaction.type),
                  subtitle: Text(transaction.date),
                  trailing: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text(
                        '${transaction.amount.toStringAsFixed(2)}',
                        style: TextStyle(
                          color: transaction.amount > 0 ? Colors.green : Colors.red,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text('余额: ${transaction.balance.toStringAsFixed(2)}'),
                    ],
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBalanceHeader() {
    return Container(
      padding: const EdgeInsets.all(16.0),
      color: Colors.blue,
      child: Center(
        child: Column(
          children: [
            const Text(
              '当前余额',
              style: TextStyle(color: Colors.white, fontSize: 16),
            ),
            const SizedBox(height: 10),
            Text(
              '¥${_currentBalance.toStringAsFixed(2)}',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 32,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTransactionHeader() {
    return Container(
      padding: const EdgeInsets.all(16.0),
      color: Colors.grey[200],
      child: const Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text('日期', style: TextStyle(fontWeight: FontWeight.bold)),
          Text('类型', style: TextStyle(fontWeight: FontWeight.bold)),
          Text('金额', style: TextStyle(fontWeight: FontWeight.bold)),
          Text('余额', style: TextStyle(fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}

class Order {
  final String customerName;
  final String orderNumber;
  final String status;
  final double cost;
  final double totalAmount;
  final int quantity;
  final double originalAmount;

  Order({
    required this.customerName,
    required this.orderNumber,
    required this.status,
    required this.cost,
    required this.totalAmount,
    required this.quantity,
    required this.originalAmount,
  });
}

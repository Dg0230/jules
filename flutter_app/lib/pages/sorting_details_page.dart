import 'package:flutter/material.dart';
import 'package:flutter_app/models/sorting_item.dart';

class SortingDetailsPage extends StatefulWidget {
  final SortingItem item;

  const SortingDetailsPage({super.key, required this.item});

  @override
  State<SortingDetailsPage> createState() => _SortingDetailsPageState();
}

class _SortingDetailsPageState extends State<SortingDetailsPage> {
  late TextEditingController _quantityController;

  @override
  void initState() {
    super.initState();
    _quantityController =
        TextEditingController(text: widget.item.actualQuantity.toString());
  }

  @override
  void dispose() {
    _quantityController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('分拣'),
        backgroundColor: Colors.white,
        elevation: 0,
        titleTextStyle: const TextStyle(
          color: Colors.black,
          fontSize: 18,
          fontWeight: FontWeight.bold,
        ),
        iconTheme: const IconThemeData(color: Colors.black),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildProductInfo(),
            const SizedBox(height: 20),
            _buildQuantityEditor(),
            const SizedBox(height: 20),
            _buildStockInfo(),
          ],
        ),
      ),
      bottomNavigationBar: Padding(
        padding: const EdgeInsets.all(8.0),
        child: ElevatedButton(
          onPressed: () {
            // Handle save
          },
          child: const Text('确认'),
        ),
      ),
    );
  }

  Widget _buildProductInfo() {
    return Row(
      children: [
        Image.network(
          widget.item.imageUrl,
          width: 100,
          height: 100,
          fit: BoxFit.cover,
        ),
        const SizedBox(width: 16),
        Expanded(
          child: Text(
            widget.item.name,
            style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
        ),
      ],
    );
  }

  Widget _buildQuantityEditor() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text('数量', style: TextStyle(fontSize: 16)),
            Row(
              children: [
                IconButton(
                  icon: const Icon(Icons.remove_circle),
                  onPressed: () {
                    setState(() {
                      int currentValue = int.parse(_quantityController.text);
                      if (currentValue > 0) {
                        _quantityController.text = (currentValue - 1).toString();
                      }
                    });
                  },
                ),
                SizedBox(
                  width: 50,
                  child: TextField(
                    controller: _quantityController,
                    textAlign: TextAlign.center,
                    keyboardType: TextInputType.number,
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.add_circle),
                  onPressed: () {
                    setState(() {
                      int currentValue = int.parse(_quantityController.text);
                      _quantityController.text = (currentValue + 1).toString();
                    });
                  },
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStockInfo() {
    return Card(
        child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('库存', style: TextStyle(fontSize: 16)),
                Text(
                  '${widget.item.stock}',
                  style: const TextStyle(
                      fontSize: 16, fontWeight: FontWeight.bold),
                ),
              ],
            )));
  }
}

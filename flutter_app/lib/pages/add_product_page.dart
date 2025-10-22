import 'package:flutter/material.dart';

class AddProductPage extends StatefulWidget {
  const AddProductPage({super.key});

  @override
  State<AddProductPage> createState() => _AddProductPageState();
}

class _AddProductPageState extends State<AddProductPage> {
  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('新增商品'),
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
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildTextField(label: '商品名称'),
              _buildTextField(label: '商品分类'),
              _buildTextField(label: '商品标签'),
              _buildTextField(label: '商品价格', keyboardType: TextInputType.number),
              _buildTextField(label: '商品成本', keyboardType: TextInputType.number),
              _buildTextField(label: '商品库存', keyboardType: TextInputType.number),
              _buildTextField(label: '商品排序', keyboardType: TextInputType.number),
              const SizedBox(height: 20),
              const Text('商品图片', style: TextStyle(fontWeight: FontWeight.bold)),
              const SizedBox(height: 10),
              ElevatedButton.icon(
                onPressed: () {
                  // Handle image upload
                },
                icon: const Icon(Icons.add_a_photo),
                label: const Text('上传图片'),
              ),
              const SizedBox(height: 20),
              _buildTextField(label: '商品详情', maxLines: 5),
              const SizedBox(height: 20),
              const Text('规格', style: TextStyle(fontWeight: FontWeight.bold)),
              const SizedBox(height: 10),
              ElevatedButton.icon(
                onPressed: () {
                  // Handle specifications
                },
                icon: const Icon(Icons.add),
                label: const Text('添加规格'),
              ),
            ],
          ),
        ),
      ),
      bottomNavigationBar: Padding(
        padding: const EdgeInsets.all(8.0),
        child: ElevatedButton(
          onPressed: () {
            if (_formKey.currentState!.validate()) {
              // Handle form submission
            }
          },
          child: const Text('保存'),
        ),
      ),
    );
  }

  Widget _buildTextField({
    required String label,
    int maxLines = 1,
    TextInputType keyboardType = TextInputType.text,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: TextFormField(
        maxLines: maxLines,
        keyboardType: keyboardType,
        decoration: InputDecoration(
          labelText: label,
          border: const OutlineInputBorder(),
        ),
        validator: (value) {
          if (value == null || value.isEmpty) {
            return '请输入$label';
          }
          return null;
        },
      ),
    );
  }
}

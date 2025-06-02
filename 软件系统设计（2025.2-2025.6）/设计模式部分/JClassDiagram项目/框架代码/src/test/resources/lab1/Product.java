// 接口1：库存管理功能
interface InventoryManageable {
    void updateStock(int quantity);  // 更新库存数量
    double calculateInventoryValue();  // 计算库存总价值
}

// 接口2：商品信息展示功能
interface Displayable {
    String getBasicInfo();  // 获取基本信息
    String getFullDetails();  // 获取完整详情
}

// 基础商品类（实现两个接口）
class Product implements InventoryManageable, Displayable {
    // 属性严格限定类型
    private int productId;
    private String productName;
    private double price;
    private int stockQuantity;  // 库存数量

    public Product(int id, String name, double price, int stock) {
        this.productId = id;
        this.productName = name;
        this.price = price;
        this.stockQuantity = stock;
    }

    // 实现接口1的方法
    @Override
    public void updateStock(int quantity) {
        this.stockQuantity += quantity;
    }

    @Override
    public double calculateInventoryValue() {
        return this.stockQuantity * this.price;
    }

    // 实现接口2的方法
    @Override
    public String getBasicInfo() {
        return "ID: " + productId + " | 名称: " + productName;
    }

    @Override
    public String getFullDetails() {
        return getBasicInfo() + "\n价格: ¥" + price 
             + "\n库存: " + stockQuantity + "件";
    }
}

// 折扣商品扩展类
class DiscountedProduct extends Product {
    private double discountRate;  // 折扣率

    public DiscountedProduct(int id, String name, double price, 
                            int stock, double discount) {
        super(id, name, price, stock);
        this.discountRate = discount;
    }

    // 重写库存价值计算方法
    @Override
    public double calculateInventoryValue() {
        return super.calculateInventoryValue() * (1 - discountRate);
    }

    // 扩展显示方法
    @Override
    public String getFullDetails() {
        return super.getFullDetails() 
             + "\n折扣率: " + (discountRate*100) + "%";
    }
}
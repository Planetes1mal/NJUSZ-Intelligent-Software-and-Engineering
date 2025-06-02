// 订单状态枚举
enum OrderStatus {
    PENDING("待处理"),
    PROCESSING("处理中"),
    COMPLETED("已完成");

    private final String description;

    OrderStatus(String desc) {
        this.description = desc;
    }

    public String getStatusInfo() {
        return name() + ":" + description;
    }
}

// 抽象泛型处理器
abstract class OrderProcessor<T extends Number> {
    protected OrderStatus currentStatus = OrderStatus.PENDING;

    // 抽象泛型方法
    public abstract void validateOrder(T orderId);

    // 泛型具体方法
    protected <E extends Exception> void logError(E exception) {
        System.out.println("错误日志: " + exception.getMessage());
    }

    // 状态操作方法
    public void updateStatus(OrderStatus newStatus) {
        this.currentStatus = newStatus;
    }
}

// 具体实现类
class DigitalOrderProcessor extends OrderProcessor<Long> {
    @Override
    public void validateOrder(Long orderId) {
        if (orderId <= 0) {
            logError(new IllegalArgumentException("非法订单ID"));
            return;
        }
        updateStatus(OrderStatus.PROCESSING);
    }

    // 泛型方法扩展
    public <V extends CharSequence> String formatReceipt(V receiptTemplate) {
        return receiptTemplate.toString().replace("#ID#", String.valueOf(currentStatus));
    }
}
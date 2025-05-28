// OrderStatus.java
public enum OrderStatus {
    // 枚举常量
    NEW(100, "新订单"),
    PROCESSING(200, "处理中"),
    SHIPPED(300, "已发货"),
    DELIVERED(400, "已送达"),
    CANCELLED(500, "已取消");

    // 枚举字段
    private final int statusCode;
    private final String description;

    // 枚举构造方法（自动private）
    OrderStatus(int code, String desc) {
        this.statusCode = code;
        this.description = desc;
    }

    // 实例方法
    public String getStatusInfo() {
        return statusCode + ":" + description;
    }

    // 静态方法
    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.statusCode == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效状态码: " + code);
    }
}
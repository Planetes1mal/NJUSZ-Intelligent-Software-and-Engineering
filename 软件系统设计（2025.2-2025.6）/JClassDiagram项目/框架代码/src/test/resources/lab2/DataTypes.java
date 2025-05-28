import java.util.*;

public class DataTypesExample {
    // 基本类型
    private int intVal;
    private double doubleVal;
    private float floatVal;
    private long longVal;
    private short shortVal;
    private byte byteVal;
    private boolean boolVal;
    private char charVal;

    // 包装类型
    private Integer integerObj;
    private Double doubleObj;
    private Float floatObj;
    private Long longObj;
    private Short shortObj;
    private Byte byteObj;
    private Boolean boolObj;
    private Character charObj;

    // 集合类型
    private List<String> stringList;
    private Set<Integer> intSet;
    private Map<String, Object> objectMap;

    // String类型
    private String text;

    // 方法包含各种参数和返回类型
    public Map<Long, Double> processNumbers(int a, Double b) {
        return new HashMap<>();
    }

    public List<Character> convertChars(byte[] data) {
        return new ArrayList<>();
    }

    public boolean validate(Short s, Boolean flag) {
        return flag;
    }
}
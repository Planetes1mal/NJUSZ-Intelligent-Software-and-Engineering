// GenericDemo.java
import java.util.*;

// 类级泛型（多参数）
public class GenericContainer<K, V> {
    private final Map<K, V> baseMap = new HashMap<>();

    // 方法级泛型（多参数）
    public <E extends Number> void addNumbericEntry(K key, E number) {
        baseMap.put(key, (V) number.toString());
    }

    // 参数级泛型（集合类型）
    public void mergeEntries(Map<? extends K, ? super V> inputMap) {
        baseMap.putAll(inputMap);
    }

    // 嵌套泛型方法
    public static <T, U> List<Pair<T, U>> zipLists(List<T> list1, List<U> list2) {
        List<Pair<T, U>> pairs = new ArrayList<>();
        int minSize = Math.min(list1.size(), list2.size());
        for (int i = 0; i < minSize; i++) {
            pairs.add(new Pair<>(list1.get(i), list2.get(i)));
        }
        return pairs;
    }
}

// 泛型辅助类
class Pair<T, U> {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    // 多参数泛型方法
    public <V extends Comparable<V>> V getMax(V a, V b) {
        return a.compareTo(b) > 0 ? a : b;
    }
}
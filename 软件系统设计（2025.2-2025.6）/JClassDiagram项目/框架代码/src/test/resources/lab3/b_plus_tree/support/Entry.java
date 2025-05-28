// Entry.java
package com.datastructure.bptree.support;

public class Entry<K extends Comparable<K>, V> implements Comparable<Entry<K, V>> {
    private final K key;
    private V value;

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(Entry<K, V> o) {
        return this.key.compareTo(o.key);
    }

    // Getters...
}
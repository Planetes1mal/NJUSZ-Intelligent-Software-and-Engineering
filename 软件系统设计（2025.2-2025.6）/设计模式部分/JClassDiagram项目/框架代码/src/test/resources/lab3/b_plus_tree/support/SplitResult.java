package com.datastructure.bptree.support;

public class SplitResult<K extends Comparable<K>, V> {
    private final K midKey;
    private final Node<K, V> newNode;

    public SplitResult(K midKey, Node<K, V> newNode) {
        this.midKey = midKey;
        this.newNode = newNode;
    }

    // Getters...
}
// Node.java
package com.datastructure.bptree.core;

import com.datastructure.bptree.support.Entry;
import com.datastructure.bptree.support.SplitResult;

public abstract class Node<K extends Comparable<K>, V> {
    protected final int order;
    protected final Entry<K, V>[] entries;
    protected int size;

    @SuppressWarnings("unchecked")
    public Node(int order) {
        this.order = order;
        this.entries = (Entry<K, V>[]) new Entry[order];
    }

    public abstract SplitResult<K, V> insert(K key, V value);
    public abstract V search(K key);
    public abstract boolean isLeaf();
}
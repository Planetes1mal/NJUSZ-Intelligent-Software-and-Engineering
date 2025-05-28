// InternalNode.java
package com.datastructure.bptree.nodes;

import com.datastructure.bptree.core.Node;
import com.datastructure.bptree.support.Entry;
import com.datastructure.bptree.support.SplitResult;

public class InternalNode<K extends Comparable<K>, V> extends Node<K, V> {
    private Node<K, V>[] children;

    @SuppressWarnings("unchecked")
    public InternalNode(int order) {
        super(order);
        this.children = (Node<K, V>[]) new Node[order + 1];
    }

    @Override
    public SplitResult<K, V> insert(K key, V value) {
        // 插入逻辑实现...
        return null;
    }

    public void insertEntry(K key, Node<K, V> child) {
        // 插入条目实现...
    }

    @Override
    public V search(K key) {
        int index = findChildIndex(key);
        return children[index].search(key);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    private int findChildIndex(K key) {
        // 二分查找实现...
        return 0;
    }
}
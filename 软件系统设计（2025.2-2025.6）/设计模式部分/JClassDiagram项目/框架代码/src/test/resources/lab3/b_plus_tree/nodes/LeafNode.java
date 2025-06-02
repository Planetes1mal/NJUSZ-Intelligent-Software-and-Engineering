// LeafNode.java
package com.datastructure.bptree.nodes;

import com.datastructure.bptree.core.Node;
import com.datastructure.bptree.features.SiblingLink;
import com.datastructure.bptree.support.Entry;
import com.datastructure.bptree.support.SplitResult;

public class LeafNode<K extends Comparable<K>, V> extends Node<K, V> implements SiblingLink<K, V> {
    private LeafNode<K, V> next;
    private LeafNode<K, V> prev;

    public LeafNode(int order) {
        super(order);
    }

    @Override
    public SplitResult<K, V> insert(K key, V value) {
        // 叶子节点插入逻辑...
        return null;
    }

    @Override
    public V search(K key) {
        // 二分查找实现...
        return null;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    // 实现SiblingLink接口
    @Override
    public LeafNode<K, V> getNext() {
        return next;
    }

    @Override
    public void setNext(LeafNode<K, V> next) {
        this.next = next;
    }

    @Override
    public LeafNode<K, V> getPrev() {
        return prev;
    }

    @Override
    public void setPrev(LeafNode<K, V> prev) {
        this.prev = prev;
    }
}
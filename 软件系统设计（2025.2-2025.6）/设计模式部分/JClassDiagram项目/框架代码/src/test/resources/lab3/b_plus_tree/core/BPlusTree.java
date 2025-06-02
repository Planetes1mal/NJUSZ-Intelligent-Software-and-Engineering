// BPlusTree.java
package com.datastructure.bptree.core;

import com.datastructure.bptree.nodes.Node;
import com.datastructure.bptree.support.Entry;
import com.datastructure.bptree.support.SplitResult;

public class BPlusTree<K extends Comparable<K>, V> {
    private Node<K, V> root;
    private final int order;

    public BPlusTree(int order) {
        this.order = order;
        this.root = new LeafNode<>(order);
    }

    public void insert(K key, V value) {
        SplitResult<K, V> result = root.insert(key, value);
        if (result != null) {
            InternalNode<K, V> newRoot = new InternalNode<>(order);
            newRoot.insertEntry(result.getMidKey(), root);
            newRoot.insertEntry(result.getMidKey(), result.getNewNode());
            root = newRoot;
        }
    }

    public V search(K key) {
        return root.search(key);
    }
}
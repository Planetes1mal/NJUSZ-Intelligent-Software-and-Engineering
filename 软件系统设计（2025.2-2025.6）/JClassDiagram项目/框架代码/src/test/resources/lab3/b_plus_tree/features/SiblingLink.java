// SiblingLink.java
package com.datastructure.bptree.features;

public interface SiblingLink<K extends Comparable<K>, V> {
    LeafNode<K, V> getNext();
    void setNext(LeafNode<K, V> next);
    LeafNode<K, V> getPrev();
    void setPrev(LeafNode<K, V> prev);
}
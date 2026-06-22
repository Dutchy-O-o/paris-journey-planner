package com.journeyplanner.graph.adt;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A dictionary implemented as a singly linked chain of nodes.
 * Ported verbatim from the original coursework (Carrano-style ADT) so the
 * graph keeps relying on a hand-written data structure rather than the JDK map.
 */
public class Dictionary<K, V> implements DictionaryInterface<K, V> {
   private Node firstNode;        // Reference to first node of chain
   private int numberOfEntries;

   public Dictionary() {
      initializeDataFields();
   }

   @Override
   public V add(K key, V value) {
      if ((key == null) || (value == null)) {
         throw new IllegalArgumentException("Cannot add null to a dictionary.");
      }
      V result = null;

      // Search chain for a node containing key
      Node currentNode = firstNode;
      while ((currentNode != null) && !key.equals(currentNode.getKey())) {
         currentNode = currentNode.getNextNode();
      }

      if (currentNode == null) {
         // Key not in dictionary; add new node at beginning of chain
         Node newNode = new Node(key, value);
         newNode.setNextNode(firstNode);
         firstNode = newNode;
         numberOfEntries++;
      } else {
         // Key in dictionary; replace corresponding value
         result = currentNode.getValue();
         currentNode.setValue(value);
      }
      return result;
   }

   @Override
   public V remove(K key) {
      V result = null;

      if (!isEmpty()) {
         // Search chain for a node containing key; save reference to preceding node
         Node currentNode = firstNode;
         Node nodeBefore = null;

         while ((currentNode != null) && !key.equals(currentNode.getKey())) {
            nodeBefore = currentNode;
            currentNode = currentNode.getNextNode();
         }

         if (currentNode != null) {
            Node nodeAfter = currentNode.getNextNode();
            if (nodeBefore == null) {
               firstNode = nodeAfter;
            } else {
               nodeBefore.setNextNode(nodeAfter);
            }
            result = currentNode.getValue();
            numberOfEntries--;
         }
      }
      return result;
   }

   @Override
   public V getValue(K key) {
      Node currentNode = firstNode;
      while ((currentNode != null) && !key.equals(currentNode.getKey())) {
         currentNode = currentNode.getNextNode();
      }
      return (currentNode != null) ? currentNode.getValue() : null;
   }

   @Override
   public boolean contains(K key) {
      return getValue(key) != null;
   }

   @Override
   public boolean isEmpty() {
      return numberOfEntries == 0;
   }

   @Override
   public int getSize() {
      return numberOfEntries;
   }

   @Override
   public final void clear() {
      initializeDataFields();
   }

   @Override
   public Iterator<K> getKeyIterator() {
      return new KeyIterator();
   }

   @Override
   public Iterator<V> getValueIterator() {
      return new ValueIterator();
   }

   private void initializeDataFields() {
      firstNode = null;
      numberOfEntries = 0;
   }

   private class KeyIterator implements Iterator<K> {
      private Node nextNode = firstNode;

      @Override
      public boolean hasNext() {
         return nextNode != null;
      }

      @Override
      public K next() {
         if (!hasNext()) {
            throw new NoSuchElementException();
         }
         K result = nextNode.getKey();
         nextNode = nextNode.getNextNode();
         return result;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private class ValueIterator implements Iterator<V> {
      private Node nextNode = firstNode;

      @Override
      public boolean hasNext() {
         return nextNode != null;
      }

      @Override
      public V next() {
         if (!hasNext()) {
            throw new NoSuchElementException();
         }
         V result = nextNode.getValue();
         nextNode = nextNode.getNextNode();
         return result;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private class Node {
      private final K key;
      private V value;
      private Node next;

      private Node(K searchKey, V dataValue) {
         key = searchKey;
         value = dataValue;
         next = null;
      }

      private K getKey() {
         return key;
      }

      private V getValue() {
         return value;
      }

      private void setValue(V newValue) {
         value = newValue;
      }

      private Node getNextNode() {
         return next;
      }

      private void setNextNode(Node nextNode) {
         next = nextNode;
      }
   }
}

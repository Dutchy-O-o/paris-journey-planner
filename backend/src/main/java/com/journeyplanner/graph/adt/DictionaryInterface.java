package com.journeyplanner.graph.adt;

import java.util.Iterator;

/**
 * An interface for a dictionary (key/value map) backed by a custom chain of nodes.
 * Kept from the original data-structures coursework so the project keeps using its
 * own ADT instead of {@link java.util.HashMap}.
 */
public interface DictionaryInterface<K, V> {
   /** Adds a new entry. If the key already exists, replaces the value.
       @return the previous value if one was replaced, otherwise null. */
   V add(K key, V value);

   /** Removes a specific entry.
       @return the value that was associated with the key, or null. */
   V remove(K key);

   /** Retrieves the value associated with a key, or null if absent. */
   V getValue(K key);

   /** @return true if the key is associated with an entry. */
   boolean contains(K key);

   /** @return an iterator over all keys. */
   Iterator<K> getKeyIterator();

   /** @return an iterator over all values. */
   Iterator<V> getValueIterator();

   /** @return true if the dictionary is empty. */
   boolean isEmpty();

   /** @return the number of entries. */
   int getSize();

   /** Removes all entries. */
   void clear();
}

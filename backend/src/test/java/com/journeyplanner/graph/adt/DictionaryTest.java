package com.journeyplanner.graph.adt;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DictionaryTest {

   @Test
   void addStoresAndRetrievesValues() {
      Dictionary<String, Integer> dict = new Dictionary<>();
      assertNull(dict.add("a", 1));
      assertNull(dict.add("b", 2));
      assertEquals(1, dict.getValue("a"));
      assertEquals(2, dict.getValue("b"));
      assertEquals(2, dict.getSize());
   }

   @Test
   void addReplacesExistingValueAndReturnsOld() {
      Dictionary<String, Integer> dict = new Dictionary<>();
      dict.add("a", 1);
      assertEquals(1, dict.add("a", 99));
      assertEquals(99, dict.getValue("a"));
      assertEquals(1, dict.getSize(), "Replacing must not grow the dictionary");
   }

   @Test
   void removeDeletesEntry() {
      Dictionary<String, Integer> dict = new Dictionary<>();
      dict.add("a", 1);
      dict.add("b", 2);
      assertEquals(1, dict.remove("a"));
      assertFalse(dict.contains("a"));
      assertNull(dict.remove("missing"));
      assertEquals(1, dict.getSize());
   }

   @Test
   void rejectsNullKeyOrValue() {
      Dictionary<String, Integer> dict = new Dictionary<>();
      assertThrows(IllegalArgumentException.class, () -> dict.add(null, 1));
      assertThrows(IllegalArgumentException.class, () -> dict.add("a", null));
   }

   @Test
   void iteratorsCoverAllEntries() {
      Dictionary<String, Integer> dict = new Dictionary<>();
      dict.add("a", 1);
      dict.add("b", 2);
      dict.add("c", 3);

      List<String> keys = new ArrayList<>();
      Iterator<String> ki = dict.getKeyIterator();
      while (ki.hasNext()) keys.add(ki.next());

      int sum = 0;
      Iterator<Integer> vi = dict.getValueIterator();
      while (vi.hasNext()) sum += vi.next();

      assertEquals(3, keys.size());
      assertTrue(keys.containsAll(List.of("a", "b", "c")));
      assertEquals(6, sum);
   }
}

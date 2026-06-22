package com.journeyplanner.graph;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DirectedGraphTest {

   /** A->B (1), B->C (1), A->C (5): the cheapest A→C goes via B (cost 2). */
   private DirectedGraph<String> sampleGraph() {
      DirectedGraph<String> g = new DirectedGraph<>();
      for (String v : List.of("A", "B", "C", "D")) {
         g.addVertex(v);
      }
      g.addEdge("A", "B", 1);
      g.addEdge("B", "C", 1);
      g.addEdge("A", "C", 5);
      return g;
   }

   private List<String> pathTo(VertexInterface<String> end) {
      List<String> path = new ArrayList<>();
      for (VertexInterface<String> v = end; v != null; v = v.getPredecessor()) {
         path.add(0, v.getLabel());
      }
      return path;
   }

   @Test
   void shortestPathByTimePrefersCheaperMultiHop() {
      DirectedGraph<String> g = sampleGraph();
      VertexInterface<String> end = g.computeShortestPathByTime("A", "C");
      assertEquals(2.0, end.getCost(), 1e-9);
      assertEquals(List.of("A", "B", "C"), pathTo(end));
   }

   @Test
   void shortestPathByStopsPrefersFewerHops() {
      DirectedGraph<String> g = sampleGraph();
      VertexInterface<String> end = g.computeShortestPathByStops("A", "C");
      // The direct A→C edge is one hop even though it is slower.
      assertEquals(List.of("A", "C"), pathTo(end));
      assertEquals(5.0, end.getCost(), 1e-9);
   }

   @Test
   void returnsNullWhenUnreachable() {
      DirectedGraph<String> g = sampleGraph(); // D has no incoming edges
      assertNull(g.computeShortestPathByTime("A", "D"));
   }

   @Test
   void throwsForUnknownVertex() {
      DirectedGraph<String> g = sampleGraph();
      assertThrows(IllegalArgumentException.class, () -> g.computeShortestPathByTime("A", "Z"));
   }

   @Test
   void edgeBookkeeping() {
      DirectedGraph<String> g = sampleGraph();
      assertEquals(4, g.getNumberOfVertices());
      assertEquals(3, g.getNumberOfEdges());
   }
}

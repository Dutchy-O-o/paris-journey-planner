package com.journeyplanner.graph;

import com.journeyplanner.graph.adt.Dictionary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A weighted directed graph backed by the custom {@link Dictionary} ADT.
 *
 * <p>The path algorithms ({@link #computeShortestPathByTime} and
 * {@link #computeShortestPathByStops}) keep all search state (distance,
 * predecessor, settled set) in local maps rather than mutating the vertices.
 * The graph is therefore never modified while answering a query, so a single
 * shared instance can serve concurrent requests safely. Each method returns an
 * immutable {@link Path} (ordered stop labels + total travel time) or {@code null}
 * when no route exists.
 */
public class DirectedGraph<T> {
   private final Dictionary<T, VertexInterface<T>> vertices;
   private int edgeCount;

   public DirectedGraph() {
      vertices = new Dictionary<>();
      edgeCount = 0;
   }

   /** An ordered path through the graph and its total travel time in seconds. */
   public record Path<T>(List<T> stops, double totalSeconds) {
   }

   public boolean addVertex(T vertexLabel) {
      VertexInterface<T> addOutcome = vertices.add(vertexLabel, new Vertex<>(vertexLabel));
      return addOutcome == null; // true if the label was not already present
   }

   public boolean addEdge(T start, T end, double weight) {
      VertexInterface<T> startVertex = vertices.getValue(start);
      VertexInterface<T> endVertex = vertices.getValue(end);
      boolean result = false;
      if ((startVertex != null) && (endVertex != null)) {
         result = startVertex.connect(endVertex, weight);
      }
      if (result) {
         edgeCount++;
      }
      return result;
   }

   public boolean hasEdge(T start, T end) {
      VertexInterface<T> beginVertex = vertices.getValue(start);
      VertexInterface<T> endVertex = vertices.getValue(end);
      boolean found = false;
      if ((beginVertex != null) && (endVertex != null)) {
         Iterator<VertexInterface<T>> neighbors = beginVertex.getNeighborIterator();
         while (!found && neighbors.hasNext()) {
            if (endVertex.equals(neighbors.next())) {
               found = true;
            }
         }
      }
      return found;
   }

   /**
    * Dijkstra's algorithm minimising total travel time (edge weight = seconds).
    *
    * @return the path from start to end, or null if none exists.
    */
   public Path<T> computeShortestPathByTime(T start, T end) {
      requireVertices(start, end);

      Map<T, Double> dist = new HashMap<>();
      Map<T, T> predecessor = new HashMap<>();
      Set<T> settled = new HashSet<>();
      PriorityQueue<Step<T>> queue = new PriorityQueue<>(Comparator.comparingDouble(s -> s.priority));

      dist.put(start, 0.0);
      queue.add(new Step<>(start, 0.0));

      while (!queue.isEmpty()) {
         T current = queue.poll().label;
         if (!settled.add(current)) {
            continue; // already finalised via a cheaper entry
         }
         if (current.equals(end)) {
            return buildPath(predecessor, end, dist.get(end));
         }

         double currentDist = dist.get(current);
         VertexInterface<T> vertex = vertices.getValue(current);
         Iterator<VertexInterface<T>> neighbors = vertex.getNeighborIterator();
         Iterator<Double> weights = vertex.getWeightIterator();
         while (neighbors.hasNext()) {
            T next = neighbors.next().getLabel();
            double candidate = currentDist + weights.next();
            if (candidate < dist.getOrDefault(next, Double.POSITIVE_INFINITY)) {
               dist.put(next, candidate);
               predecessor.put(next, current);
               queue.add(new Step<>(next, candidate));
            }
         }
      }
      return null;
   }

   /**
    * Shortest path minimising the number of stops (each edge is one hop), while
    * still accumulating the travel-time cost along that path for reporting.
    *
    * @return the path from start to end, or null if none exists.
    */
   public Path<T> computeShortestPathByStops(T start, T end) {
      requireVertices(start, end);

      Map<T, Integer> hops = new HashMap<>();
      Map<T, Double> cost = new HashMap<>();
      Map<T, T> predecessor = new HashMap<>();
      Set<T> settled = new HashSet<>();
      PriorityQueue<Step<T>> queue = new PriorityQueue<>(Comparator.comparingDouble(s -> s.priority));

      hops.put(start, 0);
      cost.put(start, 0.0);
      queue.add(new Step<>(start, 0));

      while (!queue.isEmpty()) {
         T current = queue.poll().label;
         if (!settled.add(current)) {
            continue;
         }
         if (current.equals(end)) {
            return buildPath(predecessor, end, cost.get(end));
         }

         int nextHops = hops.get(current) + 1;
         VertexInterface<T> vertex = vertices.getValue(current);
         Iterator<VertexInterface<T>> neighbors = vertex.getNeighborIterator();
         Iterator<Double> weights = vertex.getWeightIterator();
         while (neighbors.hasNext()) {
            T next = neighbors.next().getLabel();
            double weight = weights.next();
            if (nextHops < hops.getOrDefault(next, Integer.MAX_VALUE)) {
               hops.put(next, nextHops);
               cost.put(next, cost.get(current) + weight);
               predecessor.put(next, current);
               queue.add(new Step<>(next, nextHops));
            }
         }
      }
      return null;
   }

   private void requireVertices(T start, T end) {
      if (!vertices.contains(start) || !vertices.contains(end)) {
         throw new IllegalArgumentException("Invalid start or end vertex.");
      }
   }

   private Path<T> buildPath(Map<T, T> predecessor, T end, double totalSeconds) {
      LinkedList<T> stops = new LinkedList<>();
      for (T at = end; at != null; at = predecessor.get(at)) {
         stops.addFirst(at);
      }
      return new Path<>(stops, totalSeconds);
   }

   public int getNumberOfVertices() {
      return vertices.getSize();
   }

   public ArrayList<T> getVertices() {
      ArrayList<T> vertexList = new ArrayList<>();
      Iterator<T> keysIterator = vertices.getKeyIterator();
      while (keysIterator.hasNext()) {
         vertexList.add(keysIterator.next());
      }
      return vertexList;
   }

   public VertexInterface<T> getVertex(T key) {
      return vertices.getValue(key);
   }

   public boolean isEmpty() {
      return vertices.isEmpty();
   }

   public int getNumberOfEdges() {
      return edgeCount;
   }

   /** A priority-queue entry: a vertex label ranked by cost or hop count. */
   private static final class Step<L> {
      private final L label;
      private final double priority;

      private Step(L label, double priority) {
         this.label = label;
         this.priority = priority;
      }
   }
}

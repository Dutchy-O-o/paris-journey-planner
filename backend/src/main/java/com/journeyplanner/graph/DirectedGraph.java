package com.journeyplanner.graph;

import com.journeyplanner.graph.adt.Dictionary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * A weighted directed graph backed by the custom {@link Dictionary} ADT.
 *
 * <p>The path algorithms ({@link #computeShortestPathByTime} and
 * {@link #computeShortestPathByStops}) record cost and predecessor links on the
 * vertices themselves and return the destination vertex. Callers reconstruct the
 * route by walking {@link VertexInterface#getPredecessor()} from the destination
 * back to the origin. The graph deliberately knows nothing about how a route is
 * presented, so the same core can serve a console, a REST API, or tests.
 */
public class DirectedGraph<T> {
   private final Dictionary<T, VertexInterface<T>> vertices;
   private int edgeCount;

   public DirectedGraph() {
      vertices = new Dictionary<>();
      edgeCount = 0;
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

   protected void resetVertices() {
      Iterator<VertexInterface<T>> vertexIterator = vertices.getValueIterator();
      while (vertexIterator.hasNext()) {
         VertexInterface<T> nextVertex = vertexIterator.next();
         nextVertex.unvisit();
         nextVertex.setCost(0);
         nextVertex.setPredecessor(null);
      }
   }

   /**
    * Dijkstra's algorithm minimising total travel time (edge weight = seconds).
    *
    * @return the destination vertex with cost/predecessor populated, or null if
    *         no path exists.
    */
   public VertexInterface<T> computeShortestPathByTime(T start, T end) {
      if (!vertices.contains(start) || !vertices.contains(end)) {
         throw new IllegalArgumentException("Invalid start or end vertex.");
      }
      resetVertices();
      PriorityQueue<EntryPQ> pQueue = new PriorityQueue<>(Comparator.comparingDouble(EntryPQ::getCost));
      VertexInterface<T> lastVertex = vertices.getValue(end);
      pQueue.add(new EntryPQ(vertices.getValue(start), 0, null));

      while (!pQueue.isEmpty()) {
         EntryPQ entry = pQueue.poll();
         VertexInterface<T> vertex = entry.getVertex();

         if (!vertex.isVisited()) {
            vertex.visit();
            vertex.setCost(entry.getCost());
            vertex.setPredecessor(entry.getPredecessor());

            if (vertex.equals(lastVertex)) {
               return vertex;
            }

            Iterator<VertexInterface<T>> neighbors = vertex.getNeighborIterator();
            Iterator<Double> weights = vertex.getWeightIterator();
            while (neighbors.hasNext()) {
               VertexInterface<T> nextNeighbor = neighbors.next();
               double newCost = vertex.getCost() + weights.next();
               if (!nextNeighbor.isVisited()) {
                  pQueue.add(new EntryPQ(nextNeighbor, newCost, vertex));
               }
            }
         }
      }
      return null;
   }

   /**
    * Shortest path minimising the number of stops (each edge counts as one hop),
    * keeping the travel-time cost along that path for reporting.
    *
    * @return the destination vertex with cost/predecessor populated, or null if
    *         no path exists.
    */
   public VertexInterface<T> computeShortestPathByStops(T start, T end) {
      if (!vertices.contains(start) || !vertices.contains(end)) {
         throw new IllegalArgumentException("Invalid start or end vertex.");
      }
      resetVertices();
      PriorityQueue<EntryPQ> pQueue = new PriorityQueue<>(Comparator.comparingInt(EntryPQ::getStopCount));
      VertexInterface<T> endVertex = vertices.getValue(end);
      pQueue.add(new EntryPQ(vertices.getValue(start), 0, null, 0));

      while (!pQueue.isEmpty()) {
         EntryPQ entry = pQueue.poll();
         VertexInterface<T> vertex = entry.getVertex();

         if (!vertex.isVisited()) {
            vertex.visit();
            vertex.setCost(entry.getCost());
            vertex.setPredecessor(entry.getPredecessor());

            if (vertex.equals(endVertex)) {
               return vertex;
            }

            Iterator<VertexInterface<T>> neighbors = vertex.getNeighborIterator();
            Iterator<Double> weights = vertex.getWeightIterator();
            while (neighbors.hasNext()) {
               VertexInterface<T> nextNeighbor = neighbors.next();
               double newCost = vertex.getCost() + weights.next();
               if (!nextNeighbor.isVisited()) {
                  pQueue.add(new EntryPQ(nextNeighbor, newCost, vertex, entry.getStopCount() + 1));
               }
            }
         }
      }
      return null;
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

   private class EntryPQ {
      private final VertexInterface<T> vertex;
      private final VertexInterface<T> previousVertex;
      private final double cost;
      private final int stopCount;

      private EntryPQ(VertexInterface<T> vertex, double cost, VertexInterface<T> previousVertex) {
         this(vertex, cost, previousVertex, 0);
      }

      private EntryPQ(VertexInterface<T> vertex, double cost, VertexInterface<T> previousVertex, int stopCount) {
         this.vertex = vertex;
         this.previousVertex = previousVertex;
         this.cost = cost;
         this.stopCount = stopCount;
      }

      private int getStopCount() {
         return stopCount;
      }

      private VertexInterface<T> getVertex() {
         return vertex;
      }

      private VertexInterface<T> getPredecessor() {
         return previousVertex;
      }

      private double getCost() {
         return cost;
      }
   }
}

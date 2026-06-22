package com.journeyplanner.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A graph vertex holding a label, an adjacency list of weighted edges, and the
 * bookkeeping fields (visited / cost / predecessor) used by the path algorithms.
 */
class Vertex<T> implements VertexInterface<T> {
   private final T label;
   private final ArrayList<Edge> edgeList;
   private boolean visited;
   private VertexInterface<T> previousVertex;
   private double cost;

   Vertex(T vertexLabel) {
      label = vertexLabel;
      edgeList = new ArrayList<>();
      visited = false;
      previousVertex = null;
      cost = 0;
   }

   @Override
   public T getLabel() {
      return label;
   }

   @Override
   public boolean hasPredecessor() {
      return previousVertex != null;
   }

   @Override
   public void setPredecessor(VertexInterface<T> predecessor) {
      previousVertex = predecessor;
   }

   @Override
   public VertexInterface<T> getPredecessor() {
      return previousVertex;
   }

   @Override
   public void visit() {
      visited = true;
   }

   @Override
   public void unvisit() {
      visited = false;
   }

   @Override
   public boolean isVisited() {
      return visited;
   }

   @Override
   public double getCost() {
      return cost;
   }

   @Override
   public void setCost(double newCost) {
      cost = newCost;
   }

   @Override
   public String toString() {
      return label.toString();
   }

   @Override
   public boolean connect(VertexInterface<T> endVertex, double edgeWeight) {
      boolean result = false;
      if (!this.equals(endVertex)) {
         Iterator<VertexInterface<T>> neighbors = getNeighborIterator();
         boolean duplicateEdge = false;
         while (!duplicateEdge && neighbors.hasNext()) {
            VertexInterface<T> nextNeighbor = neighbors.next();
            if (endVertex.equals(nextNeighbor)) {
               duplicateEdge = true;
            }
         }
         if (!duplicateEdge) {
            edgeList.add(new Edge(endVertex, edgeWeight));
            result = true;
         }
      }
      return result;
   }

   @Override
   public boolean connect(VertexInterface<T> endVertex) {
      return connect(endVertex, 0);
   }

   @Override
   public Iterator<VertexInterface<T>> getNeighborIterator() {
      return new NeighborIterator();
   }

   @Override
   public Iterator<Double> getWeightIterator() {
      return new WeightIterator();
   }

   @Override
   public boolean hasNeighbor() {
      return !edgeList.isEmpty();
   }

   @Override
   public VertexInterface<T> getUnvisitedNeighbor() {
      VertexInterface<T> result = null;
      Iterator<VertexInterface<T>> neighbors = getNeighborIterator();
      while (neighbors.hasNext() && (result == null)) {
         VertexInterface<T> nextNeighbor = neighbors.next();
         if (!nextNeighbor.isVisited()) {
            result = nextNeighbor;
         }
      }
      return result;
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      }
      if ((other == null) || (getClass() != other.getClass())) {
         return false;
      }
      @SuppressWarnings("unchecked")
      Vertex<T> otherVertex = (Vertex<T>) other;
      return label.equals(otherVertex.label);
   }

   @Override
   public int hashCode() {
      return label.hashCode();
   }

   private class NeighborIterator implements Iterator<VertexInterface<T>> {
      private final Iterator<Edge> edges = edgeList.iterator();

      @Override
      public boolean hasNext() {
         return edges.hasNext();
      }

      @Override
      public VertexInterface<T> next() {
         if (!edges.hasNext()) {
            throw new NoSuchElementException();
         }
         return edges.next().getEndVertex();
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private class WeightIterator implements Iterator<Double> {
      private final Iterator<Edge> edges = edgeList.iterator();

      @Override
      public boolean hasNext() {
         return edges.hasNext();
      }

      @Override
      public Double next() {
         if (!edges.hasNext()) {
            throw new NoSuchElementException();
         }
         return edges.next().getWeight();
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private class Edge {
      private final VertexInterface<T> vertex;
      private final double weight;

      private Edge(VertexInterface<T> endVertex, double edgeWeight) {
         vertex = endVertex;
         weight = edgeWeight;
      }

      private VertexInterface<T> getEndVertex() {
         return vertex;
      }

      private double getWeight() {
         return weight;
      }
   }
}

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

class Vertex<T> implements VertexInterface<T> {
   private T label;
   private ArrayList<Edge> edgeList; // Change LinkedList to ArrayList
   private boolean visited;
   private VertexInterface<T> previousVertex;
   private double cost;

   public Vertex(T vertexLabel) {
      label = vertexLabel;
      edgeList = new ArrayList<>(); // Change LinkedList to ArrayList
      visited = false;
      previousVertex = null;
      cost = 0;
   }

   public T getLabel() {
      return label;
   }

   public boolean hasPredecessor() {
      return previousVertex != null;
   }

   public void setPredecessor(VertexInterface<T> predecessor) {
      previousVertex = predecessor;
   }

   public VertexInterface<T> getPredecessor() {
      return previousVertex;
   }

   public void visit() {
      visited = true;
   }

   public void unvisit() {
      visited = false;
   }

   public boolean isVisited() {
      return visited;
   }

   public double getCost() {
      return cost;
   }

   public void setCost(double newCost) {
      cost = newCost;
   }

   public String toString() {
      return label.toString();
   }

   private class WeightIterator implements Iterator<Double> {
      private Iterator<Edge> edges;

      private WeightIterator() {
         edges = edgeList.iterator(); // Change getIterator to iterator()
      }

      public boolean hasNext() {
         return edges.hasNext();
      }

      public Double next() {
         Double edgeWeight = 0.0;
         if (edges.hasNext()) {
            Edge edgeToNextNeighbor = edges.next();
            edgeWeight = edgeToNextNeighbor.getWeight();
         } else
            throw new NoSuchElementException();

         return edgeWeight;
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   public Iterator<Double> getWeightIterator() {
      return new WeightIterator();
   }

   public boolean connect(VertexInterface<T> endVertex, double edgeWeight) {
      boolean result = false;

      if (!this.equals(endVertex)) {
         Iterator<VertexInterface<T>> neighbors = getNeighborIterator();
         boolean duplicateEdge = false;

         while (!duplicateEdge && neighbors.hasNext()) {
            VertexInterface<T> nextNeighbor = neighbors.next();
            if (endVertex.equals(nextNeighbor))
               duplicateEdge = true;
         }

         if (!duplicateEdge) {
            edgeList.add(new Edge(endVertex, edgeWeight));
            result = true;
         }
      }

      return result;
   }

   public boolean connect(VertexInterface<T> endVertex) {
      return connect(endVertex, 0);
   }

   public Iterator<VertexInterface<T>> getNeighborIterator() {
      return new NeighborIterator();
   }

   public boolean hasNeighbor() {
      return !edgeList.isEmpty();
   }

   public VertexInterface<T> getUnvisitedNeighbor() {
      VertexInterface<T> result = null;

      Iterator<VertexInterface<T>> neighbors = getNeighborIterator();
      while (neighbors.hasNext() && (result == null)) {
         VertexInterface<T> nextNeighbor = neighbors.next();
         if (!nextNeighbor.isVisited())
            result = nextNeighbor;
      }

      return result;
   }

   public boolean equals(Object other) {
      boolean result;

      if ((other == null) || (getClass() != other.getClass()))
         result = false;
      else {
         @SuppressWarnings("unchecked")
         Vertex<T> otherVertex = (Vertex<T>) other;
         result = label.equals(otherVertex.label);
      }

      return result;
   }

   private class NeighborIterator implements Iterator<VertexInterface<T>> {
      private Iterator<Edge> edges;

      private NeighborIterator() {
         edges = edgeList.iterator(); // Change getIterator to iterator()
      }

      public boolean hasNext() {
         return edges.hasNext();
      }

      public VertexInterface<T> next() {
         VertexInterface<T> nextNeighbor = null;

         if (edges.hasNext()) {
            Edge edgeToNextNeighbor = edges.next();
            nextNeighbor = edgeToNextNeighbor.getEndVertex();
         } else
            throw new NoSuchElementException();

         return nextNeighbor;
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   protected class Edge {
      private VertexInterface<T> vertex;
      private double weight;

      protected Edge(VertexInterface<T> endVertex, double edgeWeight) {
         vertex = endVertex;
         weight = edgeWeight;
      }

      protected VertexInterface<T> getEndVertex() {
         return vertex;
      }

      protected double getWeight() {
         return weight;
      }
   }

   public void display() {
      System.out.print("VertexName: " + label + " ");
      Iterator<VertexInterface<T>> i = getNeighborIterator();
      Iterator<Double> w = getWeightIterator();

      while (i.hasNext()) {
         Vertex<T> v = (Vertex<T>) i.next();
         System.out.print("EdgeName: " + v + " " + w.next() + " ");
      }

      System.out.println();
   }
}

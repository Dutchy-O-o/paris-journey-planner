package com.journeyplanner.graph;

import java.util.Iterator;

/**
 * An interface for a vertex in a graph.
 *
 * @author Frank M. Carrano
 * @author Timothy M. Henry
 */
public interface VertexInterface<T> {
   /** @return the object that labels the vertex. */
   T getLabel();

   /** Marks this vertex as visited. */
   void visit();

   /** Removes this vertex's visited mark. */
   void unvisit();

   /** @return true if the vertex is visited. */
   boolean isVisited();

   /** Connects this vertex to a given vertex with a weighted, directed edge.
       @return true if the edge was added. */
   boolean connect(VertexInterface<T> endVertex, double edgeWeight);

   /** Connects this vertex to a given vertex with an unweighted, directed edge.
       @return true if the edge was added. */
   boolean connect(VertexInterface<T> endVertex);

   /** @return an iterator over the neighbouring vertices. */
   Iterator<VertexInterface<T>> getNeighborIterator();

   /** @return an iterator over the edge weights to this vertex's neighbours. */
   Iterator<Double> getWeightIterator();

   /** @return true if the vertex has at least one neighbour. */
   boolean hasNeighbor();

   /** @return an unvisited neighbour, or null if none exists. */
   VertexInterface<T> getUnvisitedNeighbor();

   /** Records the previous vertex on a path to this vertex. */
   void setPredecessor(VertexInterface<T> predecessor);

   /** @return this vertex's recorded predecessor, or null. */
   VertexInterface<T> getPredecessor();

   /** @return true if a predecessor was recorded. */
   boolean hasPredecessor();

   /** Records the cost of a path to this vertex. */
   void setCost(double newCost);

   /** @return the recorded cost of the path to this vertex. */
   double getCost();
}

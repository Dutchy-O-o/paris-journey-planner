import ADT.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;
import java.util.ArrayList;
public class DirectedGraph<T> {
    private Dictionary<T, VertexInterface<T>> vertices;
    private int edgeCount;

    public DirectedGraph(){
        vertices = new Dictionary<>();
        edgeCount = 0;
    }

    public boolean addVertex(T vertexLabel)
    {
        VertexInterface<T> addOutcome = vertices.add(vertexLabel, new Vertex<>(vertexLabel));
        return addOutcome == null; // Was addition to dictionary successful?
    } // end addVertex

    public boolean addEdge(T start, T end, double weight){//adds an edge between two vertexes
        boolean result = false;
        VertexInterface<T> startVertex = vertices.getValue(start);
        VertexInterface<T> endVertex = vertices.getValue(end);
        if((startVertex != null) && (endVertex != null)){
            result = startVertex.connect(endVertex, weight);
        }
        if(result){
            edgeCount++;
        }
        return result;
    }

    public boolean hasEdge(T start, T end){
        boolean found = false;
        VertexInterface<T> beginVertex = vertices.getValue(start);
        VertexInterface<T> endVertex = vertices.getValue(end);
        if ( (beginVertex != null) && (endVertex != null) ){
            Iterator<VertexInterface<T>> neighbors = beginVertex.getNeighborIterator();
            //checks all neighbors(edges) of the start vertex with iterator
            while (!found && neighbors.hasNext())
            {
                VertexInterface<T> nextNeighbor = neighbors.next();
                if (endVertex.equals(nextNeighbor))
                    found = true;
            }
        }

        return found;
    }

    protected void resetVertices(){
        Iterator<VertexInterface<T>> vertexIterator = vertices.getValueIterator();
        while (vertexIterator.hasNext()){
            VertexInterface<T> nextVertex = vertexIterator.next();
            nextVertex.unvisit();
            nextVertex.setCost(0);
            nextVertex.setPredecessor(null);
        }
    }

    public void shortestPath(T start, T end){
        if (!vertices.contains(start) || !vertices.contains(end)) {
            throw new IllegalArgumentException("Invalid start or end vertex.");
        }
        resetVertices();
        //use priorityqueue for it priors the cost values of entryPQ's
        PriorityQueue<EntryPQ> pQueue = new PriorityQueue<>(Comparator.comparingDouble(EntryPQ::getCost));
        VertexInterface<T> firstVertex = vertices.getValue(start);
        VertexInterface<T> lastVertex = vertices.getValue(end);
        pQueue.add(new EntryPQ(firstVertex, 0, null));

        //the while doesn't end untill there is not any possible ways between
        while(!pQueue.isEmpty()){
            EntryPQ cEntry = pQueue.poll();//take the first value of queue (startVertex)
            VertexInterface<T> cVertex = cEntry.getVertex();

            //take the unvisited vertexes to the possible paths
            if(!cVertex.isVisited()){
                // Mark the current vertex as visited and update cost/predecessor
                cVertex.visit();
                cVertex.setCost(cEntry.getCost());
                cVertex.setPredecessor(cEntry.getPredecessor());

                if (cVertex.equals(lastVertex)) {
                    displayShortestPath(lastVertex);
                    System.out.println();
                    System.out.println("Time: "+cEntry.getCost()/60+" minutes");
                    return;
                }

                // Explore neighbors and update the priority queue
                Iterator<VertexInterface<T>> n = cVertex.getNeighborIterator();
                Iterator<Double> weights = cVertex.getWeightIterator();

                while(n.hasNext()){
                    VertexInterface<T> nextNeighbor = n.next();
                    double newCost = cVertex.getCost() + weights.next();
                    if(!nextNeighbor.isVisited()){
                        pQueue.add(new EntryPQ(nextNeighbor, newCost, cVertex));
                    }
                }
            }
        }
        System.out.println("No path found for your start and end stops.");

    }

    public void shortestPathWithMinStops(T start, T end) {
        if (!vertices.contains(start) || !vertices.contains(end)) {
            System.out.println("Invalid start or end vertex.");
            return;
        }
        resetVertices();
        //use priorityqueue for it priors the stop count values of entryPQ's
        PriorityQueue<EntryPQ> pQueue = new PriorityQueue<>(Comparator.comparingInt(EntryPQ::getStopCount));
        VertexInterface<T> startVertex = vertices.getValue(start);
        VertexInterface<T> endVertex = vertices.getValue(end);
        pQueue.add(new EntryPQ(startVertex, 0, null, 0));
    
        while (!pQueue.isEmpty()) {
            EntryPQ currentEntry = pQueue.poll();
            VertexInterface<T> currentVertex = currentEntry.getVertex();
    
            if (!currentVertex.isVisited()) {
                currentVertex.visit();
                currentVertex.setCost(currentEntry.getCost());
                currentVertex.setPredecessor(currentEntry.getPredecessor());
    
                if (currentVertex.equals(endVertex)) {
                    displayShortestPath(endVertex);
                    System.out.println();
                    System.out.println("Total stops: "+currentEntry.getStopCount());
                    System.out.println("Time: "+currentEntry.getCost()/60+" minutes");
                    return;
                }
    
                Iterator<VertexInterface<T>> neighbors = currentVertex.getNeighborIterator();
                Iterator<Double> weights = currentVertex.getWeightIterator();
    
                while (neighbors.hasNext()) {
                    VertexInterface<T> nextNeighbor = neighbors.next();
                    double newCost = currentVertex.getCost() + weights.next();
                    int newStopCount = currentEntry.getStopCount() + 1; // Assuming each edge represents one stop
    
                    if (!nextNeighbor.isVisited()) {
                        pQueue.add(new EntryPQ(nextNeighbor, newCost, currentVertex, newStopCount));
                    }
                }
            }
        }
        System.out.println("No path found for your start and end stops with minimum stops.");
    }

    private void displayShortestPath(VertexInterface<T> endVertex) {
    System.out.println("Suggestion:");
    Stack<VertexInterface<T>> pathStack = new Stack<>();
    VertexInterface<T> currentVertex = endVertex;

    // Trace back the path from end to start and push vertices onto a stack
    while (currentVertex != null) {
        pathStack.push(currentVertex);
        currentVertex = currentVertex.getPredecessor();
    }

    String currentLine = " ";
    int stationNum = 0;
    boolean firstTraversal = true;
    Station prevStation = (Station)pathStack.pop().getLabel();//while starts with second station

    while(!pathStack.isEmpty()){
        
        Station currentStation = (Station)pathStack.pop().getLabel();// next station (starts with second station)
        String line =currentStation.getCurrentLine(prevStation.getStopName());
        stationNum++;

        if(firstTraversal){
            currentLine = line;
            System.out.println("Line " + currentLine);
            System.out.print(prevStation);
            firstTraversal = false;
        }

        if(line == null){
            System.out.print(" - " +prevStation.getStopName());
            System.out.println("  ( " + stationNum + " stations )");
            currentLine = line;
            System.out.println("Line W");
            System.out.print(prevStation);

            stationNum = 0;
        }
        
        else if(!line.equals(currentLine)){
            System.out.print(" - " +prevStation.getStopName());
            System.out.println("  ( " + stationNum + " stations )");
            currentLine = line;
            System.out.println("Line " + currentLine + " ");
            System.out.print(prevStation);

            stationNum =0;

        }
        prevStation = currentStation;
    }
    System.out.print(" - " + ((Station)endVertex.getLabel()).getStopName());
    if(stationNum == 0){
        System.out.println(" ( " + 1 + " stations) ");
    }else{
        System.out.println(" ( " + stationNum + " stations) ");

    }
}

    public int getNumberOfVertices() {
        return vertices.getSize();
    }

    public ArrayList<T> getVertices() {
        ArrayList<T> vertexList = new ArrayList<>();
        Iterator<T> keysIterator = vertices.getKeyIterator();
        while (keysIterator.hasNext()) {
            T vertexLabel = keysIterator.next();
            vertexList.add(vertexLabel);
        }
        return vertexList;
    }

    public VertexInterface<T> dictGetValue(T key){
        return vertices.getValue(key);
    }

    public Station getValue(T key){
        Iterator<VertexInterface<T>> vertexIterator = vertices.getValueIterator();
        while(vertexIterator.hasNext()){
            VertexInterface<T> value = vertexIterator.next();
            if(key.equals(value)){
                return (Station)value.getLabel();
            }
        }
        return null;
    }

    public VertexInterface<T> getVertexByName(String stopName) {
        Iterator<VertexInterface<T>> vertexIterator = vertices.getValueIterator();
        while (vertexIterator.hasNext()) {
            VertexInterface<T> vertex = vertexIterator.next();
            if (vertex.getLabel() instanceof Station) {
                Station station = (Station) vertex.getLabel();
                if (station.getStopName().equals(stopName)) {
                    return vertex;
                }
            }
        }
        return null; // Station with the given name not found in the graph
    }

    public VertexInterface<T> getVertexWithName(String key){
        Iterator<VertexInterface<T>> vertexIterator = vertices.getValueIterator();
        while(vertexIterator.hasNext()){
            VertexInterface<T> value = vertexIterator.next();
            T label =value.getLabel();
            if(key.equals(((Station)label).getStopName())){
                return value;
            }
        }
        return null;
    }

    public void displayEdges()
	{
		System.out.println("\nEdges exist from the first vertex in each line to the other vertices in the line.");
		System.out.println("(Edge weights are given; weights are zero for unweighted graphs):\n");
		Iterator<VertexInterface<T>> vertexIterator = vertices.getValueIterator();
		while (vertexIterator.hasNext())
		{
            Station a = new Station();
            a.setStopName("Nation");
            VertexInterface<T> A = vertexIterator.next();
			((Vertex<T>)(A)).display();
            if(A.getLabel().equals(a)){
                int x = 0;
                x++;
            }
		} // end while
	} // end displayEdges


    public boolean isEmpty(){
        return vertices.isEmpty();
    }

    public int getNumberOfEdges(){
        return edgeCount;
    }

    private class EntryPQ implements Comparable<EntryPQ>{
        private VertexInterface<T> vertex;
        private VertexInterface<T> previousVertex;
        private double cost; // cost to nextVertex
        private int stopCount;

        private EntryPQ(VertexInterface<T> vertex, double cost, VertexInterface<T> previousVertex){
            this.vertex = vertex;
            this.previousVertex = previousVertex;
            this.cost = cost;
        } // end constructor

        private EntryPQ(VertexInterface<T> vertex, double cost, VertexInterface<T> previousVertex, int stopCount) {
            this.vertex = vertex;
            this.previousVertex = previousVertex;
            this.cost = cost;
            this.stopCount = stopCount;
        } // end constructor

        public int getStopCount() {
            return stopCount;
        } // end getStopCount

        public VertexInterface<T> getVertex(){
            return vertex;
        } // end getVertex

        public VertexInterface<T> getPredecessor(){
            return previousVertex;
        } // end getPredecessor

        public double getCost(){
            return cost;
        } // end getCost

        @Override
        public int compareTo(EntryPQ otherEntry){
            return Double.compare(cost, otherEntry.cost);
        }

        public String toString(){
            return vertex.toString() + " " + cost;
        }
    }
}

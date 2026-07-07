package com.journeyplanner.service;

import com.journeyplanner.graph.DirectedGraph;
import com.journeyplanner.model.Preference;
import com.journeyplanner.model.RouteResult;
import com.journeyplanner.model.RouteResult.RouteSegment;
import com.journeyplanner.model.RouteResult.StationPoint;
import com.journeyplanner.model.Station;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Computes journeys on top of the {@link DirectedGraph} core and turns the raw
 * predecessor chain into a presentation-friendly {@link RouteResult}: an ordered
 * path grouped into per-line segments, with the travel time and stop count.
 */
@Service
public class RouteService {

   private static final String WALK_LABEL = "Walk";

   private final GraphLoader loader;

   public RouteService(GraphLoader loader) {
      this.loader = loader;
   }

   public RouteResult findRoute(String originName, String destinationName, Preference preference) {
      Station origin = loader.getStation(originName);
      if (origin == null) {
         throw new StationNotFoundException(originName);
      }
      Station destination = loader.getStation(destinationName);
      if (destination == null) {
         throw new StationNotFoundException(destinationName);
      }

      DirectedGraph<Station> graph = loader.getGraph();
      DirectedGraph.Path<Station> path = (preference == Preference.STOPS)
            ? graph.computeShortestPathByStops(origin, destination)
            : graph.computeShortestPathByTime(origin, destination);

      if (path == null) {
         throw new NoRouteException(origin.getStopName(), destination.getStopName());
      }

      List<Station> stops = path.stops();
      List<RouteSegment> segments = groupIntoSegments(stops);

      return new RouteResult(
            origin.getStopName(),
            destination.getStopName(),
            preference.name(),
            path.totalSeconds(),
            Math.max(0, stops.size() - 1),
            segments);
   }

   /**
    * Groups consecutive stations sharing the same line into one segment. The line
    * used to travel from {@code prev} to {@code cur} is resolved via
    * {@link Station#getCurrentLine(String)}; a null line means a walking transfer.
    * Adjacent segments share the transfer station as their boundary.
    */
   private List<RouteSegment> groupIntoSegments(List<Station> path) {
      List<RouteSegment> segments = new ArrayList<>();
      if (path.size() < 2) {
         return segments;
      }

      String segmentLine = null;
      List<StationPoint> stations = new ArrayList<>();

      for (int i = 1; i < path.size(); i++) {
         Station prev = path.get(i - 1);
         Station cur = path.get(i);
         String line = cur.getCurrentLine(prev.getStopName());

         if (stations.isEmpty()) {
            segmentLine = line;
            stations.add(toPoint(prev));
            stations.add(toPoint(cur));
         } else if (Objects.equals(line, segmentLine)) {
            stations.add(toPoint(cur));
         } else {
            segments.add(toSegment(segmentLine, stations));
            segmentLine = line;
            stations = new ArrayList<>();
            stations.add(toPoint(prev));
            stations.add(toPoint(cur));
         }
      }
      segments.add(toSegment(segmentLine, stations));
      return segments;
   }

   private RouteSegment toSegment(String line, List<StationPoint> stations) {
      boolean walking = (line == null);
      return new RouteSegment(walking ? WALK_LABEL : line, walking, stations);
   }

   private StationPoint toPoint(Station station) {
      return new StationPoint(station.getStopName(), station.getLatitude(), station.getLongitude());
   }
}

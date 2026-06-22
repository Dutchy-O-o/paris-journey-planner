package com.journeyplanner.model;

import java.util.List;

/**
 * A computed journey from origin to destination, grouped into per-line segments.
 *
 * @param origin            name of the starting station
 * @param destination       name of the ending station
 * @param preference        the optimisation used: {@code "TIME"} or {@code "STOPS"}
 * @param totalSeconds      total travel time in seconds
 * @param totalStops        number of hops (edges) along the route
 * @param segments          consecutive legs grouped by line; adjacent segments
 *                          share the transfer station as their boundary
 */
public record RouteResult(
      String origin,
      String destination,
      String preference,
      double totalSeconds,
      int totalStops,
      List<RouteSegment> segments) {

   /** A leg of the journey travelled on a single line (or a walking transfer). */
   public record RouteSegment(String line, boolean walking, List<StationPoint> stations) {
      /** @return number of hops on this leg. */
      public int stationCount() {
         return Math.max(0, stations.size() - 1);
      }
   }

   /** A station along the route with its name and (optional) coordinates. */
   public record StationPoint(String name, Double latitude, Double longitude) {
   }
}

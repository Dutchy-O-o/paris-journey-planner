package com.journeyplanner.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A transit station. Identity is its name (so the graph treats two stops with the
 * same name as one vertex). It also remembers, per metro line, which neighbouring
 * stations it is directly connected to, which lets the route builder name the line
 * used to travel between two adjacent stations.
 */
public class Station {

   private String stopName;
   private String stopId;
   private Double latitude;
   private Double longitude;
   private final List<Route> lines = new ArrayList<>();

   public String getStopName() {
      return stopName;
   }

   public void setStopName(String stopName) {
      this.stopName = stopName;
   }

   public String getStopId() {
      return stopId;
   }

   public void setStopId(String stopId) {
      this.stopId = stopId;
   }

   public Double getLatitude() {
      return latitude;
   }

   public void setLatitude(Double latitude) {
      this.latitude = latitude;
   }

   public Double getLongitude() {
      return longitude;
   }

   public void setLongitude(Double longitude) {
      this.longitude = longitude;
   }

   public boolean hasCoordinates() {
      return latitude != null && longitude != null;
   }

   public List<Route> getLines() {
      return lines;
   }

   /** Records that {@code relatedStation} is an adjacent stop on the given line. */
   public void addRoute(String route, String relatedStation) {
      Route currentRoute = findRoute(route);
      if (currentRoute == null) {
         lines.add(new Route(route, relatedStation));
      } else {
         currentRoute.addStation(relatedStation);
      }
   }

   private Route findRoute(String route) {
      for (Route r : lines) {
         if (r.getRoute().equals(route)) {
            return r;
         }
      }
      return null;
   }

   /** @return the line connecting this station to {@code prevName}, or null (e.g. a walking transfer). */
   public String getCurrentLine(String prevName) {
      for (Route route : lines) {
         if (route.isIn(prevName)) {
            return route.getRoute();
         }
      }
      return null;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null || obj.getClass() != this.getClass()) {
         return false;
      }
      Station otherStation = (Station) obj;
      return this.stopName.equals(otherStation.stopName);
   }

   @Override
   public int hashCode() {
      return stopName.hashCode();
   }

   @Override
   public String toString() {
      return stopName;
   }

   /** A single metro line passing through the owning station, with its adjacent stops on that line. */
   public static class Route {
      private final String route;
      private final List<String> relatedStations = new ArrayList<>();

      Route(String route, String relatedStation) {
         this.route = route;
         relatedStations.add(relatedStation);
      }

      void addStation(String relatedStation) {
         if (!relatedStations.contains(relatedStation)) {
            relatedStations.add(relatedStation);
         }
      }

      boolean isIn(String station) {
         return relatedStations.contains(station);
      }

      public String getRoute() {
         return route;
      }
   }
}

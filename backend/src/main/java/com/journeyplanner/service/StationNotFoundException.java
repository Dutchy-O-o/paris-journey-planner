package com.journeyplanner.service;

/** Thrown when a requested station name is not present in the network. */
public class StationNotFoundException extends RuntimeException {
   public StationNotFoundException(String stationName) {
      super("Station not found: " + stationName);
   }
}

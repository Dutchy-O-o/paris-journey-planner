package com.journeyplanner.service;

/** Thrown when no path exists between two stations. */
public class NoRouteException extends RuntimeException {
   public NoRouteException(String origin, String destination) {
      super("No route found between '" + origin + "' and '" + destination + "'");
   }
}

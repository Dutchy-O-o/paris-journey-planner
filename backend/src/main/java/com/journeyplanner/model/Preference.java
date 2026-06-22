package com.journeyplanner.model;

import java.util.Locale;

/** How a journey should be optimised. */
public enum Preference {
   /** Minimise total travel time (Dijkstra over edge weights in seconds). */
   TIME,
   /** Minimise the number of stops. */
   STOPS;

   /** Lenient parser accepting names ("time"/"stops") or the legacy numeric codes (1 = time, 0 = stops). */
   public static Preference from(String raw) {
      if (raw == null) {
         return TIME;
      }
      // Locale.ROOT avoids the Turkish dotted-I pitfall ("time" -> "TİME").
      String value = raw.trim().toUpperCase(Locale.ROOT);
      return switch (value) {
         case "TIME", "1", "MIN_TIME", "FASTEST" -> TIME;
         case "STOPS", "0", "MIN_STOPS", "FEWER_STOPS" -> STOPS;
         default -> throw new IllegalArgumentException("Unknown preference: " + raw);
      };
   }
}

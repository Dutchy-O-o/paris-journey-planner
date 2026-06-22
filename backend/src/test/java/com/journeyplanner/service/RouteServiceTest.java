package com.journeyplanner.service;

import com.journeyplanner.model.Preference;
import com.journeyplanner.model.RouteResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Integration-style tests over the real bundled Paris dataset. */
class RouteServiceTest {

   private static RouteService service;

   @BeforeAll
   static void setUp() {
      GraphLoader loader = new GraphLoader(
            "data/Paris_RER_Metro_v2.csv",
            "data/walk_edges.txt",
            "data/station_coords.csv");
      loader.load();
      service = new RouteService(loader);
   }

   @Test
   void findsKnownRouteWithEndpointsAndTime() {
      RouteResult r = service.findRoute("Nation", "Bastille", Preference.TIME);
      assertEquals("Nation", r.origin());
      assertEquals("Bastille", r.destination());
      assertEquals(2, r.totalStops());
      assertTrue(r.totalSeconds() > 0);
      assertFalse(r.segments().isEmpty());
   }

   @Test
   void everyStationOnRouteHasCoordinates() {
      RouteResult r = service.findRoute("Nation", "Charles de Gaulle-Etoile", Preference.TIME);
      r.segments().forEach(seg -> seg.stations().forEach(s -> {
         assertNotNull(s.latitude(), "missing latitude for " + s.name());
         assertNotNull(s.longitude(), "missing longitude for " + s.name());
      }));
   }

   @Test
   void consecutiveSegmentsShareTheTransferStation() {
      RouteResult r = service.findRoute("Nation", "Bastille", Preference.TIME);
      for (int i = 1; i < r.segments().size(); i++) {
         var prev = r.segments().get(i - 1).stations();
         var curr = r.segments().get(i).stations();
         assertEquals(prev.get(prev.size() - 1).name(), curr.get(0).name(),
               "transfer station must be shared between adjacent segments");
      }
   }

   @Test
   void unknownStationIsReported() {
      assertThrows(StationNotFoundException.class,
            () -> service.findRoute("Nowhere", "Bastille", Preference.TIME));
   }

   @Test
   void preferenceParsingIsLenient() {
      assertEquals(Preference.TIME, Preference.from("1"));
      assertEquals(Preference.STOPS, Preference.from("0"));
      assertEquals(Preference.TIME, Preference.from("time"));
      assertEquals(Preference.STOPS, Preference.from("STOPS"));
      assertThrows(IllegalArgumentException.class, () -> Preference.from("banana"));
   }
}

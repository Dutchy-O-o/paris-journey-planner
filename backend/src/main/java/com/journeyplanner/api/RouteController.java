package com.journeyplanner.api;

import com.journeyplanner.api.dto.RouteRequest;
import com.journeyplanner.api.dto.StationDto;
import com.journeyplanner.model.Preference;
import com.journeyplanner.model.RouteResult;
import com.journeyplanner.service.GraphLoader;
import com.journeyplanner.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/** REST endpoints for the journey planner. */
@RestController
@RequestMapping("/api")
public class RouteController {

   private final RouteService routeService;
   private final GraphLoader graphLoader;

   public RouteController(RouteService routeService, GraphLoader graphLoader) {
      this.routeService = routeService;
      this.graphLoader = graphLoader;
   }

   /** Lists all stations, optionally filtered by a name substring (for autocomplete). */
   @GetMapping("/stations")
   public List<StationDto> stations(@RequestParam(required = false) String q) {
      String needle = q == null ? "" : q.trim().toLowerCase();
      return graphLoader.getStations().stream()
            .filter(s -> needle.isEmpty() || s.getStopName().toLowerCase().contains(needle))
            .map(StationDto::from)
            .sorted(Comparator.comparing(StationDto::name))
            .toList();
   }

   /** Computes a route between two stations for the given preference. */
   @PostMapping("/route")
   public RouteResult route(@Valid @RequestBody RouteRequest request) {
      Preference preference = Preference.from(request.preference());
      return routeService.findRoute(request.origin(), request.destination(), preference);
   }
}

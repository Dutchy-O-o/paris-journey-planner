package com.journeyplanner.service;

import com.journeyplanner.graph.DirectedGraph;
import com.journeyplanner.model.Station;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the transit {@link DirectedGraph} once at startup from the bundled GTFS-style
 * CSV. Each row is one stop of one trip; consecutive stops of the same trip become a
 * directed, time-weighted edge. An optional walk-edges file adds walking transfers.
 *
 * <p>A name → {@link Station} index is kept (a JDK map, purely for O(1) lookup) so the
 * graph itself stays backed by the custom dictionary ADT.
 */
@Component
public class GraphLoader {

   private static final Logger log = LoggerFactory.getLogger(GraphLoader.class);

   // CSV columns: stop_id, stop_name, arrival_time, stop_sequence, direction_id,
   //              route_short_name, route_long_name, route_type
   private static final int COL_STOP_ID = 0;
   private static final int COL_STOP_NAME = 1;
   private static final int COL_ARRIVAL_TIME = 2;
   private static final int COL_STOP_SEQUENCE = 3;
   private static final int COL_ROUTE_SHORT_NAME = 5;

   private static final double WALK_TRANSFER_SECONDS = 300; // 5 minutes

   private final DirectedGraph<Station> graph = new DirectedGraph<>();
   private final Map<String, Station> stationsByName = new LinkedHashMap<>();

   private final String dataFile;
   private final String walkFile;
   private final String coordsFile;

   public GraphLoader(
         @Value("${journey.data-file:data/Paris_RER_Metro_v2.csv}") String dataFile,
         @Value("${journey.walk-file:data/walk_edges.txt}") String walkFile,
         @Value("${journey.coords-file:data/station_coords.csv}") String coordsFile) {
      this.dataFile = dataFile;
      this.walkFile = walkFile;
      this.coordsFile = coordsFile;
   }

   @PostConstruct
   void load() {
      loadStops(dataFile);
      loadCoordinates(coordsFile);
      loadWalkEdges(walkFile);
      log.info("Graph loaded: {} stations, {} edges", graph.getNumberOfVertices(), graph.getNumberOfEdges());
   }

   private void loadStops(String resourcePath) {
      List<String[]> rows = readCsv(resourcePath, true);
      Station previousStation = null;
      int previousSequence = 0;
      int previousArrival = 0;

      for (String[] values : rows) {
         if (values.length <= COL_ROUTE_SHORT_NAME) {
            continue; // malformed row
         }
         String name = values[COL_STOP_NAME].trim();
         Station currentStation = stationsByName.get(name);
         if (currentStation == null) {
            currentStation = new Station();
            currentStation.setStopName(name);
            currentStation.setStopId(values[COL_STOP_ID].trim());
            stationsByName.put(name, currentStation);
            graph.addVertex(currentStation);
         }

         int currentArrival = parseInt(values[COL_ARRIVAL_TIME].trim());
         int currentSequence = parseInt(values[COL_STOP_SEQUENCE].trim());

         // Connect to the previous stop only when it is the immediately preceding
         // stop of the same trip (sequence numbers differ by exactly one).
         if (previousStation != null && Math.abs(currentSequence - previousSequence) == 1) {
            currentStation.addRoute(values[COL_ROUTE_SHORT_NAME].trim(), previousStation.getStopName());
            graph.addEdge(previousStation, currentStation, Math.abs(currentArrival - previousArrival));
         }

         previousStation = currentStation;
         previousSequence = currentSequence;
         previousArrival = currentArrival;
      }
   }

   private void loadCoordinates(String resourcePath) {
      if (!new ClassPathResource(resourcePath).exists()) {
         log.warn("No coordinates file at {} — stations will have no lat/lon", resourcePath);
         return;
      }
      int applied = 0;
      for (String[] values : readCsv(resourcePath, true)) {
         if (values.length < 3) {
            continue;
         }
         Station station = stationsByName.get(values[0].trim());
         if (station != null) {
            try {
               station.setLatitude(Double.parseDouble(values[1].trim()));
               station.setLongitude(Double.parseDouble(values[2].trim()));
               applied++;
            } catch (NumberFormatException ignored) {
               // leave coordinates null for this station
            }
         }
      }
      log.info("Applied coordinates to {}/{} stations", applied, stationsByName.size());
   }

   private void loadWalkEdges(String resourcePath) {
      if (!new ClassPathResource(resourcePath).exists()) {
         log.info("No walk-edges file at {} — skipping walking transfers", resourcePath);
         return;
      }
      int added = 0;
      for (String[] values : readCsv(resourcePath, false)) {
         if (values.length < 2) {
            continue;
         }
         Station from = stationsByName.get(values[0].trim());
         Station to = stationsByName.get(values[1].trim());
         if (from != null && to != null && graph.addEdge(from, to, WALK_TRANSFER_SECONDS)) {
            added++;
         }
      }
      log.info("Added {} walking transfer edges", added);
   }

   private List<String[]> readCsv(String resourcePath, boolean skipHeader) {
      List<String[]> rows = new ArrayList<>();
      ClassPathResource resource = new ClassPathResource(resourcePath);
      try (InputStream in = resource.getInputStream();
           BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
         String line = reader.readLine();
         if (line != null) {
            // Strip a UTF-8 BOM if present on the first line.
            if (line.startsWith("﻿")) {
               line = line.substring(1);
            }
            if (!skipHeader) {
               rows.add(line.split(","));
            }
         }
         while ((line = reader.readLine()) != null) {
            if (!line.isBlank()) {
               rows.add(line.split(","));
            }
         }
      } catch (IOException e) {
         throw new UncheckedIOException("Failed to read resource: " + resourcePath, e);
      }
      return rows;
   }

   private static int parseInt(String value) {
      try {
         return Integer.parseInt(value);
      } catch (NumberFormatException e) {
         return 0;
      }
   }

   public DirectedGraph<Station> getGraph() {
      return graph;
   }

   public Station getStation(String name) {
      return name == null ? null : stationsByName.get(name.trim());
   }

   public Collection<Station> getStations() {
      return stationsByName.values();
   }
}

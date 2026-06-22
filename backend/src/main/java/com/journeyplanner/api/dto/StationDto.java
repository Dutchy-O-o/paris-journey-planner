package com.journeyplanner.api.dto;

import com.journeyplanner.model.Station;

/** Lightweight station view for the station list / autocomplete. */
public record StationDto(String name, Double latitude, Double longitude) {
   public static StationDto from(Station station) {
      return new StationDto(station.getStopName(), station.getLatitude(), station.getLongitude());
   }
}

package com.journeyplanner.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * A journey request. {@code preference} is optional and accepts "TIME"/"STOPS"
 * (or the legacy 1/0 codes); it defaults to fastest-by-time.
 */
public record RouteRequest(
      @NotBlank(message = "origin is required") String origin,
      @NotBlank(message = "destination is required") String destination,
      String preference) {
}

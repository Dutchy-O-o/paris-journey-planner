package com.journeyplanner.api;

import com.journeyplanner.service.NoRouteException;
import com.journeyplanner.service.StationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Translates domain/validation errors into RFC 7807 problem responses. */
@RestControllerAdvice
public class ApiExceptionHandler {

   @ExceptionHandler(StationNotFoundException.class)
   public ProblemDetail handleStationNotFound(StationNotFoundException ex) {
      return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
   }

   @ExceptionHandler(NoRouteException.class)
   public ProblemDetail handleNoRoute(NoRouteException ex) {
      return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
   }

   @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
   public ProblemDetail handleBadRequest(Exception ex) {
      String detail = (ex instanceof MethodArgumentNotValidException manve)
            ? manve.getBindingResult().getAllErrors().stream()
                  .findFirst().map(e -> e.getDefaultMessage()).orElse("Invalid request")
            : ex.getMessage();
      return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
   }
}

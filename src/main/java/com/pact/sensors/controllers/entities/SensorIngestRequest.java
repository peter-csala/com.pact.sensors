package com.pact.sensors.controllers.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class SensorIngestRequest {
    final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @NotNull(message = "must be provided in the following format: " + DATE_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FORMAT, timezone = "GMT")
    Date observedAt;

    @NotNull(message = "must be provided and it should be a hyphened UUID v4")
    UUID deviceId;

    @NotNull(message = "must be provided")
    Integer measuredValue;

    @NotEmpty(message = "must be provided and should not be empty")
    String measureUnit;
}

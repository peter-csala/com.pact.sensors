package com.pact.sensors.services.entities;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class SensorMetric {

    Date aggregatedFrom;

    Date aggregatedTill;

    String aggregateFunction;

    String aggregatedField;

    double aggregatedValue;

    long aggregatedDeviceCount;

    long aggregatedSensorRecordCount;
}

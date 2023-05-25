package com.pact.sensors.services;

import com.pact.sensors.services.entities.SensorMetric;

import java.util.Date;
import java.util.UUID;

public interface SensorMetricService {
    SensorMetric calculateAvgSensorDataForAllDevices(String sensorName, Date from, Date till);
    SensorMetric calculateAvgSensorDataForSpecificDevice(String sensorName, UUID deviceId, Date from, Date till);
}

package com.pact.sensors.services;

import com.pact.sensors.controllers.entities.SensorIngestRequest;
import com.pact.sensors.repositories.entities.SensorData;

public interface SensorIngestService {
    SensorData ingest (String sensorName, SensorIngestRequest metric);
}

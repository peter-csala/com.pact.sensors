package com.pact.sensors.services;

import com.pact.sensors.controllers.entities.SensorIngestRequest;
import com.pact.sensors.helpers.TimeProvider;
import com.pact.sensors.repositories.SensorDataRepository;
import com.pact.sensors.repositories.entities.SensorData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.temporal.ChronoUnit;

@Component
public class SimpleSensorIngestService implements SensorIngestService {

    @Autowired
    SensorDataRepository sensorRepository;

    @Autowired
    TimeProvider timeProvider;

    public SensorData ingest(String sensorName, SensorIngestRequest metric) {

        var roundedNow = timeProvider.getNow().truncatedTo(ChronoUnit.SECONDS);
        var databaseModel = SensorData.builder()
                .observedAt(metric.getObservedAt())
                .receivedAt(Date.from(roundedNow))
                .deviceId(metric.getDeviceId())
                .measureName(sensorName)
                .measuredValue(metric.getMeasuredValue())
                .measureUnit(metric.getMeasureUnit())
                .build();

        return sensorRepository.save(databaseModel);
    }
}

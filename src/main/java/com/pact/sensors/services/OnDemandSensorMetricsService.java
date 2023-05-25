package com.pact.sensors.services;

import com.pact.sensors.repositories.SensorDataRepository;
import com.pact.sensors.repositories.entities.SensorData;
import com.pact.sensors.services.entities.SensorMetric;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class OnDemandSensorMetricsService implements SensorMetricService {

    @Autowired
    SensorDataRepository repository;

    @Override
    public SensorMetric calculateAvgSensorDataForAllDevices(String sensorName, Date from, Date till) {

        var sensorData = repository.findAllByMeasureName(sensorName, from, till);
        var metricBuilder = calculateAverage(sensorData);

        return metricBuilder
                .aggregatedField(sensorName)
                .aggregatedFrom(from)
                .aggregatedTill(till)
                .build();
    }

    @Override
    public SensorMetric calculateAvgSensorDataForSpecificDevice(String sensorName, UUID deviceId, Date from, Date till) {

        var sensorData = repository.findAllByMeasureNameAndDeviceId(sensorName, deviceId, from, till);
        var metricBuilder = calculateAverage(sensorData);

        return metricBuilder
                .aggregatedField(sensorName)
                .aggregatedFrom(from)
                .aggregatedTill(till)
                .build();
    }

    private SensorMetric.SensorMetricBuilder calculateAverage(List<SensorData> sensorData) {

        var deviceCount = sensorData.stream()
                .map(SensorData::getDeviceId)
                .distinct()
                .count();

        var measuredValuesAverage = sensorData.stream()
                .mapToInt(SensorData::getMeasuredValue)
                .average()
                .orElse(Double.NaN);

        return SensorMetric.<Double>builder()
                .aggregateFunction("avg")
                .aggregatedValue(measuredValuesAverage)
                .aggregatedDeviceCount(deviceCount)
                .aggregatedSensorRecordCount(sensorData.size());
    }
}

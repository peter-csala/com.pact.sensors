package com.pact.sensors.services;

import com.pact.sensors.repositories.SensorDataRepository;
import com.pact.sensors.repositories.entities.SensorData;
import com.pact.sensors.services.entities.SensorMetric;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnDemandSensorMetricsServiceTest {

    @Mock
    SensorDataRepository repository;

    @InjectMocks
    OnDemandSensorMetricsService sut;

    //_____________________________________
    //|calculateAvgSensorDataForAllDevices|
    //_____________________________________

    @Test
    void GivenNoRelatedSensorRecord_WhenICallCalculateAvgSensorDataForAllDevices_ThenItProducesTheCorrectMetric() {
        //Arrange
        String measureName = "wind speed";
        var from = Date.from(Instant.parse("2023-05-22T00:00:00Z"));
        var till = Date.from(Instant.parse("2023-05-23T00:00:00Z"));

        when(repository.findAllByMeasureName(measureName, from, till))
                .thenReturn(Collections.emptyList());

        //Act
        var actual = sut.calculateAvgSensorDataForAllDevices(measureName, from, till);

        //Assert
        var expected = SensorMetric.builder()
                .aggregatedFrom(from)
                .aggregatedTill(till)
                .aggregatedField(measureName)
                .aggregateFunction("avg")
                .aggregatedValue(Double.NaN)
                .aggregatedDeviceCount(0L)
                .aggregatedSensorRecordCount(0L)
                .build();

        assertEquals(expected, actual);
    }

    @Test
    void GivenASingleDeviceWithMultipleRecordsForASingleSensor_WhenICallCalculateAvgSensorDataForAllDevices_ThenItProducesTheCorrectMetric() {
        //Arrange
        String measureName = "humidity";
        String measureUnit = "percentage";
        var from = Date.from(Instant.parse("2023-05-22T00:00:00Z"));
        var till = Date.from(Instant.parse("2023-05-23T00:00:00Z"));

        var recordBuilder = SensorData.builder()
                .deviceId(UUID.randomUUID())
                .measureName(measureName)
                .measureUnit(measureUnit);

        var sensorRecord1 = recordBuilder
                .observedAt(Date.from(Instant.parse("2023-05-22T15:10:00Z")))
                .measuredValue(70)
                .build();

        var sensorRecord2 = recordBuilder
                .observedAt(Date.from(Instant.parse("2023-05-22T15:15:00Z")))
                .measuredValue(75)
                .build();


        when(repository.findAllByMeasureName(measureName, from, till))
                .thenReturn(List.of(sensorRecord1, sensorRecord2));

        //Act
        var actual = sut.calculateAvgSensorDataForAllDevices(measureName, from, till);

        //Assert
        var expected = SensorMetric.builder()
                .aggregatedFrom(from)
                .aggregatedTill(till)
                .aggregatedField(measureName)
                .aggregateFunction("avg")
                .aggregatedValue(72.5)
                .aggregatedDeviceCount(1L)
                .aggregatedSensorRecordCount(2L)
                .build();

        assertEquals(expected, actual);
    }

    //NOTE: There is no need to have a test case for
    // single device with multiple sensors related records
    // since the repository filters for a single measure

    @Test
    void GivenMultipleDevicesWithMultipleRecordsForASingleSensors_WhenICallCalculateAvgSensorDataForAllDevices_ThenItProducesTheCorrectMetric() {
        //Arrange
        String measureName = "temperature";
        String measureUnit = "celsius";
        UUID device1 = UUID.randomUUID();
        UUID device2 = UUID.randomUUID();
        var from = Date.from(Instant.parse("2023-05-22T00:00:00Z"));
        var till = Date.from(Instant.parse("2023-05-23T00:00:00Z"));

        var sensorRecord1 = SensorData.builder()
                .deviceId(device1)
                .measureName(measureName)
                .measureUnit(measureUnit)
                .observedAt(Date.from(Instant.parse("2023-05-22T15:10:00Z")))
                .measuredValue(38)
                .build();

        var sensorRecord2 = SensorData.builder()
                .deviceId(device1)
                .measureName(measureName)
                .measureUnit(measureUnit)
                .observedAt(Date.from(Instant.parse("2023-05-22T15:15:00Z")))
                .measuredValue(40)
                .build();

        var sensorRecord3 = SensorData.builder()
                .deviceId(device2)
                .measureName(measureName)
                .measureUnit(measureUnit)
                .observedAt(Date.from(Instant.parse("2023-05-22T15:15:00Z")))
                .measuredValue(42)
                .build();


        when(repository.findAllByMeasureName(measureName, from, till))
                .thenReturn(List.of(sensorRecord1, sensorRecord2, sensorRecord3));

        //Act
        var actual = sut.calculateAvgSensorDataForAllDevices(measureName, from, till);

        //Assert
        var expected = SensorMetric.builder()
                .aggregatedFrom(from)
                .aggregatedTill(till)
                .aggregatedField(measureName)
                .aggregateFunction("avg")
                .aggregatedValue(40)
                .aggregatedDeviceCount(2L)
                .aggregatedSensorRecordCount(3L)
                .build();

        assertEquals(expected, actual);
    }

    //NOTE: There is no need to have a test case for
    // multiple devices with multiple sensors related records
    // since the repository filters for a single measure

    //_________________________________________
    //|calculateAvgSensorDataForSpecificDevice|
    //_________________________________________

    @Test
    void GivenNoRelatedSensorRecord_WhenICallCalculateAvgSensorDataForSpecificDevice_ThenItProducesTheCorrectMetric() {
        //Arrange
        String measureName = "wind speed";
        UUID deviceId = UUID.randomUUID();
        var from = Date.from(Instant.parse("2023-05-22T00:00:00Z"));
        var till = Date.from(Instant.parse("2023-05-23T00:00:00Z"));

        when(repository.findAllByMeasureNameAndDeviceId(measureName, deviceId, from, till))
                .thenReturn(Collections.emptyList());

        //Act
        var actual = sut.calculateAvgSensorDataForSpecificDevice(measureName,deviceId, from, till);

        //Assert
        var expected = SensorMetric.builder()
                .aggregatedFrom(from)
                .aggregatedTill(till)
                .aggregatedField(measureName)
                .aggregateFunction("avg")
                .aggregatedValue(Double.NaN)
                .aggregatedDeviceCount(0L)
                .aggregatedSensorRecordCount(0L)
                .build();

        assertEquals(expected, actual);
    }

    @Test
    void GivenMultipleSensorRecord_WhenICallCalculateAvgSensorDataForSpecificDevice_ThenItProducesTheCorrectMetric() {
        //Arrange
        String measureName = "wind speed";
        UUID deviceId = UUID.randomUUID();
        var from = Date.from(Instant.parse("2023-05-22T00:00:00Z"));
        var till = Date.from(Instant.parse("2023-05-23T00:00:00Z"));

        var recordBuilder = SensorData.builder()
                .deviceId(deviceId)
                .measureName(measureName)
                .measureUnit("km/h");

        var sensorRecord1 = recordBuilder
                .observedAt(Date.from(Instant.parse("2023-05-22T15:10:00Z")))
                .measuredValue(70)
                .build();

        var sensorRecord2 = recordBuilder
                .observedAt(Date.from(Instant.parse("2023-05-22T15:15:00Z")))
                .measuredValue(75)
                .build();

        var sensorRecord3 = recordBuilder
                .observedAt(Date.from(Instant.parse("2023-05-22T15:20:00Z")))
                .measuredValue(73)
                .build();

        when(repository.findAllByMeasureNameAndDeviceId(measureName, deviceId, from, till))
                .thenReturn(List.of(sensorRecord1, sensorRecord2, sensorRecord3));

        //Act
        var actual = sut.calculateAvgSensorDataForSpecificDevice(measureName,deviceId, from, till);

        //Assert
        var expected = SensorMetric.builder()
                .aggregatedFrom(from)
                .aggregatedTill(till)
                .aggregatedField(measureName)
                .aggregateFunction("avg")
                .aggregatedValue(72.66666666666667)
                .aggregatedDeviceCount(1L)
                .aggregatedSensorRecordCount(3L)
                .build();

        assertEquals(expected, actual);
    }
}


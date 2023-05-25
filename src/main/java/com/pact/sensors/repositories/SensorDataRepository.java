package com.pact.sensors.repositories;

import com.pact.sensors.repositories.entities.SensorData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    @Query(nativeQuery = true,
            value = "SELECT * FROM SENSOR_DATA WHERE measure_name = ?1 AND device_id = ?2 AND client_observed > ?3 AND client_observed < ?4")
    List<SensorData> findAllByMeasureNameAndDeviceId(String measureName, UUID deviceId, Date from, Date till);

    @Query(nativeQuery = true,
            value = "SELECT * FROM SENSOR_DATA WHERE measure_name = ?1 AND client_observed > ?2 AND client_observed < ?3")
    List<SensorData> findAllByMeasureName(String measureName, Date from, Date till);
}

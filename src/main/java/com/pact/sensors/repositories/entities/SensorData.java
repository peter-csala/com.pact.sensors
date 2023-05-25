package com.pact.sensors.repositories.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="sensor_data")
@Builder
@Data
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    Long id;

    @Column(name="client_observed")
    Date observedAt;

    @Column(name = "server_received")
    Date receivedAt;

    @Column(name="device_id")
    UUID deviceId;

    @Column(name="measure_name")
    String measureName;

    @Column(name="measure_integer_value")
    Integer measuredValue;

    @Column(name="measure_unit")
    String measureUnit;
}
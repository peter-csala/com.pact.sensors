package com.pact.sensors.controllers;

import com.pact.sensors.controllers.entities.SensorIngestRequest;
import com.pact.sensors.services.SensorIngestService;
import com.pact.sensors.services.SensorMetricService;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequestMapping("/api/v1/sensors")
@RestController
public class SensorController {
    final static String RELATION_NAME_FOR_SINGLE_DEVICE_METRIC = "single-device-metric";
    final static String RELATION_NAME_FOR_ALL_DEVICES_METRIC = "all-devices-metric";

    @Autowired
    SensorIngestService ingestService;

    @Autowired
    SensorMetricService metricsService;

@PostMapping("/{sensor-name}")
ResponseEntity<?> ingestSensorData(
        @PathVariable(name="sensor-name") String sensorName,
        @Valid @RequestBody SensorIngestRequest ingestRequest) {

    //Compute
    var serviceModel = ingestService.ingest(sensorName, ingestRequest);

    //Generate Response
    //Calculate surrounding days for observed at
    var calendar = Calendar.getInstance();
    calendar.setTime(serviceModel.getObservedAt());
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    var from = calendar.getTime();
    calendar.add(Calendar.DATE, 1);
    var till = calendar.getTime();

    //Generate links
    var linkSingle = createLinkForGetMetric(sensorName, from, till, Optional.of(ingestRequest.getDeviceId()), LinkRelation.of(RELATION_NAME_FOR_SINGLE_DEVICE_METRIC));
    var linkAll = createLinkForGetMetric(sensorName, from, till, Optional.empty(), LinkRelation.of(RELATION_NAME_FOR_ALL_DEVICES_METRIC));
    var responseModel = EntityModel.of(serviceModel, linkSingle, linkAll);

    return ResponseEntity
            .created(responseModel.getRequiredLink(RELATION_NAME_FOR_SINGLE_DEVICE_METRIC).toUri())
            .body(responseModel);
}

@GetMapping("/{sensor-name}/metrics/{metric-type}")
ResponseEntity<?> getASensorMetricForADateRangeForADevice(
        @PathVariable(name="sensor-name") String sensorName,
        @PathVariable(name="metric-type") String metricType,
        @RequestParam("from") @Valid @DateTimeFormat(pattern="yyyy-MM-dd") Date from,
        @RequestParam("till") @Valid @DateTimeFormat(pattern="yyyy-MM-dd") Date till,
        @RequestParam(name="device-id",required = false) Optional<UUID> deviceId)  {

    //Extra input data validation
    if (!"avg".equalsIgnoreCase(metricType)) {
        throw createCVE("metric-type", 1, metricType, String.class, "currently supports only 'avg'");
    }

    if (from.compareTo(till) > 0) {
        throw createCVE("till", 3, till, Date.class,"should be greater than from");
    }

    //Generate Response
    if (deviceId.isEmpty()) {
        var serviceModel = metricsService.calculateAvgSensorDataForAllDevices(sensorName, from, till);
        var linkAll = createLinkForGetMetric(serviceModel.getAggregatedField(), from, till, deviceId, IanaLinkRelations.SELF);
        return ResponseEntity.ok().body(EntityModel.of(serviceModel, linkAll));
    }

    var serviceModel = metricsService.calculateAvgSensorDataForSpecificDevice(sensorName, deviceId.get(), from, till);
    var linkSingle = createLinkForGetMetric(serviceModel.getAggregatedField(), from, till, deviceId, IanaLinkRelations.SELF);
    var linkAll = createLinkForGetMetric(serviceModel.getAggregatedField(), from, till, Optional.empty(), LinkRelation.of(RELATION_NAME_FOR_ALL_DEVICES_METRIC));
    return ResponseEntity.ok().body(EntityModel.of(serviceModel, linkSingle, linkAll));
}

    //TODO: Separate this logic from the controller
    <T> ConstraintViolationException createCVE(
            String parameterName,
            int parameterIndex,
            T parameter,
            Class<T> parameterType,
            String violationMessage) {

        //NOTE: here the method name does not matter
        final var propertyPath = PathImpl.createPathFromString("");
        propertyPath.addParameterNode(parameterName, parameterIndex);

        var constraintViolation = ConstraintViolationImpl.forParameterValidation(
                violationMessage,
                Collections.emptyMap(),
                Collections.emptyMap(),
                violationMessage,
                parameterType,
                parameter,
                null,
                parameter,
                propertyPath,
                null,
                null,
                null);
        return new ConstraintViolationException(Set.of(constraintViolation));
    }

    //TODO: fix its fragileness
    Link createLinkForGetMetric(String measureName, Date from, Date till, Optional<UUID> deviceId, LinkRelation relation) {
        var builder = linkTo(methodOn(SensorController.class)
                .getASensorMetricForADateRangeForADevice(measureName,"avg", from, till, deviceId));

        //Workaround to remove "{&device-id}" template from the url
        //Caution this solution is super-fragile
        var link = builder.toString();
        if(deviceId.isEmpty()) {
            link = link.replaceAll("\\{&device-id}", "");
        }
        return Link.of(link, relation);
    }
}

package com.pact.sensors.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.pact.sensors.controllers.entities.SensorIngestRequest;
import com.pact.sensors.helpers.TimeProvider;
import com.pact.sensors.repositories.SensorDataRepository;
import com.pact.sensors.repositories.entities.SensorData;

import lombok.SneakyThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SensorControllerIT {

    @MockBean
    TimeProvider timeProvider;

    @MockBean
    SensorDataRepository repository;

    @Captor
    ArgumentCaptor<SensorData> sensorDataCaptor;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @SneakyThrows
    @Test
    void GivenAValidIngestionRequest_WhenISendTheRequest_ThenItReturnsWithTheCreatedAndEntityAndRelatedLinks() {
        //Arrange
        String measureName = "windSpeed";
        String measureUnit = "km/h";
        int measuredValue = 10;
        UUID deviceId = UUID.fromString("461dbbfb-eb31-4377-9616-2ca2595aec52");

        Date observedAt = Date.from(Instant.parse("2023-05-22T15:15:00Z"));
        Instant now = Instant.parse("2023-05-22T15:17:31.76521Z");
        Instant nowRounded = Instant.parse("2023-05-22T15:17:31Z");
        Date receivedAtRounded = Date.from(nowRounded);

        var ingestRequest = SensorIngestRequest.builder()
                .deviceId(deviceId)
                .measuredValue(measuredValue)
                .measureUnit(measureUnit)
                .observedAt(observedAt)
                .build();

        var sensorData = SensorData.builder()
                .id(127L)
                .observedAt(observedAt)
                .receivedAt(receivedAtRounded)
                .deviceId(deviceId)
                .measureName(measureName)
                .measuredValue(measuredValue)
                .measureUnit(measureUnit)
                .build();

        when(timeProvider.getNow()).thenReturn(now);
        when(repository.save(sensorDataCaptor.capture()))
                .thenReturn(sensorData);

        //Act
        ResultActions actions = mockMvc.perform(post("/api/v1/sensors/" + measureName)
                .content(objectMapper.writeValueAsString(ingestRequest))
                .contentType(MediaType.APPLICATION_JSON));

        //Assert - Response
        var expectedBody = "{\"id\":127," +
                "\"observedAt\":\"2023-05-22T15:15:00.000+00:00\"," +
                "\"receivedAt\":\"2023-05-22T15:17:31.000+00:00\"," +
                "\"deviceId\":\"461dbbfb-eb31-4377-9616-2ca2595aec52\"," +
                "\"measureName\":\"windSpeed\"," +
                "\"measuredValue\":10," +
                "\"measureUnit\":\"km/h\"," +
                "\"_links\":{\"single-device-metric\":{" +
                "\"href\":\"http://localhost/api/v1/sensors/windSpeed/metrics/avg?from=2023-05-22&till=2023-05-23&device-id=461dbbfb-eb31-4377-9616-2ca2595aec52\"}," +
                "\"all-devices-metric\":{\"href\":\"http://localhost/api/v1/sensors/windSpeed/metrics/avg?from=2023-05-22&till=2023-05-23\"}}}";
        var expectedUrl = "http://localhost/api/v1/sensors/windSpeed/metrics/avg?from=2023-05-22&till=2023-05-23&device-id=461dbbfb-eb31-4377-9616-2ca2595aec52";
        actions
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("location", expectedUrl))
                .andExpect(MockMvcResultMatchers.header().string("content-type", "application/hal+json"))
                .andExpect(MockMvcResultMatchers.content().json(expectedBody));

        //Assert - Mock
        var expectedArgument = SensorData.builder()
                .observedAt(observedAt)
                .receivedAt(receivedAtRounded)
                .deviceId(deviceId)
                .measureName(measureName)
                .measuredValue(measuredValue)
                .measureUnit(measureUnit)
                .build(); //id is NOT set

        assertEquals(expectedArgument, sensorDataCaptor.getValue());
    }

    @SneakyThrows
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidIngestInputs")
    void GivenAnInvalidIngestionRequest_WhenISendTheRequest_ThenItReturnsWithProperStatusCodeAndAProblemObject(
            String testCase, String ingestRequest, String expectedError
    ) {
        //Arrange

        //Act
        ResultActions actions = mockMvc.perform(post("/api/v1/sensors/temperature")
                .content(ingestRequest)
                .contentType(MediaType.APPLICATION_JSON));

        //Assert
        var expectedBody = "{\"title\":\"Invalid data has been provided, please correct it\"," +
                "\"detail\":\"" +  expectedError + "\"}";
        actions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.header().string("content-type", "application/json"))
                .andExpect(MockMvcResultMatchers.content().json(expectedBody));
    }

    static Stream<Arguments> invalidIngestInputs() {
        return Stream.of(
                arguments("observedAt is misspelled",
                        "{\"observedAtt\":\"2023-05-21T09:45:32\",\"deviceId\":\"5d114dfe-70b0-47d8-84a2-955ea3d93199\",\"measuredValue\":27,\"measureUnit\": \"celcius\"}",
                        "['observedAt' parameter must be provided in the following format: yyyy-MM-dd'T'HH:mm:ss]"),
                arguments("measureUnit is empty and measuredValue is missing",
                        "{\"observedAt\":\"2023-05-21T09:45:32\",\"deviceId\":\"5d114dfe-70b0-47d8-84a2-955ea3d93199\",\"measureUnit\": \"\"}",
                        "['measureUnit' parameter must be provided and should not be empty, 'measuredValue' parameter must be provided]")
        );
    }

    @SneakyThrows
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidMetricRequestUrls")
    void GivenAnInvalidQueryRequest_WhenISendTheRequest_ThenItReturnsWithProperStatusCodeAndAProblemObject(
            String testCase, String requestUrl, String expectedError

    ) {
        //Arrange

        //Act
        ResultActions actions = mockMvc.perform(get(requestUrl));

        //Assert
        var expectedBody = "{\"title\":\"Invalid data has been provided, please correct it\"," +
                "\"detail\":\"" +  expectedError + "\"}";
        actions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.header().string("content-type", "application/json"))
                .andExpect(MockMvcResultMatchers.content().json(expectedBody));
    }

    static Stream<Arguments> invalidMetricRequestUrls() {
        return Stream.of(
                arguments("metric type is not allowed",
                        "/api/v1/sensors/temperature/metrics/mdn?from=2023-05-23&till=2023-05-24",
                        "['metric-type' parameter currently supports only 'avg']"),
                arguments("from greater than till",
                        "/api/v1/sensors/temperature/metrics/avg?from=2023-05-24&till=2023-05-23",
                        "['till' parameter should be greater than from]")
        );
    }
}

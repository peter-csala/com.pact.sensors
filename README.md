## Project description
This application exposes two APIs:
1. To periodically report/ingest/submit weather sensors' observed values 
```none
POST /api/v1/sensors/temperature
```
2. To query/fetch/monitor metrics of the weather sensors data over time 
```
GET /api/v1/sensors/windSpeed/metrics/avg?from=2023-05-22&till=2023-05-23
```
- Here you can optionally filter for a specific device
```
GET /api/v1/...till=2023-05-23&device-id=461dbbfb-eb31-4377-9616-2ca2595aec52
```
- Sample calls can be found in the `WeatherSensorService.postman_collection.json` file


## Assumptions
- The application is write-heavy
    - The averages are calculated during read operations on demand
    - *If it were read-heavy a rolling average technique could be used at each write*
- The time skew between the client devices' and server's wall clock are negligible
    - Device are always online (*there is no offline data collection*)
- The number of devices and their data submission frequency are considered low
    - A single node can handle the ingest requests

## Known limitations
- Time related
    - There is no timezone handling (every Date is in GMT)
    - Consumer of this API can't define against which timestamp field they want to perform aggregation
- Measure related
    - Only integers can be used as measured value
    - Measure unit is not checked (*see Known bugs section*)
- Validation related
    - Request data validation is inconsistent
      - Ingest can return multiple problems
      - Metric request fails fast on unsupported metric type 
    - Devices can report extreme values
      - There is no range validation based on the measure unit

## Known issues
- Because the measure unit is not checked that's why incorrect result could be retrieved
  - For example:
    - Device 1 reports temperature in Fahrenheit
    - Device 2 reports temperature in Celsius
    - Device 3 reports temperature in Kelvin
    - There is no data conversion before aggregation  

## Improvement ideas
- Database
  - Use "real" database engine, not H2's file database
  - Add data retention policy
- Ingest
  - Add predefined sensor schemas 
  - Allow batch requests 
  - Introduce late event concept 
- Metrics
  - Supporting other metrics than average
  - Request multiple metrics at once
  - Externalize metric calculation
  - Utilize cache   

# GovData Dashboard (Backend Challenge)

A Spring Boot web application written in Java that provides a dashboard showing how many data sets each German federal ministry has made available on [GovData.de](https://www.govdata.de/). The data is fetched from the official [GovData.de API](https://www.govdata.de/ckan/api/3/) and displayed in a table. The table is sorted by the total number of published data sets in descending order. The total number of data sets of each ministry is computed by counting and summing the number of data sets per ministry and their subordinate agencies.

## Tooling

- JDK 21
- Gradle with Groovy syntax
- Spring Boot 3.3.4
  - with [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
  - [Thymeleaf](https://www.thymeleaf.org/) as a template engine
  - Lombok
  - JUnit 5

## Usage

### Building the application

`./gradlew clean build`

### Creating an executable Jar

Create the jar file e.g. `./app/build/libs/govdata-dashboard-0.0.1-SNAPSHOT.jar` by executing the following command:

- `./gradlew bootJar`

### Running the application

Note: Execution requires Java 21.

The application can be run in two ways:

- From existing jar: `java -jar ./app/build/libs/govdata-dashboard-0.0.1-SNAPSHOT.jar`
- From source: `./gradlew bootRun` on Linux resp. `.\gradlew.bat bootRun` on Windows

The application automatically reads the default departments from the `src/main/resources/departments.json` at startup. If you want to change this, you can either directly change the file (or its content) or pass the departments as a command line argument: `java -jar ./app/build/libs/govdata-dashboard-0.0.1-SNAPSHOT.jar --departments.file=my-custom-departments.json` or `./gradlew bootRun --args="--departments.file=my-custom-departments.json"`.
The JSON file must conform to the structure of `src/main/resources/departments.json`. Any adjustments to the file will require an application restart.

### Accessing the dashboard

To access the dashboard, open a browser and navigate to `http://localhost:8080/api/v0.1/dashboard`.
To access the backend API, open a browser and navigate to `http://localhost:8080/api/v0.1/dashboard/json`.

### Example JSON output

- name = German name of the ministry
- dataSetCount = total number of data sets of the ministry and its subordinate agencies

```json
[
  {
    "name": "Bundesministerium des Innern und Heimat",
    "dataSetCount": 4087
  }
]
```

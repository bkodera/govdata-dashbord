package govdata.dashboard.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import govdata.dashboard.model.CKANOrganizationResponse;
import govdata.dashboard.model.DepartmentDto;
import govdata.dashboard.service.DashboardService;
import govdata.dashboard.service.DepartmentService;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
class DashboardServiceTest {

  DashboardService dashboardService;

  @Autowired
  DepartmentService departmentService;

  MockWebServer mockWebServer;

  ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setUp() throws Exception {
    this.mockWebServer = new MockWebServer();
    this.mockWebServer.url("/");
    this.mockWebServer.start();
    String baseUrl = String.format(
      "http://localhost:%s",
      this.mockWebServer.getPort()
    );
    WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
    this.dashboardService =
      new DashboardService(webClient, this.departmentService);
  }

  @AfterEach
  void tearDown() throws Exception {
    this.mockWebServer.shutdown();
  }

  @Test
  void testComputesCorrectDatasetCounts() throws IOException {
    CKANOrganizationResponse mockResponseBody =
      this.readOrganizationResponse("organizations_valid.json");
    this.mockWebServer.enqueue(
        new MockResponse()
          .setBody(this.mapper.writeValueAsString(mockResponseBody))
          .addHeader("Content-Type", "application/json")
      );

    StepVerifier
      .create(this.dashboardService.computeDepartmentDataSetCounts())
      .expectNextMatches(dto ->
        dto.equals(new DepartmentDto("Auswärtiges Amt", 20))
      )
      .expectNextMatches(dto ->
        dto.equals(new DepartmentDto("Bundesministerium der Justiz", 15))
      )
      .verifyComplete();
  }

  @Test
  void testExpectsErrorForInvalidResponse() throws IOException {
    CKANOrganizationResponse mockResponseBody =
      this.readOrganizationResponse("organizations_invalid.json");
    this.mockWebServer.enqueue(
        new MockResponse()
          .setBody(this.mapper.writeValueAsString(mockResponseBody))
          .addHeader("Content-Type", "application/json")
      );

    StepVerifier
      .create(this.dashboardService.computeDepartmentDataSetCounts())
      .expectError();
  }

  private CKANOrganizationResponse readOrganizationResponse(String fileName)
    throws IOException {
    return this.mapper.readValue(
        ResourceUtils.getFile("classpath:" + fileName),
        CKANOrganizationResponse.class
      );
  }
}

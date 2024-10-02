package govdata.dashboard.test;

import govdata.dashboard.service.DepartmentService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest(
  properties = "departments.file=classpath:departments_valid.json"
)
@ActiveProfiles("test")
class DepartmentServiceValidTest {

  @Autowired
  DepartmentService departmentService;

  @Test
  void testHasCorrectNumberOfDepartments() {
    Integer departments = this.departmentService.getDepartmentNames().size();
    Assertions.assertThat(departments).isEqualTo(2);
  }

  @Test
  void testNoError() {
    StepVerifier
      .create(this.departmentService.getError())
      .expectNextCount(0)
      .verifyComplete();
  }

  @Test
  void testRecognizesValidSubordinate() {
    Boolean isValidSubordinate =
      this.departmentService.isValidSubordinateOrDepartment(
          "Bundesamt für Justiz"
        );
    Assertions.assertThat(isValidSubordinate).isTrue();
  }

  @Test
  void testRecognizesValidDepartment() {
    Boolean isValidDepartment =
      this.departmentService.isValidSubordinateOrDepartment("Auswärtiges Amt");
    Assertions.assertThat(isValidDepartment).isTrue();
  }

  @Test
  void testGetCorrectDepartmentForSubordinate() {
    String department =
      this.departmentService.toDepartment("Bundesamt für Justiz");
    Assertions.assertThat(department).matches("Bundesministerium der Justiz");
  }
}

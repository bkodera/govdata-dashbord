package govdata.dashboard.test;

import govdata.dashboard.service.DepartmentService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest(
  properties = "departments.file=classpath:departments_invalid.json"
)
@ActiveProfiles("test")
class DepartmentServiceInvalidTest {

  @Autowired
  DepartmentService departmentService;

  @Test
  void testNoDepartmentsFound() {
    Integer departments = this.departmentService.getDepartmentNames().size();
    Assertions.assertThat(departments).isZero();
  }

  @Test
  void testHasError() {
    StepVerifier.create(this.departmentService.getError()).verifyError();
  }

  @Test
  void testHasNoRecognizedSubordinate() {
    Boolean isValidSubordinate =
      this.departmentService.isValidSubordinateOrDepartment(
          "Bundesamt für Justiz"
        );
    Assertions.assertThat(isValidSubordinate).isFalse();
  }
}

package govdata.dashboard.service;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import govdata.dashboard.model.department.Department;
import govdata.dashboard.model.department.SubOrdinate;
import govdata.dashboard.util.CheckedFunctionHelper;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DepartmentService {

  @Value("${departments.file}")
  private String departmentsFile;

  private Map<String, String> subordinateToDepartmentMap;

  @Getter
  private List<String> departmentNames;

  @Getter
  private Mono<String> error = Mono.empty();

  @PostConstruct
  public void loadDefaultDepartments() {
    ObjectMapper mapper = new ObjectMapper();

    List<Department> departments = Mono
      // propagates error if there is a problem with the resource
      .fromCallable(this::getDepartmentsFromFile)
      .map(CheckedFunctionHelper.wrap(mapper::readTree))
      .filter(tree -> tree.has("departments"))
      .map(tree -> tree.get("departments"))
      .filter(JsonNode::isArray)
      .switchIfEmpty(this.createError())
      .map(
        CheckedFunctionHelper.wrap(node ->
          mapper.readValue(node.traverse(), Department[].class)
        )
      )
      .map(Arrays::asList)
      .doOnError(this.handleError())
      .doOnSuccess(d -> log.info("Loaded {} departments", d.size()))
      .onErrorReturn(new ArrayList<>())
      .block();

    this.departmentNames = departments.stream().map(Department::name).toList();
    this.subordinateToDepartmentMap =
      this.mapSubordinateToDepartment(departments);
  }

  private InputStream getDepartmentsFromFile() throws IOException {
    File file = ResourceUtils.getFile(this.departmentsFile);
    if (file.exists()) {
      log.info("Reading external file: {}", file.getAbsolutePath());
      return Files.newInputStream(file.toPath());
    }
    log.info("Reading classpath resource: {}", this.departmentsFile);
    return new ClassPathResource(this.departmentsFile).getInputStream();
  }

  private Mono<JsonNode> createError() {
    return Mono.error(
      new IOException(
        String.format(
          "Invalid departments file: %s. Missing \"departments\" key or \"departments\" is not an array",
          this.departmentsFile
        )
      )
    );
  }

  private Consumer<? super Throwable> handleError() {
    return e -> {
      String errorMessage = String.format(
        "Failed to load departments: %s",
        e.getMessage()
      );
      this.error = Mono.error(new RuntimeException(errorMessage));
      log.error(errorMessage);
    };
  }

  public Boolean isValidSubordinateOrDepartment(String departmentName) {
    return (
      this.departmentNames.contains(departmentName) ||
      this.subordinateToDepartmentMap.containsKey(departmentName)
    );
  }

  /**
   * If the organization is a subordinate, find its department name.
   * If the organization already is a known department, return self.
   * @param organization any department or any subordinate
   * @return Matching Department name
   */
  public String toDepartment(String organization) {
    return this.subordinateToDepartmentMap.getOrDefault(
        organization,
        organization
      );
  }

  private Map<String, String> mapSubordinateToDepartment(
    List<Department> departments
  ) {
    return departments
      .stream()
      .flatMap(department ->
        department
          .subOrdinates()
          .stream()
          .collect(toMap(SubOrdinate::name, subOrdinate -> department.name()))
          .entrySet()
          .stream()
      )
      .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}

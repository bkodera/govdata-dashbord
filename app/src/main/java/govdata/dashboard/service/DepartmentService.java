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

  /**
   * Tries to read departments from a given JSON resource at startup.
   * On success, departments are provided as a list to process by other services.
   * On failure, an error Mono is created to be queried.
   */
  @PostConstruct
  public void loadDefaultDepartments() {
    ObjectMapper mapper = new ObjectMapper();

    List<Department> departments = Mono
      // propagates error if there is a problem with the resource
      .fromCallable(this::getDepartmentsFromFile)
      // try to read the file content as JSON
      .map(CheckedFunctionHelper.wrap(mapper::readTree))
      // check if the parsed JSON contains a top-level "departments" field
      .filter(tree -> tree.has("departments"))
      // extract the "departments" field
      .map(tree -> tree.get("departments"))
      // check if the "departments" field actually is an array
      .filter(JsonNode::isArray)
      // if there is any error while parsing the content, create an error Mono
      .switchIfEmpty(this.createError())
      // try to read the departments and map them to Department objects
      .map(
        CheckedFunctionHelper.wrap(node ->
          mapper.readValue(node.traverse(), Department[].class)
        )
      )
      .map(Arrays::asList)
      // Catch any error and store it for later
      .doOnError(this.handleError())
      .doOnSuccess(d -> log.info("Loaded {} departments", d.size()))
      // finally, create an empty list if there is an error
      .onErrorReturn(new ArrayList<>())
      .block();

    this.departmentNames = departments.stream().map(Department::name).toList();
    this.subordinateToDepartmentMap =
      this.mapSubordinateToDepartment(departments);
  }

  /**
   * Tries to read the departments JSON file. Supports reading the file as an
   * external resource (e.g. provided as a CLI parameter) or as the provided
   * classpath resource.
   * @return
   * @throws IOException
   */
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

  /**
   * Checks if a department is matching a known subordinate or a department.
   * @param departmentName Departments or subordinate
   * @return
   */
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

  /**
   * Create a mapping from subordinates to their department.
   * @param departments The departments extracted from the departments file
   */
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

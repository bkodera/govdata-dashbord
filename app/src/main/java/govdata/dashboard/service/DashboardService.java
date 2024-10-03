package govdata.dashboard.service;

import govdata.dashboard.model.ckan.CKANOrganizationEntity;
import govdata.dashboard.model.ckan.CKANOrganizationResponse;
import govdata.dashboard.model.department.DepartmentDto;
import java.net.URI;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

/**
 * Service for communicating with the GovData CKAN API to retrieve organizations (ministries and subordinates) and their publications
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

  private final WebClient webClient;
  private final DepartmentService departmentService;

  /**
   * Computes the total number of data sets for each department resp. ministry by calling the CKAN organization_list endpoint with all details to retrieve the package count (=data set count) for each organization. It then filters out all unmatched departments and subordinates and finally sums up all subordinate's and department's package counts belonging to a matched departments. The results are stored as a Flux of DepartmentDto instances for each ministry. Sorting is done in descending order by total number of data sets.
   * @return Departments with name and data set count as a Flux
   */
  public Flux<DepartmentDto> computeDepartmentDataSetCounts() {
    // check if the departments service generated an error while processing the departments JSON file
    return this.departmentService.getError()
      // discard previous Mono only if there was no error and continue to make API GET request for organizations
      .then(this.requestOrganizations())
      // Propagate error message on request failure
      .flatMap(this.handleError())
      // extract organizations list from "result" field (only on success)
      .map(CKANOrganizationResponse::result)
      // turn the result list into flux
      .flatMapMany(Flux::fromIterable)
      // filter out unmatched resp. invalid organizations (departments and subordinates)
      .filter(this.isValidSubordinateOrDepartment())
      // create DTO with name and dataset counter
      .map(this.toDepartmentDto())
      // merge with missing departments from departments service (to ensure there is a DTO for each department)
      .mergeWith(this.defaultDepartmentNames())
      // group by name because there are duplicates
      .groupBy(DepartmentDto::name)
      // sum data set counts for all departments with the same name
      .flatMap(this.combineDuplicates())
      // sort items descending by data set count
      .sort(Comparator.comparingInt(DepartmentDto::dataSetCount).reversed())
      .doOnError(e -> log.error(e.getMessage()));
  }

  /**
   * Requests the organization list from the CKAN API.
   */
  private Mono<CKANOrganizationResponse> requestOrganizations() {
    return this.webClient.get()
      .uri(this.organizationUri())
      .retrieve()
      .bodyToMono(CKANOrganizationResponse.class);
  }

  /**
   * Creates an error with an error message if the response was not successful.
   */
  private Function<? super CKANOrganizationResponse, ? extends Mono<CKANOrganizationResponse>> handleError() {
    return res ->
      res.success().booleanValue()
        ? Mono.just(res)
        : Mono.error(
          new RuntimeException(
            "Failed to load departments from CKAN API: " + res.error().message()
          )
        );
  }

  /**
   * Creates the CKAN request URI with the necessary query param.
   */
  private Function<UriBuilder, URI> organizationUri() {
    return uriBuilder ->
      uriBuilder
        .path("organization_list")
        .queryParam("all_fields", true)
        .build();
  }

  /**
   * Checks if an organization is a matching subordinate or department.
   */
  private Predicate<? super CKANOrganizationEntity> isValidSubordinateOrDepartment() {
    return org ->
      this.departmentService.isValidSubordinateOrDepartment(org.name());
  }

  /**
   * Creates a Flux of DepartmentDto objects from all known departments with initial dataset count 0.
   */
  private Flux<DepartmentDto> defaultDepartmentNames() {
    return Flux
      .fromIterable(this.departmentService.getDepartmentNames())
      .map(DepartmentDto::new);
  }

  /**
   * Adds up dataset counts for all departments with the same name.
   */
  private Function<GroupedFlux<String, DepartmentDto>, Publisher<DepartmentDto>> combineDuplicates() {
    return group ->
      group.reduce((d1, d2) ->
        new DepartmentDto(d1.name(), d1.dataSetCount() + d2.dataSetCount())
      );
  }

  /**
   * Creates a DepartmentDto from a CKANOrganizationEntity. Subordinate organizations are mapped to their departments.
   */
  private Function<CKANOrganizationEntity, DepartmentDto> toDepartmentDto() {
    return org -> {
      String department = this.departmentService.toDepartment(org.name());
      return new DepartmentDto(department, org.packageCount());
    };
  }
}

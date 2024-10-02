package govdata.dashboard.controller;

import govdata.dashboard.model.department.DepartmentDto;
import govdata.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v0.1/dashboard")
@RequiredArgsConstructor
public class DashboardRestController {

  private final DashboardService dashboardService;

  @GetMapping("/json")
  public ResponseEntity<Flux<DepartmentDto>> getAllDataSetsByFederalMinistry() {
    return ResponseEntity.ok(
      this.dashboardService.computeDepartmentDataSetCounts()
    );
  }
}

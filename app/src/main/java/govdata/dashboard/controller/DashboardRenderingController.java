package govdata.dashboard.controller;

import govdata.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/api/v0.1/dashboard")
@RequiredArgsConstructor
public class DashboardRenderingController {

  private final DashboardService dashboardService;

  @GetMapping("/")
  public Mono<Rendering> renderAllDataSetsByFederalMinistry() {
    return this.dashboardService.computeDepartmentDataSetCounts()
      .collectList()
      .map(datasets ->
        // Use the templates/index.html and pass the data to the template
        Rendering.view("index").modelAttribute("datasets", datasets).build()
      )
      .onErrorResume(error ->
        Mono.just(
          // Render the error page templates/error.html if there is an error
          Rendering
            .view("error")
            .modelAttribute("errorMessage", error.getMessage())
            .build()
        )
      );
  }
}

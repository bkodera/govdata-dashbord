package govdata.dashboard.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DashboardConfiguration {

  @Value("${govdata.ckan.url}")
  private String baseUrl;

  @Bean
  WebClient webClient() {
    return WebClient.builder().baseUrl(this.baseUrl).build();
  }
}

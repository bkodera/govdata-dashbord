package govdata.dashboard.model.ckan;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CKANOrganizationEntity(
  @JsonProperty("display_name") String name,
  @JsonProperty("package_count") Integer packageCount
) {}

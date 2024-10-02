package govdata.dashboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CKANOrganizationEntity(
  @JsonProperty("display_name") String displayName,
  @JsonProperty("package_count") Integer packageCount
) {}

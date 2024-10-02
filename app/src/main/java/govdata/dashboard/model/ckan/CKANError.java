package govdata.dashboard.model.ckan;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CKANError(@JsonProperty("__type") String type, String message) {}

package govdata.dashboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CKANError(@JsonProperty("__type") String type, String message) {}

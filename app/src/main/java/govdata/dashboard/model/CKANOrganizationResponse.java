package govdata.dashboard.model;

import java.util.List;

public record CKANOrganizationResponse(
  Boolean success,
  List<CKANOrganizationEntity> result,
  CKANError error
) {}

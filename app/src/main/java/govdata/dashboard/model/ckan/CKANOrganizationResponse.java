package govdata.dashboard.model.ckan;

import java.util.List;

public record CKANOrganizationResponse(
  Boolean success,
  List<CKANOrganizationEntity> result,
  CKANError error
) {}

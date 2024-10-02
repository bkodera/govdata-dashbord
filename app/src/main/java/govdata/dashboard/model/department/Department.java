package govdata.dashboard.model.department;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;

public record Department(
  String name,
  @JsonProperty("subordinates")
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  List<SubOrdinate> subOrdinates
) {}

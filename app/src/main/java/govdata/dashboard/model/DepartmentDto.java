package govdata.dashboard.model;

public record DepartmentDto(String name, Integer dataSetCount) {
  public DepartmentDto(String name) {
    this(name, 0);
  }
}

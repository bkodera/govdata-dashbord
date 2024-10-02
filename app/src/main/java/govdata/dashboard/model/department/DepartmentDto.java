package govdata.dashboard.model.department;

public record DepartmentDto(String name, Integer dataSetCount) {
  public DepartmentDto(String name) {
    this(name, 0);
  }
}

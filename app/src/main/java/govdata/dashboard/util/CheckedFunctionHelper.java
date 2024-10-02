package govdata.dashboard.util;

import java.util.function.Function;

public class CheckedFunctionHelper {

  private CheckedFunctionHelper() {}

  /**
   * Helper function that wraps a checked function into a runtime exception. Allows exception handling in a functional way.
   * @param <T> Input type
   * @param <R> Output type
   * @param checkedFunction Any function that throws a checked exception
   */
  public static <T, R> Function<T, R> wrap(
    CheckedFunction<T, R> checkedFunction
  ) {
    return t -> {
      try {
        return checkedFunction.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}

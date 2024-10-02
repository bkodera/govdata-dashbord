package govdata.dashboard.util;

import java.util.function.Function;

public class CheckedFunctionHelper {

  private CheckedFunctionHelper() {}

  /**
   * Helper function that executes a checked function and wraps any checked exception into a runtime exception. This allows exception handling in a functional way.
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

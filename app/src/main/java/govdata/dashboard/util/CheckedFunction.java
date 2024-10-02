package govdata.dashboard.util;

/**
 * Extension of the regular function signature that additionally allows throwing a checked exception.
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {
  R apply(T t) throws Exception;
}

package utils;

public class ParameterTypeUtils {
    public static Object typeConversion(Class clazz, String value) {
        if (clazz == Integer.class) {
            return Integer.valueOf(value);
        } else if (clazz == Double.class) {
            return Double.valueOf(value);
        } else if (clazz == Long.class) {
            return Long.valueOf(value);
        } else {
            return value;
        }
    }
}

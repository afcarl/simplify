package simplify;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;

import simplify.vm.types.SmaliClassInstance;

public class SmaliClassUtils {

    private static Map<String, Class<?>> PrimitiveTypes;
    static {
        PrimitiveTypes = new HashMap<String, Class<?>>(8);
        PrimitiveTypes.put("I", Integer.TYPE);
        PrimitiveTypes.put("S", Short.TYPE);
        PrimitiveTypes.put("J", Long.TYPE);
        PrimitiveTypes.put("B", Byte.TYPE);
        PrimitiveTypes.put("D", Double.TYPE);
        PrimitiveTypes.put("F", Float.TYPE);
        PrimitiveTypes.put("Z", Boolean.TYPE);
        PrimitiveTypes.put("C", Character.TYPE);
    }

    public static boolean isPrimitiveType(String type) {
        return PrimitiveTypes.containsKey(getBaseClass(type));
    }

    public static String smaliClassToJava(String className) {
        Class<?> type = PrimitiveTypes.get(className);
        if (type != null) {
            return type.getName();
        }

        if (className.equals("?")) {
            // Probably lazy and didn't determine type.
            // This will probably result in a Class.forName exception.
            return className;
        }

        // Java doesn't like Lcom/package/class;, it wants "com.package.class"
        return className.substring(1, className.length() - 1).replaceAll("/", ".");
    }

    private static String getBaseClass(String className) {
        // Remove any array qualifiers, e.g. [[B (2d byte array) becomes B
        return className.replace("[", "");
    }

    public static String smaliPrimitiveToJavaWrapper(String className) {
        Class<?> primitiveClass = PrimitiveTypes.get(getBaseClass(className));

        return ClassUtils.primitiveToWrapper(primitiveClass).getName();
    }

    public static String javaClassToSmali(String className) {
        for (String key : PrimitiveTypes.keySet()) {
            String typeName = PrimitiveTypes.get(key).getName();
            if (typeName.equals(className)) {
                return key;
            }
        }

        return "L" + className.replaceAll("\\.", "/") + ";";
    }

    public static String getValueType(Object value) {
        String result = null;
        if (value instanceof SmaliClassInstance) {
            result = ((SmaliClassInstance) value).getType();
        } else {
            result = value.getClass().getCanonicalName();
        }

        return result;
    }

    public static boolean isImmutableClass(String smaliClassName) {
        if (smaliClassName.startsWith("[")) {
            // Array contents can be mutated, regardless of class.
            return false;
        }

        if (smaliClassName.equals("?")) {
            // Unknown type. Was probably lazy somewhere and didn't get implied type.
            return false;
        }

        if (smaliClassName.equals("Ljava/lang/String;")) {
            return true;
        }

        if (isPrimitiveType(smaliClassName)) {
            return true;
        }

        String className = smaliClassName.replaceAll("/", ".");
        try {
            Class<?> clazz = ClassUtils.getClass(className, false);
            return ClassUtils.isPrimitiveOrWrapper(clazz);

        } catch (ClassNotFoundException e) {
        }

        return false;
    }

}

package net.morph.operator;

import java.lang.annotation.Annotation;

import org.apache.commons.lang3.ClassUtils;

/**
 * Checks for types universally known to be immutable.
 */
public class DefaultImmutableChecker extends ImmutableChecker {
    @Override
    protected boolean isImmutable(Object object) {
        if (object == null) {
            return true;
        }
        if (object instanceof String) {
            return true;
        }
        if (object instanceof Enum) {
            return true;
        }
        if (object instanceof Annotation) {
            return true;
        }
        Class<?> cls = object.getClass();
        if (cls.isPrimitive()) {
            return true;
        }
        if (ClassUtils.wrapperToPrimitive(cls) != null) {
            return true;
        }
        return false;
    }
}

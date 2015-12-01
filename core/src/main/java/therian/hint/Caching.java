package therian.hint;

import org.apache.commons.lang3.ClassUtils;

import therian.Operation;
import therian.Operator;
import therian.Reusable;
import therian.Therian;
import therian.TherianContext;
import therian.Operator.Phase;

/**
 * Caching {@link Hint}. Note that to turn caching off for the entire context, it's more performant to use
 * {@link TherianContext#putContext(Class, Object)}.
 */
//@formatter:off
public enum Caching implements Hint {
     /**
      * Specifies no caching.
      */
    NONE,

     /**
      * Specifies to cache per requested {@link Operation}, per {@link TherianContext}.
      */
    CONTEXT,

     /**
      * Specifies to:
      * <ul>
      * <li>cache per requested {@link Operation}, per {@link TherianContext}</li>
      * <li>cache supporting {@link Operator} per generic operation type, per
      * {@link Therian}</li>
      * </ul>
      */
    ALL;
//@formatter:on

    /**
     * Test whether an object is reusable, i.e. cacheable. By default, everything is considered reusable, so to mark an
     * item as *not* being reusable one would declare the {@link Reusable} annotation with the desired operator phases.
     * i.e., if the item is never reusable, it should be declared as:
     *
     * <pre>
     * &#64;Reusable({ })
     * </pre>
     *
     * It is considered nonsensical that the evaluation of a given operation/operator be reusable, without the
     * corresponding support check being likewise reusable; therefore specifying {@link Phase#EVALUATION} is understood
     * to imply {@link Phase#SUPPORT_CHECK} whether or not it is explicitly included.
     *
     * @param o
     * @param phase
     * @return whether
     * @since 0.2
     */
    public static boolean isReusable(Object o, Operator.Phase phase) {
        for (Class<?> c : ClassUtils.hierarchy(o.getClass())) {
            if (c.isAnnotationPresent(Reusable.class)) {
                for (Phase p : c.getAnnotation(Reusable.class).value()) {
                    if (p.compareTo(phase) >= 0) {
                        return true;
                    }
                }
                // stop on the nearest ancestor bearing the annotation:
                return false;
            }
        }
        return true;
    }

    /**
     * Learn whether {@code this} implies the specified {@link Caching} {@code behavior}.
     * 
     * @param behavior
     * @return {@code boolean}
     */
    public boolean implies(Caching behavior) {
        if (behavior == this) {
            return true;
        }
        return behavior != NONE && this == ALL;
    }

    @Override
    public Class<? extends Hint> getType() {
        return Caching.class;
    }
}

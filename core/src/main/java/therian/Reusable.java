/*
 *  Copyright the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package therian;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.ClassUtils;

import therian.Operator.Phase;

/**
 * Marks an {@link Operator} or {@link Operation} as being reusable, i.e. cacheable. By default, everything is
 * considered reusable, so to mark an item as *not* being reusable one would declare the {@link Reusable} annotation
 * with the desired operator phases. i.e., if the item is never reusable, it should be declared as:
 * 
 * <pre>
 * @Reusable({ })
 * </pre>
 * 
 * It is considered nonsensical that the evaluation of a given operation/operator be reusable, without the corresponding
 * support check being likewise reusable; therefore specifying {@link Phase#EVALUATION} is understood to imply
 * {@link Phase#SUPPORT_CHECK} whether or not it is explicitly included.
 * 
 * @since 0.2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Reusable {

    public static class Checker {
        private Checker() {
        }

        public boolean canReuse(Object o, Operator.Phase phase) {
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
    }

    public static final Checker CHECKER = new Checker();

    /**
     * Phases for which the item is reusable.
     * 
     * @return {@link Phase}[]
     */
    Operator.Phase[] value() default { Operator.Phase.SUPPORT_CHECK, Operator.Phase.EVALUATION };
}

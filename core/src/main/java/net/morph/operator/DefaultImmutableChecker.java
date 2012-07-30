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

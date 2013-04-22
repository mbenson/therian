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
package therian.buildweaver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * For use by Therian's build-weaver plugin module. Marks an Operator for inclusion in Operators#standard(). When used
 * on a (non-abstract) class, a default constructor will be used to instantiate the class. When used on a static field,
 * that field's value will be used. When used on a static method, the return value from an invocation of that method
 * will be used.
 */
@Documented
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
public @interface StandardOperator {
}

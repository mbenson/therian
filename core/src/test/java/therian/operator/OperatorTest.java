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
package therian.operator;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;

import therian.Operator;
import therian.Operator.DependsOn;
import therian.Therian;
import therian.TherianContext;
import therian.TherianModule;

/**
 * Abstract {@link Operator} test.
 */
public abstract class OperatorTest {

    protected TherianContext therianContext;

    @Before
    public void setup() throws Exception {
        therianContext = Therian.usingModules(modules()).context();
    }

    protected TherianModule[] modules() throws Exception {
        final TherianModule module = module();
        if (module == null) {
            return new TherianModule[0];
        }
        @SuppressWarnings("rawtypes")
        final Set<Class<? extends Operator>> operatorTypesPresent = new HashSet<Class<? extends Operator>>();
        @SuppressWarnings("rawtypes")
        final Set<Class<? extends Operator>> operatorTypesNeeded = new HashSet<Class<? extends Operator>>();

        @SuppressWarnings("rawtypes")
        class DependencyManager {

            void handle(Operator<?> operator) {
                final Class<? extends Operator> type = operator.getClass();
                operatorTypesPresent.add(type);
                operatorTypesNeeded.remove(type);

                Class<?> c = type;
                while (c != null) {
                    handle(c.getAnnotation(DependsOn.class));
                    c = c.getSuperclass();
                }
            }

            void handle(DependsOn deps) {
                if (deps != null) {
                    for (Class<? extends Operator> type : deps.value()) {
                        handle(type);
                    }
                }
            }

            void handle(Class<? extends Operator> type) {
                if (!operatorTypesPresent.contains(type)) {
                    operatorTypesNeeded.add(type);
                }
                handle(type.getAnnotation(DependsOn.class));
            }

        }

        final DependencyManager dependencyManager = new DependencyManager();
        for (Operator<?> op : module.getOperators()) {
            dependencyManager.handle(op);
        }

        if (operatorTypesNeeded.isEmpty()) {
            return new TherianModule[] { module };
        }

        final Operator<?>[] deps = new Operator[operatorTypesNeeded.size()];
        int index = 0;
        for (@SuppressWarnings("rawtypes")
        Class<? extends Operator> dep : operatorTypesNeeded) {
            deps[index++] = dep.newInstance();
        }
        return new TherianModule[] { module, TherianModule.create().withOperators(deps) };
    }

    protected TherianModule module() {
        return null;
    }
}

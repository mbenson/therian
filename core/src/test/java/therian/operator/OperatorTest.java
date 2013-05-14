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

import org.junit.Before;

import therian.Operator;
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
        return module == null ? new TherianModule[0] : new TherianModule[] { TherianModule
            .expandingDependencies(module) };
    }

    protected TherianModule module() {
        return null;
    }
}

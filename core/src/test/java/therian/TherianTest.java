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

import static org.junit.Assert.assertThat;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;
import org.slf4j.Logger;

import therian.behavior.LoggingMode;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

public class TherianTest {

    @Test(expected = NullPointerException.class)
    public void testConvertUsingNullModules() {
        Therian.usingModules((TherianModule[]) null);
    }

    @Test
    public void testLoggingMode() {
        final Therian therian = Therian.standard();
        final Logger logger = therian.getLogger(getClass());
        final TestLogger testLogger = TestLoggerFactory.getTestLogger(getClass());

        logger.trace("foo");
        assertThat(testLogger.getLoggingEvents(), IsIterableContainingInOrder.contains(LoggingEvent.trace("foo")));

        testLogger.clear();
        therian.withBehaviors(LoggingMode.NORMAL);
        logger.trace("foo");
        assertThat(testLogger.getLoggingEvents(), IsIterableContainingInOrder.contains(LoggingEvent.trace("foo")));

        testLogger.clear();
        therian.withBehaviors(LoggingMode.PANIC);
        logger.trace("foo");
        assertThat(testLogger.getLoggingEvents(), IsIterableContainingInOrder.contains(LoggingEvent.debug("foo")));
    }

}

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
package therian.cdi.internal;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;
import therian.cdi.Mapper;
import therian.operator.copy.PropertyCopier.Mapping.Value;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class TherianExtensionTest {
    @Module
    @Classes(cdi = true, value = FromToMapper.class)
    public WebApp app() {
        return new WebApp();
    }

    @Inject
    private FromToMapper mapper;

    @Test
    public void copy() {
        final From from = new From();
        from.setInteger(1234);
        from.setString("degfebfek");

        final To to = mapper.to(from);

        assertEquals(1234, to.getCount());
        assertEquals("degfebfek", to.getDescription());
    }

    @Mapper
    public interface FromToMapper {
        @Value(from = "integer", to = "count")
        @Value(from = "string", to = "description")
        To to(From from);
    }

    public static class From {
        private int integer;
        private String string;

        public int getInteger() {
            return integer;
        }

        public void setInteger(final int integer) {
            this.integer = integer;
        }

        public java.lang.String getString() {
            return string;
        }

        public void setString(final String string) {
            this.string = string;
        }
    }

    public static class To {
        private int count;
        private String description;

        public int getCount() {
            return count;
        }

        public void setCount(final int count) {
            this.count = count;
        }

        public java.lang.String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }
    }
}

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
package therian.position.relative.propertymethod.it;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import therian.position.relative.Property;

public class PropertyMethodWeaverTest {

    public static class TestBean {

        private int foo;
        private boolean bar;
        private String baz;

        public int getFoo() {
            return foo;
        }

        public void setFoo(int foo) {
            this.foo = foo;
        }

        public boolean isBar() {
            return bar;
        }

        public void setBar(boolean bar) {
            this.bar = bar;
        }

        public String getBaz() {
            return baz;
        }

        public void setBaz(String baz) {
            this.baz = baz;
        }
    }

    @Test
    public void testAccessors() {
        assertEquals("Property foo", Property.at(TestBean::getFoo).toString());
        assertEquals("Optional Property foo", Property.optional(TestBean::getFoo).toString());
        assertEquals("Property bar", Property.<TestBean, Boolean> at(TestBean::isBar).toString());
        assertEquals("Optional Property bar", Property.<TestBean, Boolean> optional(TestBean::isBar).toString());
        assertEquals("Property baz", Property.<TestBean, String> at(TestBean::getBaz).toString());
        assertEquals("Optional Property baz", Property.<TestBean, String> optional(TestBean::getBaz).toString());
    }

    @Test
    public void testMutators() {
        assertEquals("Property foo", Property.at(TestBean::setFoo).toString());
        assertEquals("Optional Property foo", Property.optional(TestBean::setFoo).toString());
        assertEquals("Property bar", Property.<TestBean, Boolean> at(TestBean::setBar).toString());
        assertEquals("Optional Property bar", Property.<TestBean, Boolean> optional(TestBean::setBar).toString());
        assertEquals("Property baz", Property.<TestBean, String> at(TestBean::setBaz).toString());
        assertEquals("Optional Property baz", Property.<TestBean, String> optional(TestBean::setBaz).toString());
    }
}

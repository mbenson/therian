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
package therian.operator.copy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Before;
import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.operation.Copy;
import therian.operator.OperatorTest;
import therian.operator.convert.NOPConverter;
import therian.position.Position;
import therian.testfixture.Author;
import therian.testfixture.Employee;
import therian.util.Positions;
import uelbox.IterableELResolver;

/**
 *
 */
public class ElementCopierTest extends OperatorTest {
    public interface LocalTypes {
        public static final TypeLiteral<List<Employee>> LIST_OF_EMPLOYEE = new TypeLiteral<List<Employee>>() {};
        public static final TypeLiteral<Set<Employee>> SET_OF_EMPLOYEE = new TypeLiteral<Set<Employee>>() {};
        public static final TypeLiteral<List<Author>> LIST_OF_AUTHOR = new TypeLiteral<List<Author>>() {};
    }

    private Employee[] employees;

    @Before
    public void setupData() {
        employees =
            new Employee[] { new Employee("Bob", "Cratchit"), new Employee("Jacob", "Marley"),
                new Employee("Ebenezer", "Scrooge") };
    }

    @SuppressWarnings("unchecked")
    private <T> T[] fill(T[] array) {
        final Class<?> elementType = array.getClass().getComponentType();
        for (int i = 0; i < array.length; i++) {
            try {
                array[i] = (T) elementType.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return array;
    }

    @Override
    protected TherianModule module() {
        return TherianModule.create()
            .withOperators(new ElementCopier(), new ConvertingCopier(), new NOPConverter(), new BeanCopier())
            .withELResolvers(new IterableELResolver());
    }

    @Test
    public void testArrayToList() {
        final Position.Readable<List<Employee>> target =
            Positions.readOnly(LocalTypes.LIST_OF_EMPLOYEE, Arrays.asList(new Employee[3]));
        assertTrue(therianContext.evalSuccess(Copy.to(target, Positions.readOnly(employees))));
        for (int i = 0, sz = employees.length; i < sz; i++) {
            assertSame(employees[i], target.getValue().get(i));
        }
    }

    @Test
    public void testArrayToPopulatedList() {
        final List<Employee> targetList = Arrays.asList(fill(new Employee[3]));
        final List<Employee> saveList = new ArrayList<>(targetList);
        final Position.Readable<List<Employee>> target = Positions.readOnly(LocalTypes.LIST_OF_EMPLOYEE, targetList);
        assertTrue(therianContext.evalSuccess(Copy.to(target, Positions.readOnly(employees))));
        assertArrayEquals(employees, target.getValue().toArray(new Employee[3]));
        for (int i = 0, sz = targetList.size(); i < sz; i++) {
            final Employee emp = targetList.get(i);
            assertSame(saveList.get(i), emp);
            assertEquals(employees[i], emp);
        }
    }

    @Test(expected = OperationException.class)
    public void testArrayToSmallerList() {
        final Position.Readable<List<Employee>> target =
            Positions.readOnly(LocalTypes.LIST_OF_EMPLOYEE, Arrays.asList(new Employee[1]));
        therianContext.eval(Copy.to(target, Positions.readOnly(employees)));
    }

    @Test
    public void testSetToList() {
        final Position.Readable<List<Employee>> target =
            Positions.readOnly(LocalTypes.LIST_OF_EMPLOYEE, Arrays.asList(new Employee[3]));
        final Set<Employee> sourceValue = new LinkedHashSet<>(Arrays.asList(employees));
        assertTrue(therianContext.evalSuccess(Copy.to(target,
            Positions.readOnly(LocalTypes.SET_OF_EMPLOYEE, sourceValue))));
        int index = 0;
        for (Employee emp : sourceValue) {
            assertSame(emp, target.getValue().get(index++));
        }
    }

    @Test
    public void testListToArray() {
        final Position.Readable<Employee[]> target = Positions.readOnly(new Employee[3]);
        assertTrue(therianContext.evalSuccess(Copy.to(target,
            Positions.readOnly(LocalTypes.LIST_OF_EMPLOYEE, Arrays.asList(employees)))));
        for (int i = 0; i < employees.length; i++) {
            assertSame(employees[i], target.getValue()[i]);
        }
    }

    @Test(expected = OperationException.class)
    public void testListToSmallerArray() {
        final Position.Readable<Employee[]> target = Positions.readOnly(new Employee[1]);
        therianContext.eval(Copy.to(target, Positions.readOnly(LocalTypes.LIST_OF_EMPLOYEE, Arrays.asList(employees))));
    }

    @Test
    public void testListToPopulatedArray() {
        final Employee[] targetArray = fill(new Employee[3]);
        final Employee[] saveArray = ArrayUtils.clone(targetArray);
        final Position.Readable<Employee[]> target = Positions.readOnly(targetArray);
        assertTrue(therianContext.evalSuccess(Copy.to(target,
            Positions.readOnly(LocalTypes.LIST_OF_EMPLOYEE, Arrays.asList(employees)))));
        for (int i = 0; i < employees.length; i++) {
            assertSame(saveArray[i], targetArray[i]);
            assertEquals(employees[i], targetArray[i]);
        }
    }

    @Test
    public void testArrayToListWithTransformation() {
        final Position.Readable<List<Author>> target =
            Positions.readOnly(LocalTypes.LIST_OF_AUTHOR, Arrays.asList(new Author[3]));
        assertTrue(therianContext.evalSuccess(Copy.to(target, Positions.readOnly(employees))));
        for (int i = 0; i < employees.length; i++) {
            final Author author = target.getValue().get(i);
            assertNotNull(author);
            assertEquals(employees[i].getFirstName(), author.getFirstName());
            assertEquals(employees[i].getLastName(), author.getLastName());
            assertNull(author.getCompany());
            assertNull(author.getAddresses());
        }

    }

}

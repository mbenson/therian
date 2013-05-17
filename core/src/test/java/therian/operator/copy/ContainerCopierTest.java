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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import therian.OperationException;
import therian.TherianModule;
import therian.TypeLiteral;
import therian.operation.Copy;
import therian.operator.OperatorTest;
import therian.operator.convert.CopyingConverter;
import therian.operator.convert.DefaultCopyingConverter;
import therian.position.Position;
import therian.testfixture.Author;
import therian.testfixture.Book;
import therian.testfixture.Employee;
import therian.testfixture.MetasyntacticVariable;
import therian.testfixture.Person;
import therian.util.Positions;

/**
 *
 */
public class ContainerCopierTest extends OperatorTest {
    private interface LocalTypes {
        static final TypeLiteral<Set<Book>> SET_OF_BOOK = new TypeLiteral<Set<Book>>() {};
        static final TypeLiteral<List<Book>> LIST_OF_BOOK = new TypeLiteral<List<Book>>() {};
        static final TypeLiteral<List<Employee>> LIST_OF_EMPLOYEE = new TypeLiteral<List<Employee>>() {};
    }

    public static class Jerk implements Person {
        private final String lastName;
        private final String firstName;
        private final String middleName;

        public Jerk(String... name) {
            lastName = name.length > 0 ? name[0] : null;
            firstName = name.length > 1 ? name[1] : null;
            if (name.length < 3) {
                middleName = null;
            } else {
                middleName = StringUtils.join(Arrays.asList(name).subList(2, name.length), ' ');
            }
        }

        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public String getMiddleName() {
            return middleName;
        }

        @Override
        public String getLastName() {
            return lastName;
        }

    }

    private Book[] books;
    private Jerk[] jerks;

    @Before
    public void setupData() {
        MetasyntacticVariable[] values = MetasyntacticVariable.values();
        books = new Book[values.length];
        for (int i = 0; i < values.length; i++) {
            books[i] = new Book();
            books[i].setTitle(values[i].name());
            Author author = new Author();
            author.setFirstName("Neal");
            author.setLastName("Stephenson");
            books[i].setAuthor(author);
        }
        jerks =
            new Jerk[] { new Jerk("Morris", "Keith"), new Jerk("Hetson", "Greg"), new Jerk("Rogerson", "Roger"),
                new Jerk("Lehrer", "Keith", "Michael", "\"Lucky\"") };
    }

    protected TherianModule module() {
        return TherianModule.create().withOperators(new ContainerCopier.ToIterable(), new ContainerCopier.ToIterator(),
            new ContainerCopier.ToEnumeration(), new ContainerCopier.ToArray(), CopyingConverter.IMPLEMENTING_SET,
            CopyingConverter.IMPLEMENTING_LIST, new DefaultCopyingConverter(), new BeanCopier());
    }

    @Test
    public void testToList() {
        final Book[] targetElements = { new Book(), new Book(), new Book() };
        final List<Book> targetValue = new ArrayList<Book>(Arrays.asList(targetElements));

        final Position.Readable<List<Book>> target = Positions.readOnly(LocalTypes.LIST_OF_BOOK, targetValue);

        therianContext.eval(Copy.to(target, Positions.readOnly(books)));
        assertEquals(targetElements.length, targetValue.size());

        int index = 0;
        for (Book targetBook : targetValue) {
            assertSame(targetElements[index], targetBook);
            assertEquals("book " + index, books[index++], targetBook);
        }
    }

    @Test
    public void testToUnmodifiableList() {
        final Book[] targetElements = { new Book(), new Book(), new Book() };
        final List<Book> targetValue = Collections.unmodifiableList(new ArrayList<Book>(Arrays.asList(targetElements)));

        final Position.Readable<List<Book>> target = Positions.readOnly(LocalTypes.LIST_OF_BOOK, targetValue);

        therianContext.eval(Copy.to(target, Positions.readOnly(books)));
        assertEquals(targetElements.length, targetValue.size());

        int index = 0;
        for (Book targetBook : targetValue) {
            assertSame(targetElements[index], targetBook);
            assertEquals("book " + index, books[index++], targetBook);
        }
    }

    @Test
    public void testToSmallerSet() {
        final Book targetElement = new Book();
        final Set<Book> targetValue = new LinkedHashSet<Book>();
        targetValue.add(targetElement);
        final Position.Readable<Set<Book>> target = Positions.readOnly(LocalTypes.SET_OF_BOOK, targetValue);

        therianContext.eval(Copy.to(target, Positions.readOnly(books)));
        assertEquals(books.length, targetValue.size());
        assertSame(targetElement, targetValue.iterator().next());

        int index = 0;
        for (Book targetBook : targetValue) {
            assertEquals("book " + index, books[index++], targetBook);
        }
    }

    @Test(expected = OperationException.class)
    public void testToUnmodifiableSmallerSet() {
        therianContext.eval(Copy.to(Positions.readOnly(LocalTypes.SET_OF_BOOK, Collections.singleton(new Book())),
            Positions.readOnly(books)));
    }

    @Test
    public void testToUnmodifiableSmallerSetWithWritableTargetPosition() {
        final Book targetElement = new Book();
        final Position.ReadWrite<Set<Book>> target =
            Positions.readWrite(LocalTypes.SET_OF_BOOK, Collections.singleton(targetElement));

        therianContext.eval(Copy.to(target, Positions.readOnly(books)));
        assertEquals(books.length, target.getValue().size());
        assertSame(targetElement, target.getValue().iterator().next());
        assertArrayEquals(books, target.getValue().toArray());
    }

    @Test
    public void testSingletonToSet() {
        final Book targetElement = new Book();
        final Position.Readable<Set<Book>> target =
            Positions.readOnly(LocalTypes.SET_OF_BOOK, Collections.singleton(targetElement));
        therianContext.eval(Copy.to(target, Positions.readOnly(books[0])));
        assertSame(targetElement, target.getValue().iterator().next());
        assertEquals(books[0], targetElement);
    }

    @Test
    public void testSingletonToLargerList() {
        final Book[] targetElements = { new Book(), new Book(), new Book() };
        final List<Book> targetValue = new ArrayList<Book>(Arrays.asList(targetElements));
        final Position.Readable<List<Book>> target = Positions.readOnly(LocalTypes.LIST_OF_BOOK, targetValue);
        therianContext.eval(Copy.to(target, Positions.readOnly(books[0])));

        assertEquals(targetElements.length, targetValue.size());

        int index = 0;
        final Iterator<Book> targetValues = targetValue.iterator();
        assertSame(targetElements[index], targetValues.next());
        assertEquals(books[0], targetElements[index++]);

        final Book blank = new Book();
        assertSame(targetElements[index], targetValues.next());
        assertEquals(blank, targetElements[index++]);
        assertSame(targetElements[index], targetValues.next());
        assertEquals(blank, targetElements[index++]);
        assertFalse(targetValues.hasNext());
    }

    @Test
    public void testSingletonToEmptySet() {
        final LinkedHashSet<Book> targetValue = new LinkedHashSet<Book>();
        final Position.Readable<Set<Book>> target = Positions.readOnly(LocalTypes.SET_OF_BOOK, targetValue);
        therianContext.eval(Copy.to(target, Positions.readOnly(books[0])));
        assertSame(targetValue, target.getValue());
        assertEquals(1, targetValue.size());
        assertEquals(books[0], targetValue.iterator().next());
    }

    @Test(expected = OperationException.class)
    public void testSingletonToUnmodifiableEmptySet() {
        therianContext.eval(Copy.to(
            Positions.readOnly(LocalTypes.SET_OF_BOOK, Collections.unmodifiableSet(new HashSet<Book>())),
            Positions.readOnly(books[0])));
    }

    @Test
    public void testSingletonToUnmodifiableEmptySetWithWritableTargetPosition() {
        final Position.ReadWrite<Set<Book>> target =
            Positions.readWrite(LocalTypes.SET_OF_BOOK, Collections.unmodifiableSet(new HashSet<Book>()));
        therianContext.eval(Copy.to(target, Positions.readOnly(books[0])));
        assertEquals(1, target.getValue().size());
        assertSame(books[0], target.getValue().iterator().next());
    }

    @Test
    public void testTypeConversionToList() {
        final List<Employee> targetList = new ArrayList<Employee>();
        therianContext.eval(Copy.to(Positions.readOnly(LocalTypes.LIST_OF_EMPLOYEE, targetList),
            Positions.readOnly(jerks)));
        final Iterator<Employee> eachEmployee = targetList.iterator();

        for (final Iterator<Jerk> eachJerk = Arrays.asList(jerks).iterator(); eachJerk.hasNext();) {
            assertTrue(eachEmployee.hasNext());
            final Jerk jerk = eachJerk.next();
            final Employee employee = eachEmployee.next();
            assertEquals(jerk.getFirstName(), employee.getFirstName());
            assertNull(employee.getMiddleName());
            assertEquals(jerk.getLastName(), employee.getLastName());
        }
        assertFalse(eachEmployee.hasNext());
    }

    @Test
    public void testToArray() {
        assertFalse(
            "Cannot use ContainerCopier to copy to null target elements",
            therianContext.supports(Copy.to(
                Positions.readWrite(MetasyntacticVariable[].class,
                    new MetasyntacticVariable[MetasyntacticVariable.values().length]),
                Positions.readOnly(MetasyntacticVariable.values()))));
    }

    @Test
    public void testToNull() {
        // using a writable target would allow the ELCoercionConverter to convert the null source value to a null target value
        assertFalse(
            "Cannot use ContainerCopier to copy to read-only null target",
            therianContext.supports(Copy.to(Positions.readOnly(MetasyntacticVariable[].class, null),
                Positions.readOnly(MetasyntacticVariable.values()))));
    }
}

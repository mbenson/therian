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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.reflect.Typed;
import org.junit.Test;

import therian.OperatorDefinitionException;
import therian.TherianModule;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operator.OperatorTest;
import therian.operator.convert.DefaultCopyingConverter;
import therian.operator.convert.NOPConverter;
import therian.operator.copy.PropertyCopier.Mapping;
import therian.operator.copy.PropertyCopier.Mapping.Value;
import therian.operator.copy.PropertyCopier.Matching;
import therian.position.Position;
import therian.testfixture.Author;
import therian.testfixture.Book;
import therian.testfixture.Employee;
import therian.testfixture.IllustratedBook;
import therian.testfixture.Person;
import therian.util.Positions;

/**
 *
 */
public class PropertyCopierTest extends OperatorTest {
    @PropertyCopier.Mapping({ @Value(from = "firstName", to = "lastName"), @Value(from = "lastName", to = "firstName") })
    public static class InvertNames extends PropertyCopier<Person, Person> {
    }

    @PropertyCopier.Mapping({ @Value(to = "author") })
    public static class PersonToBook extends PropertyCopier<Person, Book> {
    }

    @PropertyCopier.Mapping({ @Value(from = "author") })
    public static class BookToPerson extends PropertyCopier<Book, Person> {
    }

    @PropertyCopier.Mapping({ @Value(from = "#{illustrator.firstName}", to = "firstName"),
        @Value(from = "#{illustrator.lastName}", to = "lastName") })
    public static class IllustratedBookToEmployee extends PropertyCopier<IllustratedBook, Employee> {
    }

    public static class Jerk implements Person {
        private String lastName;
        private String firstName;
        private String middleName;

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

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        
    }

    @PropertyCopier.Mapping({ @Value(from = "firstName", to = "lastName"), @Value(from = "lastName", to = "firstName") })
    @PropertyCopier.Matching("middleName")
    public void holdConfig() {
    }

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new NOPConverter(), new ConvertingCopier(),
            new DefaultCopyingConverter(), new InvertNames(), new PersonToBook(), new BookToPerson(),
            new IllustratedBookToEmployee(), jerkCopier());
    }

    private PropertyCopier<Jerk, Jerk> jerkCopier() {
        final Typed<Jerk> type = TypeUtils.wrap(Jerk.class);
        Method method;
        try {
            method = getClass().getMethod("holdConfig");
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        return PropertyCopier.getInstance(type, type, method.getAnnotation(Mapping.class),
            method.getAnnotation(Matching.class));
    }

    @Test
    public void testCopy() {
        final Author sourceValue = new Author();
        sourceValue.setFirstName("Barrence");
        sourceValue.setLastName("Whitfield");
        final Position.Readable<Author> source = Positions.readOnly(sourceValue);
        final Employee targetValue = new Employee();
        final Position.ReadWrite<Employee> target = Positions.readWrite(Employee.class, targetValue);
        assertTrue(therianContext.evalSuccess(Copy.to(target, source)));
        assertSame(targetValue, target.getValue());
        assertEquals(sourceValue.getFirstName(), target.getValue().getLastName());
        assertEquals(sourceValue.getLastName(), target.getValue().getFirstName());
    }

    @Test
    public void testConvert() {
        final Author sourceValue = new Author();
        sourceValue.setFirstName("Barrence");
        sourceValue.setLastName("Whitfield");
        final Position.Readable<Author> source = Positions.readOnly(sourceValue);
        final Position.ReadWrite<Employee> target = Positions.readWrite(Employee.class);
        assertTrue(therianContext.evalSuccess(Copy.to(target, source)));
        assertNotNull(target.getValue());
        assertEquals(sourceValue.getFirstName(), target.getValue().getLastName());
        assertEquals(sourceValue.getLastName(), target.getValue().getFirstName());
    }

    @Test
    public void testEmptySourceProperty() {
        final Position.Readable<Book> target = Positions.readOnly(new Book());
        assertTrue(therianContext.evalSuccess(Copy.to(target, Positions.readOnly(new Employee("Todd", "Bridges")))));
        assertNotNull(target.getValue().getAuthor());
        assertEquals("Bridges", target.getValue().getAuthor().getFirstName());
        assertEquals("Todd", target.getValue().getAuthor().getLastName());
    }

    @Test
    public void testEmptyTargetProperty() {
        final Position.Readable<Person> target = Positions.readOnly(Person.class, new Employee());
        final Book sourceValue = new Book();
        sourceValue.setAuthor(new Author());
        sourceValue.getAuthor().setFirstName("Gary");
        sourceValue.getAuthor().setLastName("Coleman");
        assertTrue(therianContext.evalSuccess(Copy.to(target, Positions.readOnly(sourceValue))));
        assertEquals("Coleman", target.getValue().getFirstName());
        assertEquals("Gary", target.getValue().getLastName());
    }

    @Test(expected = OperatorDefinitionException.class)
    public void testMissingAnnotation() {
        new PropertyCopier<Object, Object>() {};
    }

    @Test(expected = OperatorDefinitionException.class)
    public void testEmptyAnnotation() {

        @PropertyCopier.Mapping({})
        class PropertyCopierWithEmptyAnnotation extends PropertyCopier<Object, Object> {
        }
        new PropertyCopierWithEmptyAnnotation();
    }

    @Test(expected = OperatorDefinitionException.class)
    public void testInvalidAnnotation() {
        @PropertyCopier.Mapping({ @Value })
        class PropertyCopierWithInvalidAnnotation extends PropertyCopier<Object, Object> {

        }
        new PropertyCopierWithInvalidAnnotation();
    }

    @Test
    public void testExpressionMapping() {
        final IllustratedBook book = new IllustratedBook();
        final Person illustrator = new Person() {

            @Override
            public String getFirstName() {
                return "Shel";
            }

            @Override
            public String getMiddleName() {
                return null;
            }

            @Override
            public String getLastName() {
                return "Silverstein";
            }
        };
        book.setIllustrator(illustrator);
        final Employee employee = therianContext.eval(Convert.to(Employee.class, Positions.readOnly(book)));
        assertEquals(illustrator.getFirstName(), employee.getFirstName());
        assertEquals(illustrator.getLastName(), employee.getLastName());
    }

    @Test
    public void testFactoryMappingMatching() {
        final Jerk source = new Jerk("Johnny", "Bob", "Goldstein");
        final Jerk target = new Jerk("Keith", "Morris");

        therianContext.eval(Copy.to(Positions.readOnly(target), Positions.readOnly(source)));
        assertEquals(source.getFirstName(), target.getLastName());
        assertEquals(source.getMiddleName(), target.getMiddleName());
        assertEquals(source.getLastName(), target.getFirstName());
    }
}

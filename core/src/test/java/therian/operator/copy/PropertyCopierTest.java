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

import static org.junit.Assert.*;

import org.junit.Test;

import therian.OperatorDefinitionException;
import therian.TherianModule;
import therian.operation.Convert;
import therian.operation.Copy;
import therian.operator.OperatorTest;
import therian.operator.convert.DefaultCopyingConverter;
import therian.operator.convert.NOPConverter;
import therian.operator.copy.PropertyCopier.Mapping.Value;
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

    @PropertyCopier.Mapping({ @Value(from = "", to = "author") })
    public static class PersonToBook extends PropertyCopier<Person, Book> {
    }

    @PropertyCopier.Mapping({ @Value(from = "author", to = "") })
    public static class BookToPerson extends PropertyCopier<Book, Person> {
    }

    @PropertyCopier.Mapping({ @Value(from = "#{illustrator.firstName}", to = "firstName"),
        @Value(from = "#{illustrator.lastName}", to = "lastName") })
    public static class IllustratedBookToEmployee extends PropertyCopier<IllustratedBook, Employee> {
    }

    @Override
    protected TherianModule module() {
        return TherianModule.create().withOperators(new NOPConverter(), new ConvertingCopier(),
            new DefaultCopyingConverter(), new InvertNames(), new PersonToBook(), new BookToPerson(),
            new IllustratedBookToEmployee());
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
}

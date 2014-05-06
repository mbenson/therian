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
package therian.position.relative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Before;
import org.junit.Test;

import therian.position.Position;
import therian.testfixture.Address;
import therian.testfixture.Author;
import therian.testfixture.Book;
import therian.testfixture.Employee;
import therian.testfixture.Library;
import therian.testfixture.Person;
import therian.util.Positions;

public class ExpressionTest {
    private Library library;
    private Book book1;

    @Before
    public void setup() {
        book1 = new Book();
        book1.setTitle("Charlie and the Chocolate Factory");
        final Author author = new Author();
        author.setFirstName("Roald");
        author.setLastName("Dahl");
        Address address0 = new Address();
        address0.setAddressline1("33 Wonka Way");
        Address address1 = new Address();
        address1.setAddressline1("666 Witch Way");

        author.setAddresses(Arrays.asList(address0, address1));

        book1.setAuthor(author);

        library = new Library();
        library.getTaggedBooks().put(book1.getTitle(), book1);

        final Employee librarian = new Employee("Adrian", "Pennino");
        library.setPersons(new Person[] { author, librarian });
    }

    @Test
    public void testProperty() {
        final Position.Readable<Book> book = Positions.readOnly(book1);

        final RelativePosition.ReadWrite<Book, String> titleOfBook = Expression.<String> at("title").of(book);
        assertEquals(String.class, titleOfBook.getType());
        assertEquals(book1.getTitle(), titleOfBook.getValue());
        final RelativePosition.ReadWrite<Book, String> firstNameOfAuthorOfBook =
            Expression.<String> at("author.firstName").of(book);
        assertEquals(String.class, firstNameOfAuthorOfBook.getType());
        assertEquals(book1.getAuthor().getFirstName(), firstNameOfAuthorOfBook.getValue());
        final RelativePosition.ReadWrite<Book, String> lastNameOfAuthorOfBook =
            Expression.<String> at("author.lastName").of(book);
        assertEquals(String.class, lastNameOfAuthorOfBook.getType());
        assertEquals(book1.getAuthor().getLastName(), lastNameOfAuthorOfBook.getValue());
        assertEquals(TypeUtils.parameterize(List.class, Address.class),
            Expression.<List<Address>> at("author.addresses").of(book).getType());
    }

    @Test
    public void testListIndex() {
        final Position.Readable<Author> author = Positions.readOnly(book1.getAuthor());
        final RelativePosition.ReadWrite<Author, Address> address0 = Expression.<Address> at("addresses[0]").of(author);
        assertEquals(Address.class, address0.getType());
        assertSame(author.getValue().getAddresses().get(0), address0.getValue());
        final RelativePosition.ReadWrite<Author, Address> address1 = Expression.<Address> at("addresses[1]").of(author);
        assertEquals(Address.class, address1.getType());
        assertSame(author.getValue().getAddresses().get(1), address1.getValue());

        final RelativePosition.ReadWrite<Author, Address> address2 =
            Expression.<Address> optional("addresses[2]").of(author);
        assertEquals(Address.class, address2.getType());
        assertNull(address2.getValue());
    }

    @Test
    public void testArrayIndex() {
        final Position.Readable<Library> libraryPosition = Positions.readOnly(library);
        final RelativePosition.ReadWrite<Library, Person> person0 =
            Expression.<Person> at("persons[0]").of(libraryPosition);
        assertEquals(Person.class, person0.getType());
        assertSame(library.getPersons()[0], person0.getValue());
        final RelativePosition.ReadWrite<Library, Person> person1 =
            Expression.<Person> at("persons[1]").of(libraryPosition);
        assertEquals(Person.class, person1.getType());
        assertSame(library.getPersons()[1], person1.getValue());
        final RelativePosition.ReadWrite<Library, Person> person2 =
            Expression.<Person> optional("persons[2]").of(libraryPosition);
        assertEquals(Person.class, person2.getType());
        assertNull(person2.getValue());
    }

    @Test
    public void testMapValue() {
        final Position.Readable<Library> libraryPosition = Positions.readOnly(library);
        final RelativePosition.ReadWrite<Library, Book> wonka =
            Expression.<Book> at("taggedBooks[\"Charlie and the Chocolate Factory\"]").of(libraryPosition);
        assertEquals(Book.class, wonka.getType());
        assertSame(book1, wonka.getValue());

        final RelativePosition.ReadWrite<Library, Book> witches =
            Expression.<Book> optional("taggedBooks['The Witches']").of(libraryPosition);
        assertEquals(Book.class, witches.getType());
        assertNull(witches.getValue());
    }

    @Test
    public void testKitchenSink() {
        final Position.Readable<Library> libraryPosition = Positions.readOnly(library);
        final RelativePosition.ReadWrite<Library, String> address0Line1 =
            Expression.<String> at("taggedBooks['Charlie and the Chocolate Factory'].author.addresses[0].addressline1")
                .of(libraryPosition);
        assertEquals(String.class, address0Line1.getType());
        assertEquals(book1.getAuthor().getAddresses().get(0).getAddressline1(), address0Line1.getValue());
    }

    @Test
    public void testToString() {
        final Position.Readable<Book> bookRef = Positions.readOnly(new Book());
        assertEquals(String.format("Relative Position: Expression #{author.firstName} of %s", bookRef),
            Expression.at("author.firstName").of(bookRef).toString());
    }

    @Test
    public void testOptional() {
        assertNull(Expression.optional("author.firstName").of(Positions.readOnly(Book.class, null)).getValue());
    }
}

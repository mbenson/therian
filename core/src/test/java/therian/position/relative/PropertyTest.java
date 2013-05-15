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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import therian.TypeLiteral;
import therian.position.Position;
import therian.testfixture.Address;
import therian.testfixture.Author;
import therian.testfixture.Book;
import therian.util.Positions;

public class PropertyTest {

    @Test
    public void testBasic() {
        final Book book = new Book();
        book.setTitle("Cryptonomicon");
        RelativePosition.ReadWrite<Book, Author> authorOfBook =
            Property.<Author> at("author").of(Positions.readOnly(book));
        assertEquals(Author.class, authorOfBook.getType());
        assertEquals(null, authorOfBook.getValue());
        assertEquals(Positions.readOnly(book), authorOfBook.getParentPosition());
        final Author author = new Author();
        author.setFirstName("Neal");
        author.setLastName("Stephenson");
        authorOfBook.setValue(author);
        assertEquals("Neal", authorOfBook.getValue().getFirstName());
        assertEquals("Stephenson", authorOfBook.getValue().getLastName());
    }

    @Test
    public void testNested() {
        final Author author = new Author();
        author.setFirstName("Neal");
        author.setLastName("Stephenson");
        final Book book = new Book();
        book.setTitle("Cryptonomicon");
        book.setAuthor(author);
        final RelativePosition.ReadWrite<Author, List<Address>> bookAuthorAddresses =
            Property.<List<Address>> at("addresses").of(Property.<Author> at("author").of(Positions.readOnly(book)));
        final Type addressListType = new TypeLiteral<List<Address>>() {}.value;
        assertEquals(addressListType, bookAuthorAddresses.getType());

        bookAuthorAddresses.setValue(new ArrayList<Address>());
        bookAuthorAddresses.getValue().add(new Address());
        bookAuthorAddresses.getValue().get(0).setZipCode("66666");
        assertEquals("66666", book.getAuthor().getAddresses().get(0).getZipCode());
    }

    @Test
    public void testToString() {
        final Position.Readable<Book> bookRef = Positions.readOnly(new Book());
        assertEquals(String.format("Relative Position: Property author of %s", bookRef),
            Property.at("author").of(bookRef).toString());
    }
}

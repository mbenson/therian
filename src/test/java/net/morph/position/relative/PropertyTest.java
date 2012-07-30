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
package net.morph.position.relative;

import static org.junit.Assert.assertEquals;
import net.morph.position.Ref;
import net.morph.testfixture.Author;
import net.morph.testfixture.Book;

import org.junit.Test;

public class PropertyTest {

    @Test
    public void testBasic() {
        final Book book = new Book();
        RelativePosition.ReadWrite<Book, Author> authorOfBook = Property.<Author> at("author").of(Ref.to(book));
        assertEquals(Author.class, authorOfBook.getType());
        assertEquals(null, authorOfBook.getValue());
        assertEquals(Ref.to(book), authorOfBook.getParentPosition());
        Author author = new Author();
        author.setFirstName("Neal");
        author.setLastName("Stephenson");
        authorOfBook.setValue(author);
        assertEquals("Neal", authorOfBook.getValue().getFirstName());
        assertEquals("Stephenson", authorOfBook.getValue().getLastName());
    }

}

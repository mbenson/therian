/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package therian.testfixture;

import java.util.Objects;

public class Book {

    private String title;
    private String subtitle;
    private Author author;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj instanceof Book) == false) {
            return false;
        }
        final Book other = (Book) obj;
        return Objects.equals(other.getTitle(), getTitle()) && Objects.equals(other.getSubtitle(), getSubtitle())
            && Objects.equals(other.getAuthor(), getAuthor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getSubtitle(), getAuthor());
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(getClass().getSimpleName()).append(' ').append(getTitle());
        if (getSubtitle() != null) {
            buf.append(": ").append(getSubtitle());
        }
        return buf.toString();
    }
}

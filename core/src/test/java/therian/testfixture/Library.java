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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class Library {

    private String libraryName;
    private final Map<String, Book> taggedBooks = new HashMap<String, Book>();

    private Person[] persons;

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public Map<String, Book> getTaggedBooks() {
        return taggedBooks;
    }

    public Person[] getPersons() {
        return persons;
    }

    public void setPersons(Person[] persons) {
        this.persons = persons;
    }

    public List<Employee> getEmployees() {
        if (persons == null) {
            return Collections.emptyList();
        }
        ArrayList<Employee> emps = new ArrayList<Employee>(persons.length);
        for (Person each : persons) {
            if (each instanceof Employee) {
                emps.add((Employee) each);
            }
        }
        return emps;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj instanceof Library) == false) {
            return false;
        }
        final Library other = (Library) obj;
        return Objects.equals(other.getLibraryName(), getLibraryName())
            && Objects.equals(other.getTaggedBooks(), getTaggedBooks())
            && Objects.equals(other.getEmployees(), getEmployees()) && Arrays.equals(other.getPersons(), getPersons());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLibraryName(), getTaggedBooks(), getEmployees(), getPersons());
    }
}

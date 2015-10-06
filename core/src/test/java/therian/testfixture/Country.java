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

public class Country {

    private String name;
    private String ISO2Code;
    private String ISO3Code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getISO2Code() {
        return ISO2Code;
    }

    public void setISO2Code(String ISO2Code) {
        this.ISO2Code = ISO2Code;
    }

    public String getISO3Code() {
        return ISO3Code;
    }

    public void setISO3Code(String ISO3Code) {
        this.ISO3Code = ISO3Code;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj instanceof Country) == false) {
            return false;
        }
        Country other = (Country) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getISO2Code(), other.getISO2Code())
            && Objects.equals(getISO3Code(), other.getISO3Code());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getISO2Code(), getISO3Code());
    }
}

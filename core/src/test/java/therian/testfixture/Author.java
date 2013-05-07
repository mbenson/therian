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

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

public class Author implements Person {
    private String firstName;
    private String lastName;
    private String company;
    private List<Address> addresses;

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getMiddleName() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Author == false) {
            return false;
        }
        final Author other = (Author) obj;
        return ObjectUtils.equals(other.getFirstName(), getFirstName())
            && ObjectUtils.equals(other.getLastName(), getLastName())
            && ObjectUtils.equals(other.getCompany(), getCompany())
            && ObjectUtils.equals(other.getAddresses(), getAddresses());
    }

    @Override
    public int hashCode() {
        int result = 73 << 4;
        result |= ObjectUtils.hashCode(getFirstName());
        result <<= 4;
        result |= ObjectUtils.hashCode(getLastName());
        result <<= 4;
        result |= ObjectUtils.hashCode(getCompany());
        result <<= 16;
        result |= ObjectUtils.hashCode(getAddresses());
        return result;
    }
}

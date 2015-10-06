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

public class Address implements ZipCodeCityCarrier {

	private String addressline1;
	private String addressline2;
	private String zipCode;
	private Country country;
	private String city;

	public String getAddressline1() {
		return addressline1;
	}

	public void setAddressline1(String addressline1) {
		this.addressline1 = addressline1;
	}

	public String getAddressline2() {
		return addressline2;
	}

	public void setAddressline2(String addressline2) {
		this.addressline2 = addressline2;
	}

	@Override
	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	@Override
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if ((obj instanceof Address) == false) {
			return false;
		}
		Address other = (Address) obj;
		return Objects.equals(getAddressline1(), other.getAddressline1()) && Objects.equals(getAddressline2(), other.getAddressline2())
				&& Objects.equals(getCity(), other.getCity()) && Objects.equals(getZipCode(), other.getZipCode())
				&& Objects.equals(getCountry(), other.getCountry());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getAddressline1(), getAddressline2(), getCity(), getZipCode(), getCountry());
	}
}

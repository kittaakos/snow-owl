/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.core.tests.person;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Objects;

import com.b2international.snowowl.core.store.index.Mapping;
import com.b2international.snowowl.core.terminology.Component;

/**
 * @since 4.2
 */
@Mapping(type = "person", mapping = "person_mapping.json")
public class Person extends Component {

	private String firstName;
	private String lastName;
	private int yob;
	private Collection<Address> addresses = newArrayList();

	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public int getYob() {
		return yob;
	}
	
	public Collection<Address> getAddresses() {
		return addresses;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public void setYob(int yob) {
		this.yob = yob;
	}

	public void addAddress(Address address) {
		this.addresses.add(address);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getId(), firstName, lastName, yob, getAddresses());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		return Objects.equals(getId(), other.getId()) && Objects.equals(firstName, other.firstName) && Objects.equals(lastName, other.lastName) && Objects.equals(yob, other.yob) && Objects.equals(getAddresses(), other.getAddresses()); 
	}

	public static Person of(person.Person person) {
		final Person p = new Person();
		p.setFirstName(person.getFirstName());
		p.setLastName(person.getLastName());
		p.setId(person.getId());
		p.setYob(person.getYob());
		return p;
	}
	
}

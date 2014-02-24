/*
 * Copyright 2013 the original author or authors.
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
package org.drugis.addis.security;

public class Account {
  private int id;

  private String username;

  private String firstName;

  private String lastName;

  public Account(String username, String firstName, String lastName) {
    this(-1, username, firstName, lastName);
  }

  public Account(int id, String username, String firstName, String lastName) {
    this.id = id;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public Account() {
  }

  public int getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Account account = (Account) o;

    if (id != account.id) return false;
    if (firstName != null ? !firstName.equals(account.firstName) : account.firstName != null) return false;
    if (lastName != null ? !lastName.equals(account.lastName) : account.lastName != null) return false;
    if (!username.equals(account.username)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + username.hashCode();
    result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
    result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
    return result;
  }
}

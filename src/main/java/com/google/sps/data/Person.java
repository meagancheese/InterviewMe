// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

/** Represents a user on the site. The email is used as the key to the user table. */
public class Person {
  private String email, firstName, lastName, company, job, linkedIn;

  public Person(
      String email,
      String firstName,
      String lastName,
      String company,
      String job,
      String linkedIn) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.company = company;
    this.job = job;
    this.linkedIn = linkedIn;
  }

  public void updateFirstName(String newFirstName) {
    firstName = newFirstName;
  }

  public void updateLastName(String newLastName) {
    lastName = newLastName;
  }

  public void updateCompany(String newCompany) {
    company = newCompany;
  }

  public void updateJob(String newJob) {
    job = newJob;
  }

  public void updateLinkedIn(String newLinkedIn) {
    linkedIn = newLinkedIn;
  }

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getCompany() {
    return company;
  }

  public String getJob() {
    return job;
  }

  public String getLinkedIn() {
    return linkedIn;
  }
}

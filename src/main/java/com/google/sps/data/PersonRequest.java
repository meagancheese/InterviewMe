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

package com.google.sps.data;

/** Represents the data sent in a put or post request to the Person Servlet. */
public class PersonRequest {
  private String email;
  private String firstName;
  private String lastName;
  private String company;
  private String job;
  private String linkedin;

  public PersonRequest(
      String email,
      String firstName,
      String lastName,
      String company,
      String job,
      String linkedin) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.company = company;
    this.job = job;
    this.linkedin = linkedin;
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
    return linkedin;
  }

  public String toString() {
    return String.format(
        "%s= %s:%s, %s:%s, %s:%s, %s:%s, %s:%s, %s:%s",
        "PutPersonRequest",
        "email",
        email,
        "firstName",
        firstName,
        "lastName",
        lastName,
        "company",
        company,
        "job",
        job,
        "linkedin",
        linkedin);
  }
}

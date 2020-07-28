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

import com.google.auto.value.AutoValue;
import java.util.EnumSet;

/** Represents a user on the site. The email is used as the key to the user table. */
@AutoValue
public abstract class Person {
  public abstract String id();

  public abstract String email();

  public abstract String firstName();

  public abstract String lastName();

  public abstract String company();

  public abstract String job();

  public abstract String linkedIn();

  public abstract EnumSet<Job> qualifiedJobs();
  /**
   * Creates a person that contains a user id, email, first name, last name, company, job, and
   * LinkedIn URL.
   */
  public static Person create(
      String id,
      String email,
      String firstName,
      String lastName,
      String company,
      String job,
      String linkedIn,
      EnumSet<Job> qualifiedJobs) {
    return builder()
        .setId(id)
        .setEmail(email)
        .setFirstName(firstName)
        .setLastName(lastName)
        .setCompany(company)
        .setJob(job)
        .setLinkedIn(linkedIn)
        .setQualifiedJobs(qualifiedJobs)
        .build();
  }

  // Creates a person from a Person Servlet request.
  public static Person createFromRequest(String id, String email, PersonRequest personRequest) {
    return builder()
        .setId(id)
        .setEmail(email)
        .setFirstName(personRequest.getFirstName())
        .setLastName(personRequest.getLastName())
        .setCompany(personRequest.getCompany())
        .setJob(personRequest.getJob())
        .setLinkedIn(personRequest.getLinkedIn())
        .setQualifiedJobs(personRequest.getQualifiedJobs())
        .build();
  }

  static Builder builder() {
    return new AutoValue_Person.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setId(String id);

    abstract Builder setEmail(String email);

    abstract Builder setFirstName(String firstName);

    abstract Builder setLastName(String lastName);

    abstract Builder setCompany(String company);

    abstract Builder setJob(String job);

    abstract Builder setLinkedIn(String linkedIn);

    abstract Builder setQualifiedJobs(EnumSet<Job> qualifiedJobs);

    abstract Person build();
  }
}

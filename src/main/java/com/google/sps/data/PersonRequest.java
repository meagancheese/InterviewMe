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

import java.util.EnumSet;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/** Represents the data sent in a put or post request to the Person Servlet. */
public class PersonRequest {
  private String firstName;
  private String lastName;
  private String company;
  private String job;
  private String linkedIn;
  private EnumSet<Job> qualifiedJobs;
  private boolean okShadow;

  public PersonRequest(
      String firstName,
      String lastName,
      String company,
      String job,
      String linkedIn,
      EnumSet<Job> qualifiedJobs,
      boolean okShadow) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.company = company;
    this.job = job;
    this.linkedIn = linkedIn;
    this.qualifiedJobs = qualifiedJobs;
    this.okShadow = okShadow;
  }

  public String getFirstName() {
    return Jsoup.clean(firstName, Whitelist.basic());
  }

  public String getLastName() {
    return Jsoup.clean(lastName, Whitelist.basic());
  }

  public String getCompany() {
    return Jsoup.clean(company, Whitelist.basic());
  }

  public String getJob() {
    return job;
  }

  public String getLinkedIn() {
    return linkedIn;
  }

  public EnumSet<Job> getQualifiedJobs() {
    return qualifiedJobs;
  }

  public boolean getOkShadow() {
    return okShadow;
  }

  public String toString() {
    return String.format(
        "PutPersonRequest= firstName:%s, lastName:%s, company:%s, job:%s, linkedIn:%s, qualifiedJobs: %s, okShadow: %s",
        firstName, lastName, company, job, linkedIn, qualifiedJobs, okShadow);
  }
}

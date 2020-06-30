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

import java.time.Instant;

/**
 * Availability is a time range when a given person is available to offer interviews as an
 * Interviewer.
 */
public class Availability {
  private String personEmail;
  private TimeRange when;
  private Instant date;

  /** This constructor creates a new Availability object */
  public Availability(String personEmail, TimeRange when, Instant date) {
    this.personEmail = personEmail;
    this.when = when;
    this.date = date;
  }

  public void setAvailability(Timerange range) {
    when = range;
  }

  public void setDate(Instant day) {
    date = day;
  }

  public void setEmail(String email) {
    personEmail = email;
  }

  public TimeRange getAvailability() {
    return when;
  }

  public Instant getDate() {
    return date;
  }

  public String getEmail() {
    return personEmail;
  }
}

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

import java.time.LocalDate;

/** Represents a scheduled interview. */
public class ScheduledInterview {
  // TODO: Add the Timerange java class so that this compiles.
  private TimeRange when;
  private LocalDate date; 
  private String interviewerEmail;
  private String intervieweeEmail;
  /** Creates a scheduled interview that contains a timerange, the date and the emails of the attendees. */
  public ScheduledInterview(
      TimeRange when, LocalDate date, String interviewerEmail, String intervieweeEmail) {
    this.when = when;
    this.date = date; 
    this.interviewerEmail = interviewerEmail;
    this.intervieweeEmail = intervieweeEmail;
  }

  /** Sets the timerange of the interview. */
  public void setWhen(Timerange range) {
    when = range; 
  }

  /** Sets the date of the interview. */
  public void setDate(LocalDate day) {
    date = day; 
  }

  /** 
   * Sets the interviewer email. 
   * TODO: use in future work when updating interview
   * due to cancellations */
  public void setInterviewerEmail(String email) {
    interviewerEmail = email; 
  }

  /** 
   * Sets the interviewee email. 
   * TODO: use in future work when updating interview
   * due to cancellations */
  public void setIntervieweeEmail(String email) {
    intervieweeEmail = email; 
  }

  /** Returns the timerange of the interview */
  public TimeRange getWhen() {
    return when;
  }

  /** Returns the date of the interview */
  public LocalDate getDate() {
    return date; 
  }

  /** Returns the email of the interviewer. */
  public String getInterviewerEmail() {
    return interviewerEmail;
  }

  /** Returns the email of the interviewee. */
  public String getIntervieweeEmail() {
    return intervieweeEmail;
  }
}

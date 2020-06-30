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

public class ScheduledInterview {
  private TimeRange when;
  private String interviewerEmail;
  private String intervieweeEmail;

  public ScheduledInterview(
      TimeRange when, String interviewerEmail, String intervieweeEmail) {
    this.when = when;
    this.interviewerEmail = interviewerEmail;
    this.intervieweeEmail = intervieweeEmail;
  }

  /* Returns a TimeRange representing the when the interview starts and ends.
   */
  public TimeRange getWhen() {
    return when;
  }

  /* Returns the email of the interviewer.
   */
  public String getInterviewerEmail() {
    return interviewerEmail;
  }

  /* Returns the email of the interviewee.
   */
  public String getIntervieweeEmail() {
    return intervieweeEmail;
  }
}

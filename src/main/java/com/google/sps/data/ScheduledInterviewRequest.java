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
public class ScheduledInterviewRequest {
  private long id;
  private TimeRange when;
  private String interviewerId;
  private String intervieweeId;

  public ScheduledInterviewRequest(
      long id, TimeRange when, String interviewerId, String intervieweeId) {
    this.id = id;
    this.when = when;
    this.interviewerId = interviewerId;
    this.intervieweeId = intervieweeId;
  }

  public long getId() {
    return id;
  }

  public TimeRange getWhen() {
    return when;
  }

  public String getInterviewerId() {
    return interviewerId;
  }

  public String getIntervieweeId() {
    return intervieweeId;
  }

  public String toString() {
    return String.format(
        "{%s:%s, %s:%s, %s:%s, %s:%s}",
        "id", id, "when", when, "interviewerId", interviewerId, "intervieweeId", intervieweeId);
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ScheduledInterviewRequest) {
      ScheduledInterviewRequest that = (ScheduledInterviewRequest) o;
      return this.getId() == that.getId()
          && this.getWhen().equals(that.getWhen())
          && this.getInterviewerId().equals(that.getInterviewerId())
          && this.getIntervieweeId().equals(that.getIntervieweeId());
    }
    return false;
  }
}

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

import java.util.ArrayList;

/**
 * An InterviewPostOrPutRequest is used to schedule or update an interview for the current user with
 * an interviewer who is qualified to interview for the selected position, has the selected company
 * and job, and is available at the specified time, which is represented by the utcStartTime String.
 */
public class InterviewPostOrPutRequest {
  private String company;
  private String job;
  private String utcStartTime;
  private String position;

  public InterviewPostOrPutRequest(
      String company, String job, String utcStartTime, String position) {
    this.company = company;
    this.job = job;
    this.utcStartTime = utcStartTime;
    this.position = position;
  }

  public String getCompany() {
    return company;
  }

  public String getJob() {
    return job;
  }

  public String getUtcStartTime() {
    return utcStartTime;
  }

  public String getPosition() {
    return position;
  }

  public boolean allFieldsPopulated() {
    return !(company == null || job == null || utcStartTime == null || position == null);
  }

  public String toString() {
    return String.format(
        "InterviewPostOrPutRequest= company:%s, job:%s, utcStartTime:%s, position:%s",
        company, job, utcStartTime, position);
  }
}

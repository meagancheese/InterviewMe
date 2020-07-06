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
import java.time.LocalDate;

/** Represents a scheduled interview. */
@AutoValue
public abstract class ScheduledInterview {
  // TODO: Add the TimeRange java class so that this compiles.
  abstract TimeRange when();

  abstract LocalDate date();

  abstract String interviewerEmail();

  abstract String intervieweeEmail();
  /**
   * Creates a scheduled interview that contains a timerange, the date and the emails of the
   * attendees.
   */
  static ScheduledInterview create(
      TimeRange when, LocalDate date, String interviewerEmail, String intervieweeEmail) {
    return new AutoValue_ScheduledInterview(when, date, interviewerEmail, intervieweeEmail);
  }

  static Builder builder() {
    return new AutoValue_ScheduledInterview.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setWhen(TimeRange range);

    abstract Builder setDate(LocalDate day);

    abstract Builder setInterviewerEmail(String email);

    abstract Builder setIntervieweeEmail(String email);

    abstract ScheduledInterview build();
  }
}

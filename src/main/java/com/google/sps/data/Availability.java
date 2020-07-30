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

/**
 * Availability is a time range when a given person is available to offer interviews as an
 * interviewer.
 */
@AutoValue
public abstract class Availability {
  public abstract String userId();

  public abstract TimeRange when();

  public abstract long id();

  // If this time slot has an interview.
  public abstract boolean scheduled();

  public static Availability create(String userId, TimeRange when, long id, boolean scheduled) {
    return builder().setUserId(userId).setWhen(when).setId(id).setScheduled(scheduled).build();
  }

  abstract Builder toBuilder();

  // Returns a new Availability with the old information plus an updated id.
  public Availability withId(long id) {
    return toBuilder().setId(id).build();
  }

  // Returns a new Availability with the old information with an updated scheduled status.
  public Availability withScheduled(boolean scheduled) {
    return toBuilder().setScheduled(scheduled).build();
  }

  public static Builder builder() {
    return new AutoValue_Availability.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setUserId(String userId);

    public abstract Builder setWhen(TimeRange when);

    public abstract Builder setId(long id);

    public abstract Builder setScheduled(boolean scheduled);

    public abstract Availability build();
  }
}

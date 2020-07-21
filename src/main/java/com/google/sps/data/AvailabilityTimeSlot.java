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
import com.google.common.annotations.VisibleForTesting;

/**
 * An AvailabilityTimeSlot represents a 15 minute chunk of time that has a date and time that are
 * expressed as readable date and time fields as well as a more all-encompassing utcEncoding. It
 * also has two booleans that tells whether or not the time slot has been selected (marked as a time
 * when the user is available to be an interviewer) and whether or not the time slot has been
 * scheduled (the user is already scheduled to conduct an interview during that time).
 */
@AutoValue
public abstract class AvailabilityTimeSlot {
  public abstract String utcEncoding();

  public abstract String time();

  public abstract String date();

  public abstract boolean selected();

  public abstract boolean scheduled();

  @VisibleForTesting
  static AvailabilityTimeSlot create(
      String utcEncoding, String time, String date, boolean selected, boolean scheduled) {
    return builder()
        .setUtcEncoding(utcEncoding)
        .setTime(time)
        .setDate(date)
        .setSelected(selected)
        .setScheduled(scheduled)
        .build();
  }

  static Builder builder() {
    return new AutoValue_AvailabilityTimeSlot.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setUtcEncoding(String utcEncoding);

    abstract Builder setTime(String time);

    abstract Builder setDate(String date);

    abstract Builder setSelected(boolean selected);

    abstract Builder setScheduled(boolean scheduled);

    abstract AvailabilityTimeSlot build();
  }
}

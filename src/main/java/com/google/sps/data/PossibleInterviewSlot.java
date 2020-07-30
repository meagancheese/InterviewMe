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
 * A PossibleInterviewSlot represents a 1 hour chunk of time with a utcEncoding and readable date
 * and time strings. This class is used for frontend generation only.
 */
@AutoValue
public abstract class PossibleInterviewSlot {
  public abstract String utcEncoding();

  public abstract String date();

  public abstract String time();

  public static PossibleInterviewSlot create(String utcEncoding, String date, String time) {
    return builder().setUtcEncoding(utcEncoding).setDate(date).setTime(time).build();
  }

  static Builder builder() {
    return new AutoValue_PossibleInterviewSlot.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setUtcEncoding(String utcEncoding);

    abstract Builder setDate(String date);

    abstract Builder setTime(String time);

    abstract PossibleInterviewSlot build();
  }
}

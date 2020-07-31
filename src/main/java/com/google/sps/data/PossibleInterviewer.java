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
 * A PossibleInterviewer has a company and a job, and nothing else. This class is used for frontend
 * generation only.
 */
@AutoValue
public abstract class PossibleInterviewer {
  public abstract String company();

  public abstract String job();

  public static PossibleInterviewer create(String company, String job) {
    return builder().setCompany(company).setJob(job).build();
  }

  static Builder builder() {
    return new AutoValue_PossibleInterviewer.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setCompany(String company);

    abstract Builder setJob(String job);

    abstract PossibleInterviewer build();
  }
}

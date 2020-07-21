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

public class PutAvailabilityRequest {
  private String firstSlot;
  private String lastSlot;
  private ArrayList<String> markedSlots;

  public PutAvailabilityRequest(String firstSlot, String lastSlot, ArrayList<String> markedSlots) {
    this.firstSlot = firstSlot;
    this.lastSlot = lastSlot;
    this.markedSlots = markedSlots;
  }

  public String getFirstSlot() {
    return firstSlot;
  }

  public String getLastSlot() {
    return lastSlot;
  }

  public ArrayList<String> getMarkedSlots() {
    return markedSlots;
  }

  public boolean allFieldsPopulated() {
    return !(firstSlot == null || lastSlot == null || markedSlots == null);
  }

  public String toString() {
    return String.format(
        "%s= %s:%s, %s:%s, %s:%s",
        "PutAvailabilityRequest",
        "firstSlot",
        firstSlot,
        "lastSlot",
        lastSlot,
        "markedSlots",
        markedSlots);
  }
}

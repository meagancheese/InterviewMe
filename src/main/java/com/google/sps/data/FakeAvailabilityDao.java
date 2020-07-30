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

import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/** Mimics accessing Datastore to support managing Availability entities. */
public class FakeAvailabilityDao implements AvailabilityDao {
  // storedObjects is the fake database.
  private LinkedHashMap<Long, Availability> storedObjects;

  /** Initializes the fields for FakeAvailabilityDao. */
  public FakeAvailabilityDao() {
    storedObjects = new LinkedHashMap<Long, Availability>();
  }

  /** Puts an Availability object into storedObjects with a randomly generated long as its id. */
  @Override
  public void create(Availability avail) {
    long id = new Random().nextLong();
    Availability toStoreAvail = avail.withId(id);
    storedObjects.put(id, toStoreAvail);
  }

  /** Updates an Availability in storedObjects based on its id. */
  @Override
  public void update(Availability avail) {
    storedObjects.put(avail.id(), avail);
  }

  /**
   * Retrieves the Availability from storedObjects from the given id and wraps it in an Optional. If
   * the Availability does not exist in storedObjects, the Optional is empty.
   */
  @Override
  public Optional<Availability> get(long id) {
    if (storedObjects.containsKey(id)) {
      return Optional.of(storedObjects.get(id));
    }
    return Optional.empty();
  }

  /** Deletes all Availability entities for a user ranging from minTime to maxTime. */
  @Override
  public void deleteInRangeForUser(String userId, Instant minTime, Instant maxTime) {
    List<Availability> userAvailability = getForUser(userId);
    List<Availability> userAvailabilityInRange = getInRange(userAvailability, minTime, maxTime);
    for (Availability avail : userAvailabilityInRange) {
      storedObjects.remove(avail.id());
    }
  }

  /**
   * Collects all Availabilities for the specified user within the specified time range and returns
   * them in order (by ascending start times).
   */
  @Override
  public List<Availability> getInRangeForUser(String userId, Instant minTime, Instant maxTime) {
    List<Availability> userAvailability = getForUser(userId);
    return getInRange(userAvailability, minTime, maxTime);
  }

  private List<Availability> getForUser(String userId) {
    List<Availability> allAvailability = new ArrayList<Availability>(storedObjects.values());
    List<Availability> userAvailability = new ArrayList<Availability>();
    for (Availability avail : allAvailability) {
      if (avail.userId().equals(userId)) {
        userAvailability.add(avail);
      }
    }
    return userAvailability;
  }

  /**
   * Collects all Availabilities within the specified time range and returns them in order (by
   * ascending start times).
   */
  @Override
  public List<Availability> getInRangeForAll(Instant minTime, Instant maxTime) {
    return getInRange(new ArrayList<Availability>(storedObjects.values()), minTime, maxTime);
  }

  private List<Availability> getInRange(
      List<Availability> allAvailability, Instant minTime, Instant maxTime) {
    TimeRange range = new TimeRange(minTime, maxTime);
    List<Availability> inRangeAvailability = new ArrayList<Availability>();
    for (Availability avail : allAvailability) {
      if (range.contains(avail.when())) {
        inRangeAvailability.add(avail);
      }
    }
    inRangeAvailability.sort(
        (Availability a1, Availability a2) -> {
          if (a1.when().start().equals(a2.when().start())) {
            return 0;
          }
          if (a1.when().start().isBefore(a2.when().start())) {
            return -1;
          }
          return 1;
        });
    return inRangeAvailability;
  }

  /** Returns the ids of all users that have availabilities within the specified time range. */
  public Set<String> getUsersAvailableInRange(Instant minTime, Instant maxTime) {
    Set<String> userIds = new HashSet<String>();
    TimeRange range = new TimeRange(minTime, maxTime);
    List<Availability> allAvailability = new ArrayList<Availability>(storedObjects.values());
    for (Availability avail : allAvailability) {
      if (range.contains(avail.when())) {
        userIds.add(avail.userId());
      }
    }
    return userIds;
  }
}

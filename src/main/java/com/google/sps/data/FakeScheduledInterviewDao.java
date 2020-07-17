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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/** Mimics accessing Datastore to support managing ScheduledInterview entities. */
public class FakeScheduledInterviewDao implements ScheduledInterviewDao {
  // data is the fake database
  public Map<Long, ScheduledInterview> data;

  /** Initializes the fields for ScheduledInterviewDatastoreDAO. */
  public FakeScheduledInterviewDao() {
    data = new LinkedHashMap<Long, ScheduledInterview>();
  }

  /**
   * Retrieves a scheduledInterviewEntity from storage and returns it as a ScheduledInterview
   * object.
   */
  @Override
  public Optional<ScheduledInterview> get(long id) {
    if (data.containsKey(id)) {
      return Optional.of(data.get(id));
    }
    return Optional.empty();
  }

  /**
   * Retrieves all scheduledInterview entities from storage that involve a particular user and
   * returns them as a list of ScheduledInterview objects in the order in which they occur.
   */
  @Override
  public List<ScheduledInterview> getForPerson(String email) {
    List<ScheduledInterview> relevantInterviews = new ArrayList<>();
    List<ScheduledInterview> scheduledInterviews = new ArrayList<ScheduledInterview>(data.values());
    scheduledInterviews.sort(
        (ScheduledInterview s1, ScheduledInterview s2) -> {
          if (s1.when().start().equals(s2.when().start())) {
            return 0;
          }
          if (s1.when().start().isBefore(s2.when().start())) {
            return -1;
          }
          return 1;
        });

    for (ScheduledInterview scheduledInterview : scheduledInterviews) {
      if (email.equals(scheduledInterview.interviewerEmail())
          || email.equals(scheduledInterview.intervieweeEmail())) {
        relevantInterviews.add(scheduledInterview);
      }
    }
    return relevantInterviews;
  }

  /** Returns a list of all scheduledInterviews ranging from minTime to maxTime of a user. */
  @Override
  public List<ScheduledInterview> getScheduledInterviewsInRangeForUser(
      String email, Instant minTime, Instant maxTime) {
    TimeRange range = new TimeRange(minTime, maxTime);
    List<ScheduledInterview> scheduledInterviews = getForPerson(email);
    List<ScheduledInterview> scheduledInterviewsInRange = new ArrayList<ScheduledInterview>();
    for (ScheduledInterview scheduledInterview : scheduledInterviews) {
      if (range.contains(scheduledInterview.when())) {
        scheduledInterviewsInRange.add(scheduledInterview);
      }
    }
    return scheduledInterviewsInRange;
  }

  /** Creates a ScheduledInterview Entity and stores it. */
  @Override
  public void create(ScheduledInterview scheduledInterview) {
    long generatedId = new Random().nextLong();
    ScheduledInterview storedScheduledInterview =
        ScheduledInterview.create(
            generatedId,
            scheduledInterview.when(),
            scheduledInterview.interviewerEmail(),
            scheduledInterview.intervieweeEmail());
    data.put(generatedId, storedScheduledInterview);
  }

  /** Updates an entity. */
  @Override
  public void update(ScheduledInterview scheduledInterview) {
    data.put(scheduledInterview.id(), scheduledInterview);
  }

  /** Deletes an entity. */
  @Override
  public void delete(long id) {
    data.remove(id);
  }
}

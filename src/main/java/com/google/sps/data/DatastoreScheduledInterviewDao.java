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
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

/** Accesses Datastore to support managing ScheduledInterview entities. */
public class DatastoreScheduledInterviewDao implements ScheduledInterviewDao {
  // @param datastore The DatastoreService we're using to interact with Datastore.
  private DatastoreService datastore;

  /** Initializes the fields for ScheduledInterviewDatastoreDAO. */
  public DatastoreScheduledInterviewDao() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Retrieves a scheduledInterviewEntity from Datastore and returns it as a ScheduledInterview
   * object.
   */
  @Override
  public Optional<ScheduledInterview> get(long id) {
    Key key = KeyFactory.createKey("ScheduledInterview", id);
    Entity scheduledInterviewEntity;
    try {
      scheduledInterviewEntity = datastore.get(key);
    } catch (com.google.appengine.api.datastore.EntityNotFoundException e) {
      return Optional.empty();
    }
    return Optional.of(entityToScheduledInterview(scheduledInterviewEntity));
  }

  /**
   * Retrieves all scheduledInterview entities from Datastore that involve a particular user and
   * returns them as a list of ScheduledInterview objects.
   */
  @Override
  public List<ScheduledInterview> getForPerson(String email) {
    FilterPredicate interviewerFilter =
        new FilterPredicate("interviewer", FilterOperator.EQUAL, email);
    FilterPredicate intervieweeFilter =
        new FilterPredicate("interviewee", FilterOperator.EQUAL, email);
    CompositeFilter compositeFilter =
        CompositeFilterOperator.or(interviewerFilter, intervieweeFilter);
    Query query = new Query().setFilter(compositeFilter);
    PreparedQuery results = datastore.prepare(query);
    List<ScheduledInterview> relevantInterviews = new ArrayList<>();

    for (Entity entity : results.asIterable()) {
      relevantInterviews.add(entityToScheduledInterview(entity));
    }
    return relevantInterviews;
  }

  /** Creates a ScheduledInterview Entity. */
  @Override
  public void create(ScheduledInterview scheduledInterview) {
    Entity scheduledInterviewEntity = new Entity("ScheduledInterview");
    scheduledInterviewEntity.setProperty("startTime", scheduledInterview.when().start().toString());
    scheduledInterviewEntity.setProperty("endTime", scheduledInterview.when().end().toString());
    scheduledInterviewEntity.setProperty("date", scheduledInterview.date().toString());
    scheduledInterviewEntity.setProperty("interviewer", scheduledInterview.interviewerEmail());
    scheduledInterviewEntity.setProperty("interviewee", scheduledInterview.intervieweeEmail());
    datastore.put(scheduledInterviewEntity);
  }

  /** Updates an entity in datastore. */
  @Override
  public void update(ScheduledInterview scheduledInterview) {
    delete(scheduledInterview.id());
    create(scheduledInterview);
  }

  /** Deletes an entity in datastore. */
  @Override
  public void delete(long id) {
    Key key = KeyFactory.createKey("ScheduledInterview", id);
    datastore.delete(key);
  }

  /** Creates a ScheduledInterview object from a datastore entity. */
  public ScheduledInterview entityToScheduledInterview(Entity scheduledInterviewEntity) {
    return ScheduledInterview.create(
        Long.valueOf(scheduledInterviewEntity.getKey().toString()),
        new TimeRange(
            Instant.parse(scheduledInterviewEntity.getProperty("startTime").toString()),
            Instant.parse(scheduledInterviewEntity.getProperty("endTime").toString())),
        LocalDate.parse(scheduledInterviewEntity.getProperty("date").toString()),
        (String) scheduledInterviewEntity.getProperty("interviewer"),
        (String) scheduledInterviewEntity.getProperty("interviewee"));
  }
}

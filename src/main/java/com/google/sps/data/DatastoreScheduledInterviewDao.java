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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
   * returns them as a list of ScheduledInterview objects in the order in which they occur.
   */
  @Override
  public List<ScheduledInterview> getForPerson(String email) {
    FilterPredicate interviewerFilter =
        new FilterPredicate("interviewer", FilterOperator.EQUAL, email);
    FilterPredicate intervieweeFilter =
        new FilterPredicate("interviewee", FilterOperator.EQUAL, email);
    CompositeFilter compositeFilter =
        CompositeFilterOperator.or(interviewerFilter, intervieweeFilter);
    Query query =
        new Query("ScheduledInterview")
            .setFilter(compositeFilter)
            .addSort("startTime", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);
    List<ScheduledInterview> relevantInterviews = new ArrayList<>();

    for (Entity entity : results.asIterable()) {
      relevantInterviews.add(entityToScheduledInterview(entity));
    }
    return relevantInterviews;
  }

  /**
   * Returns a list of all scheduledInterviews ranging from minTime to maxTime of a user. minTime
   * and maxTime are in milliseconds.
   */
  @Override
  public List<ScheduledInterview> getScheduledInterviewsInRangeForUser(
      String email, Instant minTime, Instant maxTime) {
    Filter interviewerFilter = new FilterPredicate("interviewer", FilterOperator.EQUAL, email);
    Filter intervieweeFilter = new FilterPredicate("interviewee", FilterOperator.EQUAL, email);
    CompositeFilter scheduledForUserFilter =
        CompositeFilterOperator.or(interviewerFilter, intervieweeFilter);
    List<Entity> entities =
        getEntitiesInRange(minTime, maxTime, Optional.of(scheduledForUserFilter));
    List<ScheduledInterview> scheduledInterviews = new ArrayList<ScheduledInterview>();
    for (Entity entity : entities) {
      scheduledInterviews.add(entityToScheduledInterview(entity));
    }
    return scheduledInterviews;
  }

  /** Creates a ScheduledInterview Entity and stores it in Datastore. */
  @Override
  public void create(ScheduledInterview scheduledInterview) {
    datastore.put(scheduledInterviewToEntity(scheduledInterview));
  }

  /** Updates an entity in datastore. */
  @Override
  public void update(ScheduledInterview scheduledInterview) {
    datastore.put(scheduledInterviewToEntityForUpdate(scheduledInterview));
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
        scheduledInterviewEntity.getKey().getId(),
        new TimeRange(
            Instant.ofEpochMilli((long) scheduledInterviewEntity.getProperty("startTime")),
            Instant.ofEpochMilli((long) scheduledInterviewEntity.getProperty("endTime"))),
        (String) scheduledInterviewEntity.getProperty("interviewer"),
        (String) scheduledInterviewEntity.getProperty("interviewee"));
  }

  /** Creates a scheduledInterview Entity from a scheduledInterview object. */
  public Entity scheduledInterviewToEntity(ScheduledInterview scheduledInterview) {
    Entity scheduledInterviewEntity = new Entity("ScheduledInterview");
    scheduledInterviewEntity.setProperty(
        "startTime", scheduledInterview.when().start().toEpochMilli());
    scheduledInterviewEntity.setProperty("endTime", scheduledInterview.when().end().toEpochMilli());
    scheduledInterviewEntity.setProperty("interviewer", scheduledInterview.interviewerEmail());
    scheduledInterviewEntity.setProperty("interviewee", scheduledInterview.intervieweeEmail());
    return scheduledInterviewEntity;
  }

  /** Creates a scheduledInterview Entity with the updated fields and id of a scheduledInterview */
  public Entity scheduledInterviewToEntityForUpdate(ScheduledInterview scheduledInterview) {
    Entity scheduledInterviewEntity = new Entity("ScheduledInterview", scheduledInterview.id());
    scheduledInterviewEntity.setProperty(
        "startTime", scheduledInterview.when().start().toEpochMilli());
    scheduledInterviewEntity.setProperty("endTime", scheduledInterview.when().end().toEpochMilli());
    scheduledInterviewEntity.setProperty("interviewer", scheduledInterview.interviewerEmail());
    scheduledInterviewEntity.setProperty("interviewee", scheduledInterview.intervieweeEmail());
    return scheduledInterviewEntity;
  }

  /**
   * Returns interviews within a desired range in the order in which they occur. For example:
   * scheduledInterviews starting >= 2:00PM and ending <= 6:00PM on a certain date. The maxTime is
   * 6:00PM on that day.
   */
  private List<Entity> getEntitiesInRange(
      Instant minTime, Instant maxTime, Optional<Filter> filterOpt) {
    Filter minFilter =
        new FilterPredicate(
            "startTime", FilterOperator.GREATER_THAN_OR_EQUAL, minTime.toEpochMilli());
    // Queries can only perform inequality filters on one parameter, and so instead
    // of using endTime for the maxFilter, startTime is used and the maxTime has 60
    // minutes subtracted from it to be equal to the latest possible startTime.
    Filter maxFilter =
        new FilterPredicate(
            "startTime",
            FilterOperator.LESS_THAN_OR_EQUAL,
            maxTime.minus(60, ChronoUnit.MINUTES).toEpochMilli());
    CompositeFilter compFilter = CompositeFilterOperator.and(minFilter, maxFilter);
    if (filterOpt.isPresent()) {
      compFilter = CompositeFilterOperator.and(compFilter, filterOpt.get());
    }
    Query scheduledInterviewQuery =
        new Query("ScheduledInterview")
            .setFilter(compFilter)
            .addSort("startTime", SortDirection.ASCENDING);
    return datastore.prepare(scheduledInterviewQuery).asList(FetchOptions.Builder.withDefaults());
  }
}

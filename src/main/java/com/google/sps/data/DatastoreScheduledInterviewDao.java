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
   * Returns a list, sorted by start time, of all scheduled ScheduledInterview objects between
   * minTime and maxTime.
   */
  public List<ScheduledInterview> getInRange(Instant minTime, Instant maxTime) {
    List<Entity> entities = getEntitiesInRange(minTime, maxTime, Optional.empty());
    List<ScheduledInterview> scheduledInterviews = new ArrayList<ScheduledInterview>();
    for (Entity entity : entities) {
      scheduledInterviews.add(entityToScheduledInterview(entity));
    }
    return scheduledInterviews;
  }

  /**
   * Returns a list, sorted by start time, of all ScheduledInterview objects ranging from minTime to
   * maxTime that are for the selected position and do not already have a shadow.
   */
  public List<ScheduledInterview> getForPositionWithoutShadowInRange(
      Job position, Instant minTime, Instant maxTime) {
    List<Filter> allFilters = new ArrayList<>();
    allFilters.add(getTimeFilter(minTime, maxTime));
    allFilters.add(new FilterPredicate("position", FilterOperator.EQUAL, position.name()));
    allFilters.add(new FilterPredicate("shadow", FilterOperator.EQUAL, ""));
    CompositeFilter forPositionWithoutShadowInRange = CompositeFilterOperator.and(allFilters);

    Query query =
        new Query("ScheduledInterview")
            .setFilter(forPositionWithoutShadowInRange)
            .addSort("startTime", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);
    List<ScheduledInterview> relevantInterviews = new ArrayList<>();

    for (Entity entity : results.asIterable()) {
      relevantInterviews.add(entityToScheduledInterview(entity));
    }
    return relevantInterviews;
  }

  /**
   * Retrieves all scheduledInterview entities from Datastore that involve a particular user and
   * returns them as a list of ScheduledInterview objects in the order in which they occur.
   */
  @Override
  public List<ScheduledInterview> getForPerson(String userId) {
    Query query =
        new Query("ScheduledInterview")
            .setFilter(getUserFilter(userId))
            .addSort("startTime", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);
    List<ScheduledInterview> relevantInterviews = new ArrayList<>();

    for (Entity entity : results.asIterable()) {
      relevantInterviews.add(entityToScheduledInterview(entity));
    }
    return relevantInterviews;
  }

  // Returns a filter checking if userId is any role in a ScheduledInterview.
  private CompositeFilter getUserFilter(String userId) {
    Filter interviewerFilter = new FilterPredicate("interviewer", FilterOperator.EQUAL, userId);
    Filter intervieweeFilter = new FilterPredicate("interviewee", FilterOperator.EQUAL, userId);
    Filter shadowFilter = new FilterPredicate("shadow", FilterOperator.EQUAL, userId);
    return CompositeFilterOperator.or(
        CompositeFilterOperator.or(interviewerFilter, intervieweeFilter), shadowFilter);
  }

  /** Returns a list of all scheduledInterviews ranging from minTime to maxTime of a user. */
  @Override
  public List<ScheduledInterview> getScheduledInterviewsInRangeForUser(
      String userId, Instant minTime, Instant maxTime) {
    List<Entity> entities =
        getEntitiesInRange(minTime, maxTime, Optional.of(getUserFilter(userId)));
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
        (String) scheduledInterviewEntity.getProperty("interviewee"),
        (String) scheduledInterviewEntity.getProperty("meetLink"),
        Job.valueOf((String) scheduledInterviewEntity.getProperty("position")),
        (String) scheduledInterviewEntity.getProperty("shadow"));
  }

  /** Creates a scheduledInterview Entity from a scheduledInterview object. */
  public Entity scheduledInterviewToEntity(ScheduledInterview scheduledInterview) {
    Entity scheduledInterviewEntity = new Entity("ScheduledInterview");
    scheduledInterviewEntity.setProperty(
        "startTime", scheduledInterview.when().start().toEpochMilli());
    scheduledInterviewEntity.setProperty("endTime", scheduledInterview.when().end().toEpochMilli());
    scheduledInterviewEntity.setProperty("interviewer", scheduledInterview.interviewerId());
    scheduledInterviewEntity.setProperty("interviewee", scheduledInterview.intervieweeId());
    scheduledInterviewEntity.setProperty("meetLink", scheduledInterview.meetLink());
    scheduledInterviewEntity.setProperty("position", scheduledInterview.position().name());
    scheduledInterviewEntity.setProperty("shadow", scheduledInterview.shadowId());
    return scheduledInterviewEntity;
  }

  /** Creates a scheduledInterview Entity with the updated fields and id of a scheduledInterview */
  public Entity scheduledInterviewToEntityForUpdate(ScheduledInterview scheduledInterview) {
    Entity scheduledInterviewEntity = new Entity("ScheduledInterview", scheduledInterview.id());
    scheduledInterviewEntity.setProperty(
        "startTime", scheduledInterview.when().start().toEpochMilli());
    scheduledInterviewEntity.setProperty("endTime", scheduledInterview.when().end().toEpochMilli());
    scheduledInterviewEntity.setProperty("interviewer", scheduledInterview.interviewerId());
    scheduledInterviewEntity.setProperty("interviewee", scheduledInterview.intervieweeId());
    scheduledInterviewEntity.setProperty("meetLink", scheduledInterview.meetLink());
    scheduledInterviewEntity.setProperty("position", scheduledInterview.position().name());
    scheduledInterviewEntity.setProperty("shadow", scheduledInterview.shadowId());
    return scheduledInterviewEntity;
  }

  /**
   * Returns interviews within a desired range in the order in which they occur. For example: to get
   * scheduledInterviews starting >= 2:00PM and ending <= 6:00PM on a certain date, set the maxTime
   * to be 6:00PM on that date.
   */
  private List<Entity> getEntitiesInRange(
      Instant minTime, Instant maxTime, Optional<Filter> filterOpt) {
    CompositeFilter startAndEndFilter = getTimeFilter(minTime, maxTime);
    if (filterOpt.isPresent()) {
      startAndEndFilter = CompositeFilterOperator.and(startAndEndFilter, filterOpt.get());
    }
    Query scheduledInterviewQuery =
        new Query("ScheduledInterview")
            .setFilter(startAndEndFilter)
            .addSort("startTime", SortDirection.ASCENDING);
    return datastore.prepare(scheduledInterviewQuery).asList(FetchOptions.Builder.withDefaults());
  }

  private CompositeFilter getTimeFilter(Instant minTime, Instant maxTime) {
    Filter startTimeFilter =
        new FilterPredicate(
            "startTime", FilterOperator.GREATER_THAN_OR_EQUAL, minTime.toEpochMilli());
    // Queries can only perform inequality filters on one parameter, and so instead
    // of using endTime for the endTimeFilter, startTime is used and the maxTime has 60
    // minutes subtracted from it to be equal to the latest possible startTime.
    Filter endTimeFilter =
        new FilterPredicate(
            "startTime",
            FilterOperator.LESS_THAN_OR_EQUAL,
            maxTime.minus(60, ChronoUnit.MINUTES).toEpochMilli());
    return CompositeFilterOperator.and(startTimeFilter, endTimeFilter);
  }
}

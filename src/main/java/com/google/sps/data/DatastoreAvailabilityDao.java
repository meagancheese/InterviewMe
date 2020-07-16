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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Accesses Datastore to support managing Availability entities. */
public class DatastoreAvailabilityDao implements AvailabilityDao {
  // @param datastore the DatastoreService we're using to interact with Datastore.
  private DatastoreService datastore;

  /** Initializes the fields for DatastoreAvailabilityDao. */
  public DatastoreAvailabilityDao() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Retrieve the availability from Datastore from its id and wrap it in an Optional. If the id
   * isn't in Datastore, the Optional is empty.
   */
  @Override
  public Optional<Availability> get(long id) {
    Key key = KeyFactory.createKey("Availability", id);
    Entity availabilityEntity;
    try {
      availabilityEntity = datastore.get(key);
    } catch (com.google.appengine.api.datastore.EntityNotFoundException e) {
      return Optional.empty();
    }
    return Optional.of(entityToAvailability(availabilityEntity));
  }

  // Adds an Availability object into Datastore.
  @Override
  public void create(Availability avail) {
    datastore.put(availabilityToNewEntity(avail));
  }

  // Updates the specified id with the new availability.
  @Override
  public void update(Availability avail) {
    datastore.put(availabilityToUpdatedEntity(avail));
  }

  static Entity availabilityToNewEntity(Availability avail) {
    Entity availabilityEntity = new Entity("Availability");
    availabilityEntity.setProperty("email", avail.email());
    availabilityEntity.setProperty("startTime", avail.when().start().toEpochMilli());
    availabilityEntity.setProperty("endTime", avail.when().end().toEpochMilli());
    availabilityEntity.setProperty("scheduled", avail.scheduled());
    return availabilityEntity;
  }

  static Entity availabilityToUpdatedEntity(Availability avail) {
    Entity availabilityEntity = new Entity("Availability", avail.id());
    availabilityEntity.setProperty("email", avail.email());
    availabilityEntity.setProperty("startTime", avail.when().start().toEpochMilli());
    availabilityEntity.setProperty("endTime", avail.when().end().toEpochMilli());
    availabilityEntity.setProperty("scheduled", avail.scheduled());
    return availabilityEntity;
  }

  static Availability entityToAvailability(Entity availabilityEntity) {
    return Availability.create(
        (String) availabilityEntity.getProperty("email"),
        TimeRange.fromStartEnd(
            Instant.ofEpochMilli((long) availabilityEntity.getProperty("startTime")),
            Instant.ofEpochMilli((long) availabilityEntity.getProperty("endTime"))),
        availabilityEntity.getKey().getId(),
        (boolean) availabilityEntity.getProperty("scheduled"));
  }

  // Deletes all Availability entities for a user ranging from minTime to maxTime.
  // minTime and maxTime are in milliseconds.
  @Override
  public void deleteInRangeForUser(String email, long minTime, long maxTime) {
    Filter userFilter = new FilterPredicate("email", FilterOperator.EQUAL, email);
    List<Entity> entities = getEntitiesInRange(minTime, maxTime, Optional.of(userFilter));
    List<Key> keyList = new ArrayList<>();
    for (Entity entity : entities) {
      keyList.add(entity.getKey());
    }
    // This iterative deletion avoids XG transactions, which max out at 25 root entities.
    for (Key key : keyList) {
      Transaction txn = datastore.beginTransaction();
      datastore.delete(txn, key);
      txn.commit();
    }
  }

  // Returns a list of all Availabilities ranging from minTime to maxTime of a user.
  // minTime and maxTime are in milliseconds.
  @Override
  public List<Availability> getInRangeForUser(String email, long minTime, long maxTime) {
    Filter userFilter = new FilterPredicate("email", FilterOperator.EQUAL, email);
    List<Entity> entities = getEntitiesInRange(minTime, maxTime, Optional.of(userFilter));
    List<Availability> availability = new ArrayList<Availability>();
    for (Entity entity : entities) {
      availability.add(entityToAvailability(entity));
    }
    return availability;
  }

  // Returns all Availabilities across all users ranging from minTime to maxTime.
  // minTime and maxTime are in milliseconds.
  @Override
  public List<Availability> getInRangeForAll(long minTime, long maxTime) {
    List<Entity> entities = getEntitiesInRange(minTime, maxTime, Optional.empty());
    List<Availability> availability = new ArrayList<Availability>();
    for (Entity entity : entities) {
      availability.add(entityToAvailability(entity));
    }
    return availability;
  }

  private List<Entity> getEntitiesInRange(long minTime, long maxTime, Optional<Filter> filterOpt) {
    Filter minFilter =
        new FilterPredicate("startTime", FilterOperator.GREATER_THAN_OR_EQUAL, minTime);
    Filter maxFilter = new FilterPredicate("startTime", FilterOperator.LESS_THAN_OR_EQUAL, maxTime);
    CompositeFilter compFilter = CompositeFilterOperator.and(minFilter, maxFilter);
    if (filterOpt.isPresent()) {
      compFilter = CompositeFilterOperator.and(compFilter, filterOpt.get());
    }

    Query availQuery = new Query("Availability").setFilter(compFilter);
    return datastore.prepare(availQuery).asList(FetchOptions.Builder.withDefaults());
  }
}

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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/** Accesses Datastore to support managing Person entities. */
public class DatastorePersonDao implements PersonDao {
  // @param datastore the DatastoreService we're using to interact with Datastore.
  private DatastoreService datastore;

  /** Initializes the fields for PersonDatastoreDAO. */
  public DatastorePersonDao() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /** We make an entity in Datastore with person's fields as properties. */
  @Override
  public void create(Person person) {
    datastore.put(personToEntity(person));
  }

  /** We update an entity in Datastore with person's fields as properties. */
  @Override
  public void update(Person person) {
    datastore.put(personToEntity(person));
  }

  /**
   * Retrieve the person from Datastore from their id and wrap it in an Optional. If they aren't in
   * Datastore, the Optional is empty.
   */
  @Override
  public Optional<Person> get(String id) {
    Key key = KeyFactory.createKey("Person", id);
    Entity personEntity;
    try {
      personEntity = datastore.get(key);
    } catch (com.google.appengine.api.datastore.EntityNotFoundException e) {
      return Optional.empty();
    }
    return Optional.of(entityToPerson(personEntity));
  }

  // Returns the job qualification booleans in personEntity as an EnumSet.
  private static EnumSet<Job> entityBooleansToEnumSet(Entity personEntity) {
    List<Job> qualifiedJobs = new ArrayList<>();
    for (Job job : Job.values()) {
      if ((boolean) personEntity.getProperty(job.toString())) qualifiedJobs.add(job);
    }
    if (qualifiedJobs.isEmpty()) {
      return EnumSet.noneOf(Job.class);
    }
    return EnumSet.copyOf(qualifiedJobs);
  }

  public static Person entityToPerson(Entity personEntity) {
    return Person.create(
        (String) personEntity.getProperty("id"),
        (String) personEntity.getProperty("email"),
        (String) personEntity.getProperty("firstName"),
        (String) personEntity.getProperty("lastName"),
        (String) personEntity.getProperty("company"),
        (String) personEntity.getProperty("job"),
        (String) personEntity.getProperty("linkedIn"),
        entityBooleansToEnumSet(personEntity),
        (boolean) personEntity.getProperty("okShadow"));
  }

  public static Entity personToEntity(Person person) {
    Entity personEntity = new Entity("Person", person.id());
    personEntity.setProperty("id", person.id());
    personEntity.setProperty("email", person.email());
    personEntity.setProperty("firstName", person.firstName());
    personEntity.setProperty("lastName", person.lastName());
    personEntity.setProperty("company", person.company());
    personEntity.setProperty("job", person.job());
    personEntity.setProperty("linkedIn", person.linkedIn());
    personEntity.setProperty("okShadow", person.okShadow());
    for (Job job : person.qualifiedJobs()) {
      personEntity.setProperty(job.toString(), true);
    }
    for (Job job : EnumSet.complementOf(person.qualifiedJobs())) {
      personEntity.setProperty(job.toString(), false);
    }
    return personEntity;
  }
}

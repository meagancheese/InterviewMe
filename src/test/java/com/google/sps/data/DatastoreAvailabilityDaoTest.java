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

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;

@RunWith(JUnit4.class)
public class DatastoreAvailabilityDaoTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private DatastoreAvailabilityDao dao;
  private DatastoreService datastore;

  private final Availability availabilityOne =
      Availability.create(
          "user1@mail.com",
          new TimeRange(
              Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
          (long) -1,
          true);

  private final Availability availabilityTwo =
      Availability.create(
          "user1@mail.com",
          new TimeRange(
              Instant.parse("2020-07-07T15:45:00Z"), Instant.parse("2020-07-07T16:00:00Z")),
          (long) -1,
          false);

  private final Availability availabilityThree =
      Availability.create(
          "user2@mail.com",
          new TimeRange(
              Instant.parse("2020-07-07T17:30:00Z"), Instant.parse("2020-07-07T17:45:00Z")),
          (long) -1,
          true);

  private final Availability availabilityFour =
      Availability.create(
          "user1@mail.com",
          new TimeRange(
              Instant.parse("2020-07-07T22:30:00Z"), Instant.parse("2020-07-07T22:45:00Z")),
          (long) -1,
          true);

  @Before
  public void setUp() {
    helper.setUp();
    dao = new DatastoreAvailabilityDao();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Checks that an Availability is stored in datastore.
  @Test
  public void createsAvailability() {
    dao.create(availabilityOne);
    Entity entity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability storedAvailability = dao.entityToAvailability(entity);
    Availability availabilityOneWithID =
        Availability.create(
            availabilityOne.email(),
            availabilityOne.when(),
            storedAvailability.id(),
            availabilityOne.scheduled());
    Assert.assertEquals(availabilityOneWithID, storedAvailability);
  }

  // Checks that an Availability is updated in datastore.
  @Test
  public void updatesAvailability() {
    dao.create(availabilityOne);
    Entity entity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability storedAvailability = dao.entityToAvailability(entity);
    Availability update =
        Availability.create(
            availabilityOne.email(),
            availabilityOne.when(),
            storedAvailability.id(),
            !availabilityOne.scheduled());
    dao.update(update);
    Entity updatedEntity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability updatedAvailability = dao.entityToAvailability(updatedEntity);
    Assert.assertEquals(update, updatedAvailability);
  }

  // Checks that an Availability is returned when it exists within datastore.
  @Test
  public void getsAvailability() {
    dao.create(availabilityTwo);
    Entity entity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability storedAvailability = dao.entityToAvailability(entity);
    Optional<Availability> actualAvailabilityOptional = dao.get(storedAvailability.id());
    Availability expectedAvailability =
        Availability.create(
            availabilityTwo.email(),
            availabilityTwo.when(),
            storedAvailability.id(),
            availabilityTwo.scheduled());
    Optional<Availability> expectedAvailabilityOptional = Optional.of(expectedAvailability);
    Assert.assertEquals(expectedAvailabilityOptional, actualAvailabilityOptional);
  }

  // Checks that an empty Optional is returned when an Availability does not exist within
  // datastore.
  @Test
  public void failsToGetAvailability() {
    Optional<Availability> actual = dao.get(24);
    Optional<Availability> expected = Optional.empty();
    Assert.assertEquals(expected, actual);
  }

  // Checks that the Availability objects within a given time range for a specified user
  // are deleted.
  @Test
  public void deletesInRange() {
    dao.create(availabilityOne);
    dao.create(availabilityTwo);
    dao.create(availabilityFour);
    dao.deleteInRangeForUser(
        "user1@mail.com", availabilityOne.when().start(), availabilityTwo.when().end());
    Entity entity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability actual = dao.entityToAvailability(entity);
    Availability expected =
        Availability.create(
            availabilityFour.email(),
            availabilityFour.when(),
            actual.id(),
            availabilityFour.scheduled());
    Assert.assertEquals(expected, actual);
  }

  // Checks that only the Availability objects for the specified user are deleted within
  // a given time range (and not the Availability objects of other users).
  @Test
  public void deletesUsersAvailabilityInRange() {
    dao.create(availabilityOne);
    dao.create(availabilityTwo);
    dao.create(availabilityThree);
    dao.deleteInRangeForUser(
        "user1@mail.com", availabilityOne.when().start(), availabilityThree.when().end());
    Entity entity = datastore.prepare(new Query("Availability")).asSingleEntity();
    Availability actual = dao.entityToAvailability(entity);
    Availability expected =
        Availability.create(
            availabilityThree.email(),
            availabilityThree.when(),
            actual.id(),
            availabilityThree.scheduled());
    Assert.assertEquals(expected, actual);
  }

  // Checks that all Availability objects for a user within a given time range are returned.
  @Test
  public void getsUsersAvailabilityInRange() {
    dao.create(availabilityOne);
    dao.create(availabilityTwo);
    dao.create(availabilityFour);
    List<Availability> actual =
        dao.getInRangeForUser(
            "user1@mail.com", availabilityOne.when().start(), availabilityTwo.when().end());
    List<Entity> entities =
        datastore
            .prepare(new Query("Availability").addSort("startTime", SortDirection.ASCENDING))
            .asList(FetchOptions.Builder.withDefaults());
    List<Availability> availabilities = new ArrayList<Availability>();
    for (Entity entity : entities) {
      availabilities.add(dao.entityToAvailability(entity));
    }
    Availability expectedAvailabilityOne =
        Availability.create(
            availabilityOne.email(),
            availabilityOne.when(),
            availabilities.get(0).id(),
            availabilityOne.scheduled());
    Availability expectedAvailabilityTwo =
        Availability.create(
            availabilityTwo.email(),
            availabilityTwo.when(),
            availabilities.get(1).id(),
            availabilityTwo.scheduled());
    List<Availability> expectedAvailabilities = new ArrayList<Availability>();
    expectedAvailabilities.add(expectedAvailabilityOne);
    expectedAvailabilities.add(expectedAvailabilityTwo);
    Assert.assertEquals(expectedAvailabilities, actual);
  }

  // Checks that only the Availability objects for the specified user are returned within
  // a given time range (and not the Availability objects of other users).
  @Test
  public void onlyGetsSpecifiedUsersAvailabilityInRange() {
    dao.create(availabilityOne);
    dao.create(availabilityTwo);
    dao.create(availabilityThree);
    List<Availability> actual =
        dao.getInRangeForUser(
            "user1@mail.com", availabilityOne.when().start(), availabilityThree.when().end());
    List<Entity> entities =
        datastore
            .prepare(new Query("Availability").addSort("startTime", SortDirection.ASCENDING))
            .asList(FetchOptions.Builder.withDefaults());
    List<Availability> availabilities = new ArrayList<Availability>();
    for (Entity entity : entities) {
      availabilities.add(dao.entityToAvailability(entity));
    }
    Availability expectedAvailabilityOne =
        Availability.create(
            availabilityOne.email(),
            availabilityOne.when(),
            availabilities.get(0).id(),
            availabilityOne.scheduled());
    Availability expectedAvailabilityTwo =
        Availability.create(
            availabilityTwo.email(),
            availabilityTwo.when(),
            availabilities.get(1).id(),
            availabilityTwo.scheduled());
    List<Availability> expectedAvailabilities = new ArrayList<Availability>();
    expectedAvailabilities.add(expectedAvailabilityOne);
    expectedAvailabilities.add(expectedAvailabilityTwo);
    Assert.assertEquals(expectedAvailabilities, actual);
  }

  // Checks that all of the Availability objects within a given time range are returned.
  @Test
  public void getsAllUsersAvailabilityInRange() {
    dao.create(availabilityOne);
    dao.create(availabilityTwo);
    dao.create(availabilityThree);
    dao.create(availabilityFour);
    List<Availability> actual =
        dao.getInRangeForAll(availabilityOne.when().start(), availabilityThree.when().end());
    List<Entity> entities =
        datastore
            .prepare(new Query("Availability").addSort("startTime", SortDirection.ASCENDING))
            .asList(FetchOptions.Builder.withDefaults());
    List<Availability> availabilities = new ArrayList<Availability>();
    for (Entity entity : entities) {
      availabilities.add(dao.entityToAvailability(entity));
    }
    Availability expectedAvailabilityOne =
        Availability.create(
            availabilityOne.email(),
            availabilityOne.when(),
            availabilities.get(0).id(),
            availabilityOne.scheduled());
    Availability expectedAvailabilityTwo =
        Availability.create(
            availabilityTwo.email(),
            availabilityTwo.when(),
            availabilities.get(1).id(),
            availabilityTwo.scheduled());
    Availability expectedAvailabilityThree =
        Availability.create(
            availabilityThree.email(),
            availabilityThree.when(),
            availabilities.get(2).id(),
            availabilityThree.scheduled());
    List<Availability> expectedAvailabilities = new ArrayList<Availability>();
    expectedAvailabilities.add(expectedAvailabilityOne);
    expectedAvailabilities.add(expectedAvailabilityTwo);
    expectedAvailabilities.add(expectedAvailabilityThree);
    Assert.assertEquals(expectedAvailabilities, actual);
  }

  // Checks that all of the Availability objects within a given time range are returned
  // in ascending order by start time.
  @Test
  public void getsAllUsersAvailabilityInRangeInOrder() {
    dao.create(availabilityFour);
    dao.create(availabilityThree);
    dao.create(availabilityTwo);
    dao.create(availabilityOne);
    List<Availability> actual =
        dao.getInRangeForAll(availabilityOne.when().start(), availabilityThree.when().end());
    List<Entity> entities =
        datastore
            .prepare(new Query("Availability").addSort("startTime", SortDirection.ASCENDING))
            .asList(FetchOptions.Builder.withDefaults());
    List<Availability> availabilities = new ArrayList<Availability>();
    for (Entity entity : entities) {
      availabilities.add(dao.entityToAvailability(entity));
    }
    Availability expectedAvailabilityOne =
        Availability.create(
            availabilityOne.email(),
            availabilityOne.when(),
            availabilities.get(0).id(),
            availabilityOne.scheduled());
    Availability expectedAvailabilityTwo =
        Availability.create(
            availabilityTwo.email(),
            availabilityTwo.when(),
            availabilities.get(1).id(),
            availabilityTwo.scheduled());
    Availability expectedAvailabilityThree =
        Availability.create(
            availabilityThree.email(),
            availabilityThree.when(),
            availabilities.get(2).id(),
            availabilityThree.scheduled());
    List<Availability> expectedAvailabilities = new ArrayList<Availability>();
    expectedAvailabilities.add(expectedAvailabilityOne);
    expectedAvailabilities.add(expectedAvailabilityTwo);
    expectedAvailabilities.add(expectedAvailabilityThree);
    Assert.assertEquals(expectedAvailabilities, actual);
  }
}

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
import java.util.EnumSet;
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
public class DatastorePersonDaoTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private DatastorePersonDao dao;
  private DatastoreService datastore;

  private final Person a =
      Person.create("id_a", "a@gmail.com", "a", "a", "", "", "", EnumSet.of(Job.SOFTWARE_ENGINEER));

  @Before
  public void setUp() {
    helper.setUp();
    dao = new DatastorePersonDao();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Checks that a Person is stored in datastore.
  @Test
  public void createsPerson() {
    dao.create(a);
    Entity entity = datastore.prepare(new Query("Person")).asSingleEntity();
    Person storedPerson = dao.entityToPerson(entity);
    Person personAWithID =
        Person.create(
            storedPerson.id(),
            a.email(),
            a.firstName(),
            a.lastName(),
            a.company(),
            a.job(),
            a.linkedIn(),
            a.qualifiedJobs());
    Assert.assertEquals(personAWithID, storedPerson);
  }

  // Checks that a Person is updated in datastore.
  @Test
  public void updatesPerson() {
    dao.create(a);
    Entity entity = datastore.prepare(new Query("Person")).asSingleEntity();
    Person storedPerson = dao.entityToPerson(entity);
    Person update =
        Person.create(
            storedPerson.id(),
            a.email(),
            a.firstName(),
            a.lastName(),
            a.company(),
            a.job(),
            a.linkedIn(),
            EnumSet.of(Job.BUSINESS_ANALYST, Job.NETWORK_ENGINEER));
    dao.update(update);
    Entity updatedEntity = datastore.prepare(new Query("Person")).asSingleEntity();
    Person updatedPerson = dao.entityToPerson(updatedEntity);
    Assert.assertEquals(update, updatedPerson);
  }

  // Checks that a Person is returned when it exists within datastore.
  @Test
  public void getsPerson() {
    dao.create(a);
    Entity entity = datastore.prepare(new Query("Person")).asSingleEntity();
    Person storedPerson = dao.entityToPerson(entity);
    Optional<Person> actualPersonOptional = dao.get(storedPerson.id());
    Person expectedPerson =
        Person.create(
            storedPerson.id(),
            a.email(),
            a.firstName(),
            a.lastName(),
            a.company(),
            a.job(),
            a.linkedIn(),
            a.qualifiedJobs());
    Optional<Person> expectedPersonOptional = Optional.of(expectedPerson);
    Assert.assertEquals(expectedPersonOptional, actualPersonOptional);
  }

  // Checks that an empty Optional is returned when a Person does not exist within
  // datastore.
  // @Test
  public void failsToGetPerson() {
    Optional<Person> actual = dao.get("$");
    Optional<Person> expected = Optional.empty();
    Assert.assertEquals(expected, actual);
  }
}

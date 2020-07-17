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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DatastoreScheduledInterviewTest {

  DatastoreScheduledInterviewDao dao = new DatastoreScheduledInterviewDao();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private final ScheduledInterview scheduledInterview1 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
          "user@company.org",
          "user@mail.com");

  private final ScheduledInterview scheduledInterview2 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
          "user@company.org",
          "user2@mail.com");

  private final ScheduledInterview scheduledInterview3 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
          "user3@company.org",
          "user2@mail.com");

  private final ScheduledInterview scheduledInterview4 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T20:00:10Z"), Instant.parse("2020-07-06T21:00:10Z")),
          "user@company.org",
          "user2@mail.com");

  private final ScheduledInterview scheduledInterview5 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T21:00:10Z"), Instant.parse("2020-07-06T22:00:10Z")),
          "user@company.org",
          "user3@mail.com");

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Test whether the scheduledInterview was added to datastore.
  @Test
  public void createsAndStoresEntity() {
    dao.create(scheduledInterview1);
    Entity entity = datastore.prepare(new Query("ScheduledInterview")).asSingleEntity();
    ScheduledInterview storedScheduledInterview = dao.entityToScheduledInterview(entity);
    ScheduledInterview copyScheduledInterview1 =
        ScheduledInterview.create(
            storedScheduledInterview.id(),
            scheduledInterview1.when(),
            scheduledInterview1.interviewerEmail(),
            scheduledInterview1.intervieweeEmail());
    Assert.assertEquals(copyScheduledInterview1, storedScheduledInterview);
  }

  // Tests whether all scheduledInterviews for a particular user are returned.
  @Test
  public void getsAllScheduledInterviews() {
    dao.create(scheduledInterview1);
    dao.create(scheduledInterview2);
    dao.create(scheduledInterview3);
    List<ScheduledInterview> result = dao.getForPerson(scheduledInterview1.interviewerEmail());
    ScheduledInterview copyScheduledInterview1 =
        ScheduledInterview.create(
            result.get(0).id(),
            scheduledInterview1.when(),
            scheduledInterview1.interviewerEmail(),
            scheduledInterview1.intervieweeEmail());
    ScheduledInterview copyScheduledInterview2 =
        ScheduledInterview.create(
            result.get(1).id(),
            scheduledInterview2.when(),
            scheduledInterview2.interviewerEmail(),
            scheduledInterview2.intervieweeEmail());
    List<ScheduledInterview> expected = new ArrayList<ScheduledInterview>();
    expected.add(copyScheduledInterview1);
    expected.add(copyScheduledInterview2);
    Assert.assertEquals(expected, result);
  }

  // Tests deleting a user's scheduledInterview.
  @Test
  public void deletesScheduledInterview() {
    dao.create(scheduledInterview1);
    dao.create(scheduledInterview2);
    List<ScheduledInterview> result = dao.getForPerson("user@company.org");
    dao.delete(result.get(0).id());
    Entity entity = datastore.prepare(new Query("ScheduledInterview")).asSingleEntity();
    ScheduledInterview storedScheduledInterview = dao.entityToScheduledInterview(entity);
    ScheduledInterview copyScheduledInterview2 =
        ScheduledInterview.create(
            storedScheduledInterview.id(),
            scheduledInterview2.when(),
            scheduledInterview2.interviewerEmail(),
            scheduledInterview2.intervieweeEmail());
    Assert.assertEquals(copyScheduledInterview2, storedScheduledInterview);
  }

  // Tests updating a user's scheduledInterview.
  @Test
  public void updatesScheduledInterview() {
    dao.create(scheduledInterview1);
    Entity entity = datastore.prepare(new Query("ScheduledInterview")).asSingleEntity();
    ScheduledInterview previousStoredScheduledInterview = dao.entityToScheduledInterview(entity);
    ScheduledInterview updatedStoredScheduledInterview =
        ScheduledInterview.create(
            previousStoredScheduledInterview.id(),
            scheduledInterview2.when(),
            scheduledInterview2.interviewerEmail(),
            scheduledInterview2.intervieweeEmail());
    dao.update(updatedStoredScheduledInterview);
    Entity updatedEntity = datastore.prepare(new Query("ScheduledInterview")).asSingleEntity();
    ScheduledInterview updatedScheduledInterview = dao.entityToScheduledInterview(updatedEntity);
    Assert.assertEquals(updatedStoredScheduledInterview, updatedScheduledInterview);
  }

  // Tests retrieving a scheduledInterview from Datastore.
  @Test
  public void getsScheduledInterview() {
    dao.create(scheduledInterview1);
    Entity entity = datastore.prepare(new Query("ScheduledInterview")).asSingleEntity();
    ScheduledInterview storedScheduledInterview = dao.entityToScheduledInterview(entity);
    Optional<ScheduledInterview> actualScheduledInterviewOptional =
        dao.get(storedScheduledInterview.id());
    ScheduledInterview expectedScheduledInterview =
        ScheduledInterview.create(
            storedScheduledInterview.id(),
            scheduledInterview1.when(),
            scheduledInterview1.interviewerEmail(),
            scheduledInterview1.intervieweeEmail());
    Optional<ScheduledInterview> expectedScheduledInterviewOptional =
        Optional.of(expectedScheduledInterview);
    Assert.assertEquals(expectedScheduledInterviewOptional, actualScheduledInterviewOptional);
  }

  // Tests retrieving a scheduledInterview that doesn't exist from Datastore.
  @Test
  public void failsGetScheduledInterview() {
    Optional<ScheduledInterview> actual = dao.get(2);
    Assert.assertEquals(Optional.empty(), actual);
  }

  // Tests retrieving all scheduledInterviews for a particular user in a certain range
  @Test
  public void getsScheduledInterviewsInRange() {
    dao.create(scheduledInterview1);
    dao.create(scheduledInterview2);
    dao.create(scheduledInterview3);
    dao.create(scheduledInterview4);
    dao.create(scheduledInterview5);
    List<ScheduledInterview> result =
        dao.getScheduledInterviewsInRangeForUser(
            scheduledInterview1.interviewerEmail(),
            scheduledInterview2.when().start().toEpochMilli(),
            scheduledInterview4.when().end().toEpochMilli());
    List<Entity> entities =
        datastore
            .prepare(new Query("ScheduledInterview").addSort("startTime", SortDirection.ASCENDING))
            .asList(FetchOptions.Builder.withDefaults());
    List<ScheduledInterview> scheduledInterviews = new ArrayList<ScheduledInterview>();
    for (Entity entity : entities) {
      scheduledInterviews.add(dao.entityToScheduledInterview(entity));
    }
    ScheduledInterview expectedScheduledInterview1 =
        ScheduledInterview.create(
            scheduledInterviews.get(1).id(),
            scheduledInterview2.when(),
            scheduledInterview2.interviewerEmail(),
            scheduledInterview2.intervieweeEmail());

    ScheduledInterview expectedScheduledInterview2 =
        ScheduledInterview.create(
            scheduledInterviews.get(3).id(),
            scheduledInterview4.when(),
            scheduledInterview4.interviewerEmail(),
            scheduledInterview4.intervieweeEmail());
    List<ScheduledInterview> expected = new ArrayList<ScheduledInterview>();
    expected.add(expectedScheduledInterview1);
    expected.add(expectedScheduledInterview2);

    Assert.assertEquals(expected, result);
  }
}

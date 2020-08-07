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

public class DatastoreScheduledInterviewDaoTest {

  DatastoreScheduledInterviewDao dao = new DatastoreScheduledInterviewDao();

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private final ScheduledInterview scheduledInterview1 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
          "user@company.org",
          "user@mail.com",
          "meet_link",
          Job.TECHNICAL_SALES,
          /*shadowId=*/ "");

  private final ScheduledInterview scheduledInterview2 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
          "user@company.org",
          "user2@mail.com",
          "meet_link",
          Job.PRODUCT_MANAGER,
          /*shadowId=*/ "");

  private final ScheduledInterview scheduledInterview3 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T19:00:10Z"), Instant.parse("2020-07-06T20:00:10Z")),
          "user3@company.org",
          "user2@mail.com",
          "meet_link",
          Job.PRODUCT_MANAGER,
          /*shadowId=*/ "");

  private final ScheduledInterview scheduledInterview4 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T20:00:10Z"), Instant.parse("2020-07-06T21:00:10Z")),
          "user@company.org",
          "user2@mail.com",
          "meet_link",
          Job.PRODUCT_MANAGER,
          /*shadowId=*/ "");

  private final ScheduledInterview scheduledInterview5 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T21:00:10Z"), Instant.parse("2020-07-06T22:00:10Z")),
          "user@company.org",
          "user3@mail.com",
          "meet_link",
          Job.SOFTWARE_ENGINEER,
          /*shadowId=*/ "");
  private final ScheduledInterview scheduledInterview6 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T21:30:10Z"), Instant.parse("2020-07-06T22:30:10Z")),
          "user2@mail.com",
          "user@company.org",
          "meet_link",
          Job.SOFTWARE_ENGINEER,
          /*shadowId=*/ "");
  private final ScheduledInterview scheduledInterview7 =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T22:00:10Z"), Instant.parse("2020-07-06T23:00:10Z")),
          "user2@mail.com",
          "user3@mail.com",
          "meet_link",
          Job.SOFTWARE_ENGINEER,
          /*shadowId=*/ "user@company.org");

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
            scheduledInterview1.interviewerId(),
            scheduledInterview1.intervieweeId(),
            scheduledInterview1.meetLink(),
            scheduledInterview1.position(),
            scheduledInterview1.shadowId());
    Assert.assertEquals(copyScheduledInterview1, storedScheduledInterview);
  }

  // Tests whether all scheduledInterviews for a particular user are retrieved. Tests all 3 roles.
  @Test
  public void getForPerson() {
    dao.create(scheduledInterview5);
    dao.create(scheduledInterview6);
    dao.create(scheduledInterview7);
    List<ScheduledInterview> result = dao.getForPerson(scheduledInterview1.interviewerId());
    ScheduledInterview copyScheduledInterview1 =
        ScheduledInterview.create(
            result.get(0).id(),
            scheduledInterview5.when(),
            scheduledInterview5.interviewerId(),
            scheduledInterview5.intervieweeId(),
            scheduledInterview5.meetLink(),
            scheduledInterview5.position(),
            scheduledInterview5.shadowId());
    ScheduledInterview copyScheduledInterview2 =
        ScheduledInterview.create(
            result.get(1).id(),
            scheduledInterview6.when(),
            scheduledInterview6.interviewerId(),
            scheduledInterview6.intervieweeId(),
            scheduledInterview6.meetLink(),
            scheduledInterview6.position(),
            scheduledInterview6.shadowId());
    ScheduledInterview copyScheduledInterview3 =
        ScheduledInterview.create(
            result.get(2).id(),
            scheduledInterview7.when(),
            scheduledInterview7.interviewerId(),
            scheduledInterview7.intervieweeId(),
            scheduledInterview7.meetLink(),
            scheduledInterview7.position(),
            scheduledInterview7.shadowId());
    List<ScheduledInterview> expected = new ArrayList<ScheduledInterview>();
    expected.add(copyScheduledInterview1);
    expected.add(copyScheduledInterview2);
    expected.add(copyScheduledInterview3);
    Assert.assertEquals(expected, result);
  }

  // Tests that only interviews without a shadow are returned.
  @Test
  public void getsInterviewsWithoutShadow() {
    ScheduledInterview interviewWithAShadow =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-06T21:30:10Z"), Instant.parse("2020-07-06T22:30:10Z")),
            "interviewer",
            "interviewee",
            "meet_link",
            Job.NETWORK_ENGINEER,
            "shadow");
    ScheduledInterview interviewWithoutAShadow =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-06T22:30:10Z"), Instant.parse("2020-07-06T23:30:10Z")),
            "interviewer",
            "interviewee",
            "meet_link",
            Job.NETWORK_ENGINEER,
            "");
    dao.create(interviewWithAShadow);
    dao.create(interviewWithoutAShadow);
    List<ScheduledInterview> actual =
        dao.getForPositionWithoutShadowInRange(
            Job.NETWORK_ENGINEER,
            Instant.parse("2020-07-06T21:30:10Z"),
            Instant.parse("2020-07-06T23:30:10Z"));
    ScheduledInterview expectedInterview =
        ScheduledInterview.create(
            actual.get(0).id(),
            interviewWithoutAShadow.when(),
            interviewWithoutAShadow.interviewerId(),
            interviewWithoutAShadow.intervieweeId(),
            interviewWithoutAShadow.meetLink(),
            interviewWithoutAShadow.position(),
            interviewWithoutAShadow.shadowId());
    List<ScheduledInterview> expected = new ArrayList<ScheduledInterview>();
    expected.add(expectedInterview);
    Assert.assertEquals(expected, actual);
  }

  // Tests that only interviews for the specified position are returned.
  @Test
  public void getsInterviewsForPosition() {
    ScheduledInterview interviewWithRightPosition =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-06T22:30:10Z"), Instant.parse("2020-07-06T23:30:10Z")),
            "interviewer",
            "interviewee",
            "meet_link",
            Job.NETWORK_ENGINEER,
            "");
    ScheduledInterview interviewWithWrongPosition =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-06T22:30:10Z"), Instant.parse("2020-07-06T23:30:10Z")),
            "interviewer",
            "interviewee",
            "meet_link",
            Job.SOFTWARE_ENGINEER,
            "");
    dao.create(interviewWithRightPosition);
    dao.create(interviewWithWrongPosition);
    List<ScheduledInterview> actual =
        dao.getForPositionWithoutShadowInRange(
            Job.NETWORK_ENGINEER,
            Instant.parse("2020-07-06T21:30:10Z"),
            Instant.parse("2020-07-06T23:30:10Z"));
    ScheduledInterview expectedInterview =
        ScheduledInterview.create(
            actual.get(0).id(),
            interviewWithRightPosition.when(),
            interviewWithRightPosition.interviewerId(),
            interviewWithRightPosition.intervieweeId(),
            interviewWithRightPosition.meetLink(),
            interviewWithRightPosition.position(),
            interviewWithRightPosition.shadowId());
    List<ScheduledInterview> expected = new ArrayList<ScheduledInterview>();
    expected.add(expectedInterview);
    Assert.assertEquals(expected, actual);
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
            scheduledInterview2.interviewerId(),
            scheduledInterview2.intervieweeId(),
            scheduledInterview2.meetLink(),
            scheduledInterview2.position(),
            scheduledInterview2.shadowId());
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
            scheduledInterview2.interviewerId(),
            scheduledInterview2.intervieweeId(),
            scheduledInterview2.meetLink(),
            scheduledInterview2.position(),
            scheduledInterview2.shadowId());
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
            scheduledInterview1.interviewerId(),
            scheduledInterview1.intervieweeId(),
            scheduledInterview1.meetLink(),
            scheduledInterview1.position(),
            scheduledInterview1.shadowId());
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
  public void getsScheduledInterviewsInRangeForUser() {
    dao.create(scheduledInterview1);
    dao.create(scheduledInterview2);
    dao.create(scheduledInterview3);
    dao.create(scheduledInterview4);
    dao.create(scheduledInterview5);
    List<ScheduledInterview> result =
        dao.getScheduledInterviewsInRangeForUser(
            scheduledInterview1.interviewerId(),
            scheduledInterview2.when().start(),
            scheduledInterview4.when().end());
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
            scheduledInterview2.interviewerId(),
            scheduledInterview2.intervieweeId(),
            scheduledInterview2.meetLink(),
            scheduledInterview2.position(),
            scheduledInterview2.shadowId());

    ScheduledInterview expectedScheduledInterview2 =
        ScheduledInterview.create(
            scheduledInterviews.get(3).id(),
            scheduledInterview4.when(),
            scheduledInterview4.interviewerId(),
            scheduledInterview4.intervieweeId(),
            scheduledInterview4.meetLink(),
            scheduledInterview4.position(),
            scheduledInterview4.shadowId());
    List<ScheduledInterview> expected = new ArrayList<ScheduledInterview>();
    expected.add(expectedScheduledInterview1);
    expected.add(expectedScheduledInterview2);

    Assert.assertEquals(expected, result);
  }

  // Tests retrieving all scheduledInterviews from Datastore, non-empty result.
  @Test
  public void getsScheduledInterviewsInRange() {
    dao.create(scheduledInterview1);
    dao.create(scheduledInterview2);
    dao.create(scheduledInterview3);
    dao.create(scheduledInterview4);
    dao.create(scheduledInterview5);
    List<ScheduledInterview> result =
        dao.getInRange(scheduledInterview2.when().start(), scheduledInterview4.when().end());
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
            scheduledInterview2.interviewerId(),
            scheduledInterview2.intervieweeId(),
            scheduledInterview2.meetLink(),
            scheduledInterview2.position(),
            scheduledInterview2.shadowId());
    ScheduledInterview expectedScheduledInterview2 =
        ScheduledInterview.create(
            scheduledInterviews.get(2).id(),
            scheduledInterview3.when(),
            scheduledInterview3.interviewerId(),
            scheduledInterview3.intervieweeId(),
            scheduledInterview3.meetLink(),
            scheduledInterview3.position(),
            scheduledInterview3.shadowId());
    ScheduledInterview expectedScheduledInterview3 =
        ScheduledInterview.create(
            scheduledInterviews.get(3).id(),
            scheduledInterview4.when(),
            scheduledInterview4.interviewerId(),
            scheduledInterview4.intervieweeId(),
            scheduledInterview4.meetLink(),
            scheduledInterview4.position(),
            scheduledInterview4.shadowId());
    List<ScheduledInterview> expected = new ArrayList<ScheduledInterview>();
    expected.add(expectedScheduledInterview1);
    expected.add(expectedScheduledInterview2);
    expected.add(expectedScheduledInterview3);

    Assert.assertEquals(expected, result);
  }

  // Tests retrieving all scheduledInterviews from Datastore, empty result.
  @Test
  public void getsScheduledInterviewsInRangeEmpty() {
    dao.create(scheduledInterview1);
    dao.create(scheduledInterview2);
    dao.create(scheduledInterview3);
    dao.create(scheduledInterview4);
    dao.create(scheduledInterview5);
    List<ScheduledInterview> observed =
        dao.getInRange(
            Instant.parse("2020-07-06T11:00:10Z"), Instant.parse("2020-07-06T12:00:10Z"));
    Assert.assertEquals(observed.size(), 0);
  }
}

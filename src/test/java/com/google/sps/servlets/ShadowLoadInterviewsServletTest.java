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

package com.google.sps.servlets;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.Availability;
import com.google.sps.data.FakeAvailabilityDao;
import com.google.sps.data.FakePersonDao;
import com.google.sps.data.FakeScheduledInterviewDao;
import com.google.sps.data.Job;
import com.google.sps.data.Person;
import com.google.sps.data.PossibleInterviewSlot;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import javax.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.Test;
import com.google.gson.JsonSyntaxException;

@RunWith(JUnit4.class)
public final class ShadowLoadInterviewsServletTest {
  LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
  private FakeScheduledInterviewDao scheduledInterviewDao;
  private FakePersonDao personDao;

  @Before
  public void setUp() {
    helper.setUp();
    scheduledInterviewDao = new FakeScheduledInterviewDao();
    personDao = new FakePersonDao();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void tooLargePositiveOffset() {
    ShadowLoadInterviewsServlet servlet = new ShadowLoadInterviewsServlet();
    servlet.init(scheduledInterviewDao, personDao, Instant.now());
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@gmail.com").setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "740");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          servlet.doGet(getRequest, getResponse);
        });
  }

  @Test
  public void tooLargeNegativeOffset() {
    ShadowLoadInterviewsServlet servlet = new ShadowLoadInterviewsServlet();
    servlet.init(scheduledInterviewDao, personDao, Instant.now());
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@gmail.com").setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "-740");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          servlet.doGet(getRequest, getResponse);
        });
  }

  @Test
  public void onlyReturnsInterviewsWhoWantShadows() throws IOException {
    Person personOkWithShadow1 =
        Person.create(
            "person1",
            "person1@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    Person personOkWithShadow2 =
        Person.create(
            "person2",
            "person2@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    Person personNotOkWithShadow =
        Person.create(
            "person3",
            "person3@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ false);
    ScheduledInterview interviewWhoDoesNotWantShadow =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-07T17:00:00Z"), Instant.parse("2020-07-07T18:00:00Z")),
            personOkWithShadow1.id(),
            personNotOkWithShadow.id(),
            "meet_link",
            Job.NETWORK_ENGINEER,
            /*shadowId=*/ "");
    ScheduledInterview interviewWhoWantsShadow =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-07T16:00:00Z"), Instant.parse("2020-07-07T17:00:00Z")),
            personOkWithShadow1.id(),
            personOkWithShadow2.id(),
            "meet_link",
            Job.NETWORK_ENGINEER,
            /*shadowId=*/ "");
    personDao.create(personOkWithShadow1);
    personDao.create(personOkWithShadow2);
    personDao.create(personNotOkWithShadow);
    scheduledInterviewDao.create(interviewWhoDoesNotWantShadow);
    scheduledInterviewDao.create(interviewWhoWantsShadow);

    ShadowLoadInterviewsServlet servlet = new ShadowLoadInterviewsServlet();
    servlet.init(scheduledInterviewDao, personDao, Instant.parse("2020-07-07T16:00:00Z"));
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@gmail.com").setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "0");
    getRequest.addParameter("position", "NETWORK_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<List<PossibleInterviewSlot>> possibleInterviewSlots =
        (List<List<PossibleInterviewSlot>>) getRequest.getAttribute("monthList");
    ImmutableList.Builder<List<PossibleInterviewSlot>> expected = ImmutableList.builder();
    List<PossibleInterviewSlot> day = new ArrayList<PossibleInterviewSlot>();
    PossibleInterviewSlot slot =
        PossibleInterviewSlot.create("2020-07-07T16:00:00Z", "Tuesday 7/7", "4:00 PM - 5:00 PM");
    day.add(slot);
    expected.add(day);
    List<List<PossibleInterviewSlot>> expectedInterviewSlots = expected.build();
    Assert.assertEquals(expectedInterviewSlots, possibleInterviewSlots);
  }

  @Test
  public void onlyReturnsInterviewsUserIsNotAPartOf() throws IOException {
    Person currentUser =
        Person.create(
            String.format("%d", "user@mail.com".hashCode()),
            "user@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    Person otherPerson1 =
        Person.create(
            "person1",
            "person1@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    Person otherPerson2 =
        Person.create(
            "person2",
            "person2@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    ScheduledInterview involvesUser =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-07T17:00:00Z"), Instant.parse("2020-07-07T18:00:00Z")),
            currentUser.id(),
            otherPerson1.id(),
            "meet_link",
            Job.NETWORK_ENGINEER,
            /*shadowId=*/ "");
    ScheduledInterview doesNotInvolveUser =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-07T16:00:00Z"), Instant.parse("2020-07-07T17:00:00Z")),
            otherPerson1.id(),
            otherPerson2.id(),
            "meet_link",
            Job.NETWORK_ENGINEER,
            /*shadowId=*/ "");
    personDao.create(currentUser);
    personDao.create(otherPerson1);
    personDao.create(otherPerson2);
    scheduledInterviewDao.create(involvesUser);
    scheduledInterviewDao.create(doesNotInvolveUser);

    ShadowLoadInterviewsServlet servlet = new ShadowLoadInterviewsServlet();
    servlet.init(scheduledInterviewDao, personDao, Instant.parse("2020-07-07T16:00:00Z"));
    helper.setEnvIsLoggedIn(true).setEnvEmail(currentUser.email()).setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "0");
    getRequest.addParameter("position", "NETWORK_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<List<PossibleInterviewSlot>> possibleInterviewSlots =
        (List<List<PossibleInterviewSlot>>) getRequest.getAttribute("monthList");
    ImmutableList.Builder<List<PossibleInterviewSlot>> expected = ImmutableList.builder();
    List<PossibleInterviewSlot> day = new ArrayList<PossibleInterviewSlot>();
    PossibleInterviewSlot slot =
        PossibleInterviewSlot.create("2020-07-07T16:00:00Z", "Tuesday 7/7", "4:00 PM - 5:00 PM");
    day.add(slot);
    expected.add(day);
    List<List<PossibleInterviewSlot>> expectedInterviewSlots = expected.build();
    Assert.assertEquals(expectedInterviewSlots, possibleInterviewSlots);
  }

  @Test
  public void onlyReturnsInterviewsWithoutAShadow() throws IOException {
    Person shadow =
        Person.create(
            "person1",
            "person1@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    Person interviewer =
        Person.create(
            "person2",
            "person2@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    Person interviewee =
        Person.create(
            "person3",
            "person3@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    ScheduledInterview withShadow =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-07T17:00:00Z"), Instant.parse("2020-07-07T18:00:00Z")),
            interviewer.id(),
            interviewee.id(),
            "meet_link",
            Job.NETWORK_ENGINEER,
            shadow.id());
    ScheduledInterview withoutShadow =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-07T16:00:00Z"), Instant.parse("2020-07-07T17:00:00Z")),
            interviewer.id(),
            interviewee.id(),
            "meet_link",
            Job.NETWORK_ENGINEER,
            /*shadowId=*/ "");
    personDao.create(shadow);
    personDao.create(interviewer);
    personDao.create(interviewee);
    scheduledInterviewDao.create(withShadow);
    scheduledInterviewDao.create(withoutShadow);

    ShadowLoadInterviewsServlet servlet = new ShadowLoadInterviewsServlet();
    servlet.init(scheduledInterviewDao, personDao, Instant.parse("2020-07-07T16:00:00Z"));
    helper.setEnvIsLoggedIn(true).setEnvEmail("userEmail").setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "0");
    getRequest.addParameter("position", "NETWORK_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<List<PossibleInterviewSlot>> possibleInterviewSlots =
        (List<List<PossibleInterviewSlot>>) getRequest.getAttribute("monthList");
    ImmutableList.Builder<List<PossibleInterviewSlot>> expected = ImmutableList.builder();
    List<PossibleInterviewSlot> day = new ArrayList<PossibleInterviewSlot>();
    PossibleInterviewSlot slot =
        PossibleInterviewSlot.create("2020-07-07T16:00:00Z", "Tuesday 7/7", "4:00 PM - 5:00 PM");
    day.add(slot);
    expected.add(day);
    List<List<PossibleInterviewSlot>> expectedInterviewSlots = expected.build();
    Assert.assertEquals(expectedInterviewSlots, possibleInterviewSlots);
  }
}

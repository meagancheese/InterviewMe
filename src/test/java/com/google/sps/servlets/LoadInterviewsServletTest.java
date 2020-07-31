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
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.Test;
import com.google.gson.JsonSyntaxException;

@RunWith(JUnit4.class)
public final class LoadInterviewsServletTest {
  LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
  private FakeAvailabilityDao availabilityDao;
  private FakeScheduledInterviewDao scheduledInterviewDao;
  private FakePersonDao personDao;
  private MockServletContext context;

  private final String qualifiedSWEAndNEEmail = "qualifiedSWEAndNE@mail.com";
  private final String qualifiedSWEAndNEId = String.format("%d", qualifiedSWEAndNEEmail.hashCode());
  private final Person qualifiedSWEAndNE =
      Person.create(
          qualifiedSWEAndNEId,
          qualifiedSWEAndNEEmail,
          "User",
          "Test",
          "Google",
          "SWE",
          "linkedIn",
          EnumSet.of(Job.SOFTWARE_ENGINEER, Job.NETWORK_ENGINEER));
  private final Availability qualifiedSWEAndNEAvail1 =
      Availability.create(
          qualifiedSWEAndNE.id(),
          new TimeRange(
              Instant.parse("2020-07-07T16:30:00Z"), Instant.parse("2020-07-07T16:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability qualifiedSWEAndNEAvail2 =
      Availability.create(
          qualifiedSWEAndNE.id(),
          new TimeRange(
              Instant.parse("2020-07-07T16:45:00Z"), Instant.parse("2020-07-07T17:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability qualifiedSWEAndNEAvail3 =
      Availability.create(
          qualifiedSWEAndNE.id(),
          new TimeRange(
              Instant.parse("2020-07-07T17:00:00Z"), Instant.parse("2020-07-07T17:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability qualifiedSWEAndNEAvail4 =
      Availability.create(
          qualifiedSWEAndNE.id(),
          new TimeRange(
              Instant.parse("2020-07-07T17:15:00Z"), Instant.parse("2020-07-07T17:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);

  private final String qualifiedSWEEmail = "qualifiedSWE@mail.com";
  private final String qualifiedSWEId = String.format("%d", qualifiedSWEEmail.hashCode());
  private final Person qualifiedSWE =
      Person.create(
          qualifiedSWEId,
          qualifiedSWEEmail,
          "User",
          "Test",
          "Google",
          "SWE",
          "linkedIn",
          EnumSet.of(Job.SOFTWARE_ENGINEER));
  private final Availability qualifiedSWEAvail1 =
      Availability.create(
          qualifiedSWE.id(),
          new TimeRange(
              Instant.parse("2020-07-08T16:30:00Z"), Instant.parse("2020-07-08T16:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability qualifiedSWEAvail2 =
      Availability.create(
          qualifiedSWE.id(),
          new TimeRange(
              Instant.parse("2020-07-08T16:45:00Z"), Instant.parse("2020-07-08T17:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability qualifiedSWEAvail3 =
      Availability.create(
          qualifiedSWE.id(),
          new TimeRange(
              Instant.parse("2020-07-08T17:00:00Z"), Instant.parse("2020-07-08T17:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability qualifiedSWEAvail4 =
      Availability.create(
          qualifiedSWE.id(),
          new TimeRange(
              Instant.parse("2020-07-08T17:15:00Z"), Instant.parse("2020-07-08T17:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);

  @Before
  public void setUp() {
    helper.setUp();
    availabilityDao = new FakeAvailabilityDao();
    scheduledInterviewDao = new FakeScheduledInterviewDao();
    personDao = new FakePersonDao();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void tooLargePositiveOffset() {
    LoadInterviewsServlet servlet = new LoadInterviewsServlet();
    servlet.init(availabilityDao, scheduledInterviewDao, personDao, Instant.now());
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@gmail.com").setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "740");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          servlet.doGet(getRequest, getResponse);
        });
  }

  @Test
  public void tooLargeNegativeOffset() {
    LoadInterviewsServlet servlet = new LoadInterviewsServlet();
    servlet.init(availabilityDao, scheduledInterviewDao, personDao, Instant.now());
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@gmail.com").setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "-740");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          servlet.doGet(getRequest, getResponse);
        });
  }

  @Test
  public void onlyReturnsHourLongSlots() throws IOException {
    LoadInterviewsServlet servlet = new LoadInterviewsServlet();
    servlet.init(
        availabilityDao, scheduledInterviewDao, personDao, Instant.parse("2020-07-07T13:15:00Z"));
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");
    personDao.create(qualifiedSWEAndNE);
    // A 15 minute slot
    availabilityDao.create(
        Availability.create(
            qualifiedSWEAndNE.id(),
            new TimeRange(
                Instant.parse("2020-07-07T13:30:00Z"), Instant.parse("2020-07-07T13:45:00Z")),
            /*id=*/ -1,
            false));
    // A 30 minute slot
    availabilityDao.create(
        Availability.create(
            qualifiedSWEAndNE.id(),
            new TimeRange(
                Instant.parse("2020-07-07T14:30:00Z"), Instant.parse("2020-07-07T14:45:00Z")),
            /*id=*/ -1,
            false));
    availabilityDao.create(
        Availability.create(
            qualifiedSWEAndNE.id(),
            new TimeRange(
                Instant.parse("2020-07-07T14:45:00Z"), Instant.parse("2020-07-07T15:00:00Z")),
            /*id=*/ -1,
            false));
    // A 45 minute slot
    availabilityDao.create(
        Availability.create(
            qualifiedSWEAndNE.id(),
            new TimeRange(
                Instant.parse("2020-07-07T15:30:00Z"), Instant.parse("2020-07-07T15:45:00Z")),
            /*id=*/ -1,
            false));
    availabilityDao.create(
        Availability.create(
            qualifiedSWEAndNE.id(),
            new TimeRange(
                Instant.parse("2020-07-07T15:45:00Z"), Instant.parse("2020-07-07T16:00:00Z")),
            /*id=*/ -1,
            false));
    availabilityDao.create(
        Availability.create(
            qualifiedSWEAndNE.id(),
            new TimeRange(
                Instant.parse("2020-07-07T16:00:00Z"), Instant.parse("2020-07-07T16:15:00Z")),
            /*id=*/ -1,
            false));
    // An hour slot
    availabilityDao.create(qualifiedSWEAndNEAvail1);
    availabilityDao.create(qualifiedSWEAndNEAvail2);
    availabilityDao.create(qualifiedSWEAndNEAvail3);
    availabilityDao.create(qualifiedSWEAndNEAvail4);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "60");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<List<PossibleInterviewSlot>> possibleInterviewSlots =
        (List<List<PossibleInterviewSlot>>) getRequest.getAttribute("weekList");
    ImmutableList.Builder<List<PossibleInterviewSlot>> expected = ImmutableList.builder();
    List<PossibleInterviewSlot> day = new ArrayList<PossibleInterviewSlot>();
    PossibleInterviewSlot slot =
        PossibleInterviewSlot.create("2020-07-07T16:30:00Z", "Tuesday 7/7", "5:30 PM - 6:30 PM");
    day.add(slot);
    expected.add(day);
    List<List<PossibleInterviewSlot>> expectedInterviewSlots = expected.build();
    Assert.assertEquals(expectedInterviewSlots, possibleInterviewSlots);
  }

  @Test
  public void onlyReturnsUnscheduledSlots() throws IOException {
    LoadInterviewsServlet servlet = new LoadInterviewsServlet();
    servlet.init(
        availabilityDao, scheduledInterviewDao, personDao, Instant.parse("2020-07-07T13:15:00Z"));
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");
    personDao.create(qualifiedSWEAndNE);
    // A scheduled hour slot
    availabilityDao.create(qualifiedSWEAndNEAvail1.withScheduled(true));
    availabilityDao.create(qualifiedSWEAndNEAvail2.withScheduled(true));
    availabilityDao.create(qualifiedSWEAndNEAvail3.withScheduled(true));
    availabilityDao.create(qualifiedSWEAndNEAvail4.withScheduled(true));
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "0");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<List<PossibleInterviewSlot>> possibleInterviewSlots =
        (List<List<PossibleInterviewSlot>>) getRequest.getAttribute("weekList");
    ImmutableList.Builder<List<PossibleInterviewSlot>> expected = ImmutableList.builder();
    List<List<PossibleInterviewSlot>> expectedInterviewSlots = expected.build();
    Assert.assertEquals(expectedInterviewSlots, possibleInterviewSlots);
  }

  @Test
  public void removesInterviewsThatConflictWithUser() throws IOException {
    LoadInterviewsServlet servlet = new LoadInterviewsServlet();
    servlet.init(
        availabilityDao, scheduledInterviewDao, personDao, Instant.parse("2020-07-07T13:15:00Z"));
    String userEmail = "person@gmail.com";
    helper.setEnvIsLoggedIn(true).setEnvEmail(userEmail).setEnvAuthDomain("auth");
    personDao.create(qualifiedSWEAndNE);
    // A scheduled interview for the current user that overlaps with the only possible interviewer
    // time.
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-07T16:00:00Z"), Instant.parse("2020-07-07T17:00:00Z")),
            "interviewerId",
            String.format("%d", userEmail.hashCode())));
    // An unscheduled hour slot for an interviewer
    availabilityDao.create(qualifiedSWEAndNEAvail1);
    availabilityDao.create(qualifiedSWEAndNEAvail2);
    availabilityDao.create(qualifiedSWEAndNEAvail3);
    availabilityDao.create(qualifiedSWEAndNEAvail4);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "0");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<List<PossibleInterviewSlot>> possibleInterviewSlots =
        (List<List<PossibleInterviewSlot>>) getRequest.getAttribute("weekList");
    ImmutableList.Builder<List<PossibleInterviewSlot>> expected = ImmutableList.builder();
    List<List<PossibleInterviewSlot>> expectedInterviewSlots = expected.build();
    Assert.assertEquals(expectedInterviewSlots, possibleInterviewSlots);
  }

  @Test
  public void noSchedulingWithYourself() throws IOException {
    LoadInterviewsServlet servlet = new LoadInterviewsServlet();
    servlet.init(
        availabilityDao, scheduledInterviewDao, personDao, Instant.parse("2020-07-07T13:15:00Z"));
    helper.setEnvIsLoggedIn(true).setEnvEmail(qualifiedSWEAndNE.email()).setEnvAuthDomain("auth");
    personDao.create(qualifiedSWEAndNE);
    // An hour of the user's availability
    availabilityDao.create(qualifiedSWEAndNEAvail1);
    availabilityDao.create(qualifiedSWEAndNEAvail2);
    availabilityDao.create(qualifiedSWEAndNEAvail3);
    availabilityDao.create(qualifiedSWEAndNEAvail4);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "0");
    getRequest.addParameter("position", "NETWORK_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<List<PossibleInterviewSlot>> possibleInterviewSlots =
        (List<List<PossibleInterviewSlot>>) getRequest.getAttribute("weekList");
    ImmutableList.Builder<List<PossibleInterviewSlot>> expected = ImmutableList.builder();
    List<List<PossibleInterviewSlot>> expectedInterviewSlots = expected.build();
    Assert.assertEquals(expectedInterviewSlots, possibleInterviewSlots);
  }

  @Test
  public void possibleInterviewSlotsAreSorted() throws IOException {
    LoadInterviewsServlet servlet = new LoadInterviewsServlet();
    servlet.init(
        availabilityDao, scheduledInterviewDao, personDao, Instant.parse("2020-07-07T13:15:00Z"));
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");
    personDao.create(qualifiedSWEAndNE);
    // An hour and 15 minute slot
    availabilityDao.create(qualifiedSWEAndNEAvail1);
    availabilityDao.create(qualifiedSWEAndNEAvail2);
    availabilityDao.create(qualifiedSWEAndNEAvail3);
    availabilityDao.create(qualifiedSWEAndNEAvail4);
    availabilityDao.create(
        Availability.create(
            qualifiedSWEAndNE.id(),
            new TimeRange(
                Instant.parse("2020-07-07T17:30:00Z"), Instant.parse("2020-07-07T17:45:00Z")),
            /*id=*/ -1,
            false));
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "0");
    getRequest.addParameter("position", "NETWORK_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<ArrayList<PossibleInterviewSlot>> possibleInterviewSlots =
        (List<ArrayList<PossibleInterviewSlot>>) getRequest.getAttribute("weekList");
    List<ArrayList<PossibleInterviewSlot>> expected =
        new ArrayList<ArrayList<PossibleInterviewSlot>>();
    PossibleInterviewSlot slot1 =
        PossibleInterviewSlot.create("2020-07-07T16:30:00Z", "Tuesday 7/7", "4:30 PM - 5:30 PM");
    PossibleInterviewSlot slot2 =
        PossibleInterviewSlot.create("2020-07-07T16:45:00Z", "Tuesday 7/7", "4:45 PM - 5:45 PM");
    ArrayList<PossibleInterviewSlot> day = new ArrayList<PossibleInterviewSlot>();
    day.add(slot1);
    day.add(slot2);
    expected.add(day);
    Assert.assertEquals(expected, possibleInterviewSlots);
  }

  @Test
  public void daysAreSorted() throws IOException {
    LoadInterviewsServlet servlet = new LoadInterviewsServlet();
    servlet.init(
        availabilityDao, scheduledInterviewDao, personDao, Instant.parse("2020-07-07T13:15:00Z"));
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");
    personDao.create(qualifiedSWEAndNE);
    personDao.create(qualifiedSWE);
    // An hour slot on 7/7
    availabilityDao.create(qualifiedSWEAndNEAvail1);
    availabilityDao.create(qualifiedSWEAndNEAvail2);
    availabilityDao.create(qualifiedSWEAndNEAvail3);
    availabilityDao.create(qualifiedSWEAndNEAvail4);
    // An hour slot on 7/8
    availabilityDao.create(qualifiedSWEAvail1);
    availabilityDao.create(qualifiedSWEAvail2);
    availabilityDao.create(qualifiedSWEAvail3);
    availabilityDao.create(qualifiedSWEAvail4);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "0");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<ArrayList<PossibleInterviewSlot>> possibleInterviewSlots =
        (List<ArrayList<PossibleInterviewSlot>>) getRequest.getAttribute("weekList");
    List<ArrayList<PossibleInterviewSlot>> expected =
        new ArrayList<ArrayList<PossibleInterviewSlot>>();
    PossibleInterviewSlot slot1 =
        PossibleInterviewSlot.create("2020-07-07T16:30:00Z", "Tuesday 7/7", "4:30 PM - 5:30 PM");
    PossibleInterviewSlot slot2 =
        PossibleInterviewSlot.create("2020-07-08T16:30:00Z", "Wednesday 7/8", "4:30 PM - 5:30 PM");
    ArrayList<PossibleInterviewSlot> day1 = new ArrayList<PossibleInterviewSlot>();
    day1.add(slot1);
    ArrayList<PossibleInterviewSlot> day2 = new ArrayList<PossibleInterviewSlot>();
    day2.add(slot2);
    expected.add(day1);
    expected.add(day2);
    Assert.assertEquals(expected, possibleInterviewSlots);
  }

  @Test
  public void onlyReturnsQualifiedInterviewerTimes() throws IOException {
    LoadInterviewsServlet servlet = new LoadInterviewsServlet();
    servlet.init(
        availabilityDao, scheduledInterviewDao, personDao, Instant.parse("2020-07-07T13:15:00Z"));
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");
    personDao.create(qualifiedSWEAndNE);
    personDao.create(qualifiedSWE);
    availabilityDao.create(qualifiedSWEAndNEAvail1);
    availabilityDao.create(qualifiedSWEAndNEAvail2);
    availabilityDao.create(qualifiedSWEAndNEAvail3);
    availabilityDao.create(qualifiedSWEAndNEAvail4);
    availabilityDao.create(qualifiedSWEAvail1);
    availabilityDao.create(qualifiedSWEAvail2);
    availabilityDao.create(qualifiedSWEAvail3);
    availabilityDao.create(qualifiedSWEAvail4);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("timeZoneOffset", "0");
    getRequest.addParameter("position", "NETWORK_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    List<ArrayList<PossibleInterviewSlot>> possibleInterviewSlots =
        (List<ArrayList<PossibleInterviewSlot>>) getRequest.getAttribute("weekList");
    List<ArrayList<PossibleInterviewSlot>> expected =
        new ArrayList<ArrayList<PossibleInterviewSlot>>();
    PossibleInterviewSlot slot =
        PossibleInterviewSlot.create("2020-07-07T16:30:00Z", "Tuesday 7/7", "4:30 PM - 5:30 PM");
    ArrayList<PossibleInterviewSlot> day = new ArrayList<PossibleInterviewSlot>();
    day.add(slot);
    expected.add(day);
    Assert.assertEquals(expected, possibleInterviewSlots);
  }
}

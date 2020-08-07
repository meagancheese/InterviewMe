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
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.Availability;
import com.google.sps.data.FakeAvailabilityDao;
import com.google.sps.data.FakePersonDao;
import com.google.sps.data.Job;
import com.google.sps.data.Person;
import com.google.sps.data.PossibleInterviewer;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;

@RunWith(JUnit4.class)
public final class ShowInterviewersServletTest {
  LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
  private FakeAvailabilityDao availabilityDao;
  private FakePersonDao personDao;

  private final String googleSWE1Email = "googleSWE1@mail";
  private final String googleSWE1Id = String.format("%d", googleSWE1Email.hashCode());
  private final Person googleSWE1 =
      Person.create(
          googleSWE1Id,
          googleSWE1Email,
          "Test",
          "Subject",
          "Google",
          "SWE",
          "linkedIn",
          EnumSet.of(Job.SOFTWARE_ENGINEER),
          /*okShadow=*/ true);
  private final Availability googleSWE1Avail1 =
      Availability.create(
          googleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-07T13:30:00Z"), Instant.parse("2020-07-07T13:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE1Avail2 =
      Availability.create(
          googleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-07T13:45:00Z"), Instant.parse("2020-07-07T14:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE1Avail3 =
      Availability.create(
          googleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-07T14:00:00Z"), Instant.parse("2020-07-07T14:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE1Avail4 =
      Availability.create(
          googleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-07T14:15:00Z"), Instant.parse("2020-07-07T14:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);

  private final String googleSWE2Email = "googleSWE2@mail";
  private final String googleSWE2Id = String.format("%d", googleSWE2Email.hashCode());
  private final Person googleSWE2 =
      Person.create(
          googleSWE2Id,
          googleSWE2Email,
          "Test",
          "Subject",
          "Google",
          "SWE",
          "linkedIn",
          EnumSet.of(Job.SOFTWARE_ENGINEER),
          /*okShadow=*/ true);
  private final Availability googleSWE2Avail1 =
      Availability.create(
          googleSWE2.id(),
          new TimeRange(
              Instant.parse("2020-07-07T13:30:00Z"), Instant.parse("2020-07-07T13:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE2Avail2 =
      Availability.create(
          googleSWE2.id(),
          new TimeRange(
              Instant.parse("2020-07-07T13:45:00Z"), Instant.parse("2020-07-07T14:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE2Avail3 =
      Availability.create(
          googleSWE2.id(),
          new TimeRange(
              Instant.parse("2020-07-07T14:00:00Z"), Instant.parse("2020-07-07T14:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE2Avail4 =
      Availability.create(
          googleSWE2.id(),
          new TimeRange(
              Instant.parse("2020-07-07T14:15:00Z"), Instant.parse("2020-07-07T14:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);

  private final String googleNEEmail = "googleNE@mail";
  private final String googleNEId = String.format("%d", googleNEEmail.hashCode());
  private final Person googleNE =
      Person.create(
          googleNEId,
          googleNEEmail,
          "Test",
          "Subject",
          "Google",
          "NE",
          "linkedIn",
          EnumSet.of(Job.NETWORK_ENGINEER),
          /*okShadow=*/ true);
  private final Availability googleNEAvail1 =
      Availability.create(
          googleNE.id(),
          new TimeRange(
              Instant.parse("2020-07-07T13:30:00Z"), Instant.parse("2020-07-07T13:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleNEAvail2 =
      Availability.create(
          googleNE.id(),
          new TimeRange(
              Instant.parse("2020-07-07T13:45:00Z"), Instant.parse("2020-07-07T14:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleNEAvail3 =
      Availability.create(
          googleNE.id(),
          new TimeRange(
              Instant.parse("2020-07-07T14:00:00Z"), Instant.parse("2020-07-07T14:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleNEAvail4 =
      Availability.create(
          googleNE.id(),
          new TimeRange(
              Instant.parse("2020-07-07T14:15:00Z"), Instant.parse("2020-07-07T14:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);

  @Before
  public void setUp() {
    helper.setUp();
    availabilityDao = new FakeAvailabilityDao();
    personDao = new FakePersonDao();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void onlyReturnsInterviewersWhoAreAvailableForTheFullHour() throws IOException {
    personDao.create(googleSWE1);
    ShowInterviewersServlet servlet = new ShowInterviewersServlet();
    servlet.init(availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@gmail.com").setEnvAuthDomain("auth");
    availabilityDao.create(googleSWE1Avail1);
    availabilityDao.create(googleSWE1Avail2);
    availabilityDao.create(googleSWE1Avail3);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("utcStartTime", "2020-07-07T13:30:00Z");
    getRequest.addParameter("date", "Tuesday 7/7");
    getRequest.addParameter("time", "1:30 PM - 2:30 PM");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    Set<PossibleInterviewer> actual =
        (Set<PossibleInterviewer>) getRequest.getAttribute("interviewers");
    Assert.assertEquals(new HashSet<PossibleInterviewer>(), actual);
  }

  @Test
  public void onlyReturnsInterviewersWhoAreNotScheduled() throws IOException {
    personDao.create(googleSWE1);
    ShowInterviewersServlet servlet = new ShowInterviewersServlet();
    servlet.init(availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");
    availabilityDao.create(googleSWE1Avail1.withScheduled(true));
    availabilityDao.create(googleSWE1Avail2.withScheduled(true));
    availabilityDao.create(googleSWE1Avail3.withScheduled(true));
    availabilityDao.create(googleSWE1Avail4.withScheduled(true));
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("utcStartTime", "2020-07-07T13:30:00Z");
    getRequest.addParameter("date", "Tuesday 7/7");
    getRequest.addParameter("time", "1:30 PM - 2:30 PM");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    Set<PossibleInterviewer> actual =
        (Set<PossibleInterviewer>) getRequest.getAttribute("interviewers");
    Assert.assertEquals(new HashSet<PossibleInterviewer>(), actual);
  }

  @Test
  public void noSchedulingWithYourself() throws IOException {
    ShowInterviewersServlet servlet = new ShowInterviewersServlet();
    servlet.init(availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail(googleSWE1.email()).setEnvAuthDomain("auth");
    availabilityDao.create(googleSWE1Avail1);
    availabilityDao.create(googleSWE1Avail2);
    availabilityDao.create(googleSWE1Avail3);
    availabilityDao.create(googleSWE1Avail4);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("utcStartTime", "2020-07-07T13:30:00Z");
    getRequest.addParameter("date", "Tuesday 7/7");
    getRequest.addParameter("time", "1:30 PM - 2:30 PM");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    Set<PossibleInterviewer> actual =
        (Set<PossibleInterviewer>) getRequest.getAttribute("interviewers");
    Assert.assertEquals(new HashSet<PossibleInterviewer>(), actual);
  }

  @Test
  public void successfulRun() throws IOException {
    personDao.create(googleSWE1);
    ShowInterviewersServlet servlet = new ShowInterviewersServlet();
    servlet.init(availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");
    availabilityDao.create(googleSWE1Avail1);
    availabilityDao.create(googleSWE1Avail2);
    availabilityDao.create(googleSWE1Avail3);
    availabilityDao.create(googleSWE1Avail4);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("utcStartTime", "2020-07-07T13:30:00Z");
    getRequest.addParameter("date", "Tuesday 7/7");
    getRequest.addParameter("time", "1:30 PM - 2:30 PM");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    Set<PossibleInterviewer> actual =
        (Set<PossibleInterviewer>) getRequest.getAttribute("interviewers");
    Set<PossibleInterviewer> expected = new HashSet<PossibleInterviewer>();
    PossibleInterviewer googleSWE1Details =
        PossibleInterviewer.create(googleSWE1.company(), googleSWE1.job());
    expected.add(googleSWE1Details);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noRepeatsOfCompanyAndJob() throws IOException {
    personDao.create(googleSWE1);
    personDao.create(googleSWE2);
    ShowInterviewersServlet servlet = new ShowInterviewersServlet();
    servlet.init(availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");
    availabilityDao.create(googleSWE1Avail1);
    availabilityDao.create(googleSWE1Avail2);
    availabilityDao.create(googleSWE1Avail3);
    availabilityDao.create(googleSWE1Avail4);
    availabilityDao.create(googleSWE2Avail1);
    availabilityDao.create(googleSWE2Avail2);
    availabilityDao.create(googleSWE2Avail3);
    availabilityDao.create(googleSWE2Avail4);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("utcStartTime", "2020-07-07T13:30:00Z");
    getRequest.addParameter("date", "Tuesday 7/7");
    getRequest.addParameter("time", "1:30 PM - 2:30 PM");
    getRequest.addParameter("position", "SOFTWARE_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    Set<PossibleInterviewer> actual =
        (Set<PossibleInterviewer>) getRequest.getAttribute("interviewers");
    Set<PossibleInterviewer> expected = new HashSet<PossibleInterviewer>();
    PossibleInterviewer googleSWE1Details =
        PossibleInterviewer.create(googleSWE1.company(), googleSWE1.job());
    expected.add(googleSWE1Details);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void onlyReturnsQualifiedInterviewers() throws IOException {
    personDao.create(googleSWE1);
    personDao.create(googleNE);
    ShowInterviewersServlet servlet = new ShowInterviewersServlet();
    servlet.init(availabilityDao, personDao);
    helper.setEnvIsLoggedIn(true).setEnvEmail("person@gmail.com").setEnvAuthDomain("auth");
    availabilityDao.create(googleSWE1Avail1);
    availabilityDao.create(googleSWE1Avail2);
    availabilityDao.create(googleSWE1Avail3);
    availabilityDao.create(googleSWE1Avail4);
    availabilityDao.create(googleNEAvail1);
    availabilityDao.create(googleNEAvail2);
    availabilityDao.create(googleNEAvail3);
    availabilityDao.create(googleNEAvail4);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("utcStartTime", "2020-07-07T13:30:00Z");
    getRequest.addParameter("date", "Tuesday 7/7");
    getRequest.addParameter("time", "1:30 PM - 2:30 PM");
    getRequest.addParameter("position", "NETWORK_ENGINEER");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    servlet.doGet(getRequest, getResponse);
    Set<PossibleInterviewer> actual =
        (Set<PossibleInterviewer>) getRequest.getAttribute("interviewers");
    Set<PossibleInterviewer> expected = new HashSet<PossibleInterviewer>();
    PossibleInterviewer googleNEDetails =
        PossibleInterviewer.create(googleNE.company(), googleNE.job());
    expected.add(googleNEDetails);
    Assert.assertEquals(expected, actual);
  }
}

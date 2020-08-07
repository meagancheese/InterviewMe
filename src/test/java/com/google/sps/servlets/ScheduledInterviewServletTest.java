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

import com.google.appengine.tools.development.testing.LocalCapabilitiesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.Availability;
import com.google.sps.data.CalendarAccess;
import com.google.sps.data.FakeAvailabilityDao;
import com.google.sps.data.FakeCalendarAccess;
import com.google.sps.data.FakeEmailSender;
import com.google.sps.data.FakePersonDao;
import com.google.sps.data.FakeScheduledInterviewDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewRequest;
import com.google.sps.data.TimeRange;
import com.google.sps.utils.EmailUtils;
import com.google.sps.utils.FakeEmailUtils;
import com.google.gson.reflect.TypeToken;
import com.google.sps.data.Job;
import com.google.sps.data.Person;
import com.google.sps.servlets.ScheduledInterviewServlet;
import com.google.sps.data.PutAvailabilityRequest;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;
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
import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public final class ScheduledInterviewServletTest {
  LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalCapabilitiesServiceTestConfig());
  private FakeScheduledInterviewDao scheduledInterviewDao;
  private FakeAvailabilityDao availabilityDao;
  private FakePersonDao personDao;
  private FakeCalendarAccess calendarAccess;
  private FakeEmailSender emailSender;
  private FakeEmailUtils emailUtils;

  private final Person googleSWE1 =
      Person.create(
          emailToId("user1@mail"),
          "user1@mail",
          "User",
          "Test",
          "Google",
          "SWE",
          "linkedIn",
          EnumSet.of(Job.SOFTWARE_ENGINEER),
          /*okShadow=*/ true);
  private final Availability googleSWE1Avail1 =
      Availability.create(
          googleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE1Avail2 =
      Availability.create(
          googleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:00:00Z"), Instant.parse("2020-07-20T13:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE1Avail3 =
      Availability.create(
          googleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:15:00Z"), Instant.parse("2020-07-20T13:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE1Avail4 =
      Availability.create(
          googleSWE1.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:30:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);

  private final Person googleSWE2QualPMInterviewer =
      Person.create(
          emailToId("user2@mail"),
          "user2@mail",
          "User",
          "Test",
          "Google",
          "SWE",
          "linkedIn",
          EnumSet.of(Job.SOFTWARE_ENGINEER, Job.PRODUCT_MANAGER),
          /*okShadow=*/ true);
  private final Availability googleSWE2QualPMInterviewerAvail1 =
      Availability.create(
          googleSWE2QualPMInterviewer.id(),
          new TimeRange(
              Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE2QualPMInterviewerAvail2 =
      Availability.create(
          googleSWE2QualPMInterviewer.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:00:00Z"), Instant.parse("2020-07-20T13:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE2QualPMInterviewerAvail3 =
      Availability.create(
          googleSWE2QualPMInterviewer.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:15:00Z"), Instant.parse("2020-07-20T13:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googleSWE2QualPMInterviewerAvail4 =
      Availability.create(
          googleSWE2QualPMInterviewer.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:30:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);

  private final Person googlePM =
      Person.create(
          emailToId("user3@mail"),
          "user3@mail",
          "User",
          "Test",
          "Google",
          "PM",
          "linkedIn",
          EnumSet.of(Job.SOFTWARE_ENGINEER, Job.PRODUCT_MANAGER),
          /*okShadow=*/ true);
  private final Availability googlePMAvail1 =
      Availability.create(
          googlePM.id(),
          new TimeRange(
              Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:00:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googlePMAvail2 =
      Availability.create(
          googlePM.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:00:00Z"), Instant.parse("2020-07-20T13:15:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googlePMAvail3 =
      Availability.create(
          googlePM.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:15:00Z"), Instant.parse("2020-07-20T13:30:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Availability googlePMAvail4 =
      Availability.create(
          googlePM.id(),
          new TimeRange(
              Instant.parse("2020-07-20T13:30:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
          /*id=*/ -1,
          /*scheduled=*/ false);
  private final Person shadow =
      Person.create(
          emailToId("shadow@gmail.com"),
          "shadow@gmail.com",
          "shadow_first_name",
          "shadow_last_name",
          "shadow_company",
          "shadow_job",
          "shadow_linkedIn",
          EnumSet.noneOf(Job.class),
          /*okShadow=*/ true);

  @Before
  public void setUp() {
    helper.setUp();
    try {
      emailSender = new FakeEmailSender(new Email("interviewme.business@gmail.com"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    scheduledInterviewDao = new FakeScheduledInterviewDao();
    availabilityDao = new FakeAvailabilityDao();
    personDao = new FakePersonDao();
    calendarAccess = new FakeCalendarAccess();
    emailUtils = new FakeEmailUtils();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Tests whether only the scheduled interviews that the current user is involved in are returned.
  @Test
  public void returnsScheduledInterviewsForUser() throws IOException {
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(
        scheduledInterviewDao, availabilityDao, personDao, calendarAccess, emailSender, emailUtils);
    helper.setEnvIsLoggedIn(true).setEnvEmail(googleSWE1.email()).setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personDao.create(googleSWE1);
    personDao.create(googleSWE2QualPMInterviewer);
    personDao.create(googlePM);
    personDao.create(shadow);
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-05T18:00:00Z"), Instant.parse("2020-07-05T19:00:00Z")),
            googleSWE1.id(),
            googleSWE2QualPMInterviewer.id(),
            "meet_link",
            Job.PRODUCT_MANAGER,
            shadow.id()));
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-05T20:00:00Z"), Instant.parse("2020-07-05T21:00:00Z")),
            googleSWE2QualPMInterviewer.id(),
            googlePM.id(),
            "meet_link",
            Job.SOFTWARE_ENGINEER,
            /*shadowId=*/ ""));
    getRequest.addParameter("timeZone", "America/New_York");
    getRequest.addParameter("userTime", "2020-07-05T22:00:00Z");
    scheduledInterviewServlet.doGet(getRequest, getResponse);
    List<ScheduledInterviewRequest> actual =
        (List<ScheduledInterviewRequest>) getRequest.getAttribute("scheduledInterviews");
    ScheduledInterviewRequest expectedInterview =
        new ScheduledInterviewRequest(
            actual.get(0).getId(),
            "Sunday, July 5, 2020 from 2:00 PM to 3:00 PM",
            googleSWE1.firstName(),
            googleSWE2QualPMInterviewer.firstName(),
            "Interviewer",
            /*hasStarted=*/ true,
            "meet_link",
            Job.PRODUCT_MANAGER.name(),
            shadow.firstName());
    List<ScheduledInterviewRequest> expected = new ArrayList<ScheduledInterviewRequest>();
    expected.add(expectedInterview);
    Assert.assertEquals(expected, actual);
  }

  // Tests that the list of scheduledInterviews is in the correct order
  @Test
  public void returnsScheduledInterviewsInOrder() throws IOException {
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(
        scheduledInterviewDao, availabilityDao, personDao, calendarAccess, emailSender, emailUtils);
    helper.setEnvIsLoggedIn(true).setEnvEmail(googleSWE1.email()).setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personDao.create(googleSWE1);
    personDao.create(googleSWE2QualPMInterviewer);
    personDao.create(googlePM);
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-05T18:00:00Z"), Instant.parse("2020-07-05T19:00:00Z")),
            googleSWE1.id(),
            googleSWE2QualPMInterviewer.id(),
            "meet_link",
            Job.PRODUCT_MANAGER,
            /*shadowId=*/ ""));
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-05T20:00:00Z"), Instant.parse("2020-07-05T21:00:00Z")),
            googleSWE1.id(),
            googleSWE2QualPMInterviewer.id(),
            "meet_link",
            Job.PRODUCT_MANAGER,
            /*shadowId=*/ ""));
    getRequest.addParameter("timeZone", "Etc/UCT");
    getRequest.addParameter("userTime", "2020-07-05T22:00:00Z");
    scheduledInterviewServlet.doGet(getRequest, getResponse);
    List<ScheduledInterviewRequest> actual =
        (List<ScheduledInterviewRequest>) getRequest.getAttribute("scheduledInterviews");
    ScheduledInterviewRequest scheduledInterview1 =
        new ScheduledInterviewRequest(
            actual.get(0).getId(),
            "Sunday, July 5, 2020 from 6:00 PM to 7:00 PM",
            googleSWE1.firstName(),
            googleSWE2QualPMInterviewer.firstName(),
            "Interviewer",
            true,
            "meet_link",
            Job.PRODUCT_MANAGER.name(),
            "None");
    ScheduledInterviewRequest scheduledInterview2 =
        new ScheduledInterviewRequest(
            actual.get(1).getId(),
            "Sunday, July 5, 2020 from 8:00 PM to 9:00 PM",
            googleSWE1.firstName(),
            googleSWE2QualPMInterviewer.firstName(),
            "Interviewer",
            true,
            "meet_link",
            Job.PRODUCT_MANAGER.name(),
            "None");
    List<ScheduledInterviewRequest> expected = new ArrayList<ScheduledInterviewRequest>();
    expected.add(scheduledInterview1);
    expected.add(scheduledInterview2);
    // Used assertThat in order to see what the actual field differences are
    assertThat(actual).containsExactlyElementsIn(expected);
  }

  // Tests whether a scheduledInterview object was added to datastore with one possible interviewer.
  @Test
  public void onlyReturnsInterviewersWithMatchingCompanyAndJob() throws IOException {
    personDao.create(googleSWE1);
    availabilityDao.create(googleSWE1Avail1);
    availabilityDao.create(googleSWE1Avail2);
    availabilityDao.create(googleSWE1Avail3);
    availabilityDao.create(googleSWE1Avail4);
    personDao.create(googlePM);
    availabilityDao.create(googlePMAvail1);
    availabilityDao.create(googlePMAvail2);
    availabilityDao.create(googlePMAvail3);
    availabilityDao.create(googlePMAvail4);
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(
        scheduledInterviewDao, availabilityDao, personDao, calendarAccess, emailSender, emailUtils);
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    String jsonString =
        "{\"company\":\"Google\",\"job\":\"SWE\",\"utcStartTime\":\"2020-07-20T12:45:00Z\",\"position\":\"SOFTWARE_ENGINEER\"}";
    postRequest.setContent(jsonString.getBytes(StandardCharsets.UTF_8));
    scheduledInterviewServlet.doPost(postRequest, postResponse);
    List<ScheduledInterview> actual =
        scheduledInterviewDao.getScheduledInterviewsInRangeForUser(
            googleSWE1.id(),
            Instant.parse("2020-07-20T12:45:00Z"),
            Instant.parse("2020-07-20T13:45:00Z"));
    ScheduledInterview expected =
        ScheduledInterview.create(
            actual.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
            googleSWE1.id(),
            emailToId("user@company.org"),
            String.valueOf(actual.get(0).id()),
            Job.SOFTWARE_ENGINEER,
            /*shadowId=*/ "");
    Assert.assertEquals(expected, actual.get(0));
  }

  // Tests that the selected interviewer is one of the possible interviewers.
  @Test
  public void picksOneOfThePossibleInterviewers() throws IOException {
    personDao.create(googleSWE1);
    availabilityDao.create(googleSWE1Avail1);
    availabilityDao.create(googleSWE1Avail2);
    availabilityDao.create(googleSWE1Avail3);
    availabilityDao.create(googleSWE1Avail4);
    personDao.create(googleSWE2QualPMInterviewer);
    availabilityDao.create(googleSWE2QualPMInterviewerAvail1);
    availabilityDao.create(googleSWE2QualPMInterviewerAvail2);
    availabilityDao.create(googleSWE2QualPMInterviewerAvail3);
    availabilityDao.create(googleSWE2QualPMInterviewerAvail4);
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(
        scheduledInterviewDao, availabilityDao, personDao, calendarAccess, emailSender, emailUtils);
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    String jsonString =
        "{\"company\":\"Google\",\"job\":\"SWE\",\"utcStartTime\":\"2020-07-20T12:45:00Z\",\"position\":\"SOFTWARE_ENGINEER\"}";
    postRequest.setContent(jsonString.getBytes(StandardCharsets.UTF_8));
    scheduledInterviewServlet.doPost(postRequest, postResponse);
    List<ScheduledInterview> actual =
        scheduledInterviewDao.getScheduledInterviewsInRangeForUser(
            emailToId("user@company.org"),
            Instant.parse("2020-07-20T12:45:00Z"),
            Instant.parse("2020-07-20T13:45:00Z"));
    ScheduledInterview expected1 =
        ScheduledInterview.create(
            actual.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
            googleSWE1.id(),
            emailToId("user@company.org"),
            String.valueOf(actual.get(0).id()),
            Job.SOFTWARE_ENGINEER,
            /*shadowId=*/ "");
    ScheduledInterview expected2 =
        ScheduledInterview.create(
            actual.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
            googleSWE2QualPMInterviewer.id(),
            emailToId("user@company.org"),
            String.valueOf(actual.get(0).id()),
            Job.SOFTWARE_ENGINEER,
            /*shadowId=*/ "");
    boolean actualIsExpectedOneOrTwo =
        actual.get(0).equals(expected1) || actual.get(0).equals(expected2);
    Assert.assertTrue(actualIsExpectedOneOrTwo);
  }

  // Tests that the availabilities for the involved parties are marked as scheduled.
  @Test
  public void availabilitiesAreScheduled() throws IOException {
    personDao.create(googleSWE1);
    availabilityDao.create(googleSWE1Avail1);
    availabilityDao.create(googleSWE1Avail2);
    availabilityDao.create(googleSWE1Avail3);
    availabilityDao.create(googleSWE1Avail4);
    availabilityDao.create(
        Availability.create(
            emailToId("user@company.org"),
            new TimeRange(
                Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:00:00Z")),
            /*id=*/ -1,
            false));

    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(
        scheduledInterviewDao, availabilityDao, personDao, calendarAccess, emailSender, emailUtils);
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    String jsonString =
        "{\"company\":\"Google\",\"job\":\"SWE\",\"utcStartTime\":\"2020-07-20T12:45:00Z\",\"position\":\"SOFTWARE_ENGINEER\"}";
    postRequest.setContent(jsonString.getBytes(StandardCharsets.UTF_8));
    scheduledInterviewServlet.doPost(postRequest, postResponse);
    boolean allAvailabilitiesAreScheduled = true;
    List<Availability> affectedAvailabilityForInterviewer =
        availabilityDao.getInRangeForUser(
            googleSWE1.id(),
            Instant.parse("2020-07-20T12:45:00Z"),
            Instant.parse("2020-07-20T13:45:00Z"));
    List<Availability> affectedAvailabilityForInterviewee =
        availabilityDao.getInRangeForUser(
            emailToId("user@company.org"),
            Instant.parse("2020-07-20T12:45:00Z"),
            Instant.parse("2020-07-20T13:45:00Z"));
    List<Availability> allAffectedAvailability = new ArrayList<Availability>();
    allAffectedAvailability.addAll(affectedAvailabilityForInterviewer);
    allAffectedAvailability.addAll(affectedAvailabilityForInterviewee);
    for (Availability avail : allAffectedAvailability) {
      if (!avail.scheduled()) {
        allAvailabilitiesAreScheduled = false;
      }
    }
    Assert.assertTrue(allAvailabilitiesAreScheduled);
  }

  // Tests errors with Instant parsing.
  @Test
  public void invalidInstant() throws IOException {
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(
        scheduledInterviewDao, availabilityDao, personDao, calendarAccess, emailSender, emailUtils);
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    // No dash between 07 and 20
    String jsonString =
        "{\"company\":\"Google\",\"job\":\"SWE\",\"utcStartTime\":\"2020-0720T12:45:00Z\",\"position\":\"SOFTWARE_ENGINEER\"}";
    postRequest.setContent(jsonString.getBytes(StandardCharsets.UTF_8));
    scheduledInterviewServlet.doPost(postRequest, postResponse);
    Assert.assertEquals(400, postResponse.getStatus());
  }

  // Shadow's first name should be in the ScheduledInterviewRequest.
  @Test
  public void getShadowName() throws IOException {
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(
        scheduledInterviewDao, availabilityDao, personDao, calendarAccess, emailSender, emailUtils);
    helper.setEnvIsLoggedIn(true).setEnvEmail(googleSWE1.email()).setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personDao.create(googleSWE1);
    personDao.create(googleSWE2QualPMInterviewer);
    personDao.create(shadow);
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-05T18:00:00Z"), Instant.parse("2020-07-05T19:00:00Z")),
            googleSWE1.id(),
            googleSWE2QualPMInterviewer.id(),
            "meet_link",
            Job.SOFTWARE_ENGINEER,
            shadow.id()));
    getRequest.addParameter("timeZone", "America/New_York");
    getRequest.addParameter("userTime", "2020-07-05T22:00:00Z");
    scheduledInterviewServlet.doGet(getRequest, getResponse);
    List<ScheduledInterviewRequest> actual =
        (List<ScheduledInterviewRequest>) getRequest.getAttribute("scheduledInterviews");
    ScheduledInterviewRequest expectedInterview =
        new ScheduledInterviewRequest(
            actual.get(0).getId(),
            "Sunday, July 5, 2020 from 2:00 PM to 3:00 PM",
            googleSWE1.firstName(),
            googleSWE2QualPMInterviewer.firstName(),
            "Interviewer",
            true,
            "meet_link",
            "SOFTWARE_ENGINEER",
            shadow.firstName());
    List<ScheduledInterviewRequest> expected = new ArrayList<ScheduledInterviewRequest>();
    expected.add(expectedInterview);
    Assert.assertEquals(expected, actual);
  }

  // No shadow means "None" is in the ScheduledInterviewRequest.
  @Test
  public void noShadow() throws IOException {
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(
        scheduledInterviewDao, availabilityDao, personDao, calendarAccess, emailSender, emailUtils);
    helper.setEnvIsLoggedIn(true).setEnvEmail(googleSWE1.email()).setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personDao.create(googleSWE1);
    personDao.create(googleSWE2QualPMInterviewer);
    personDao.create(shadow);
    scheduledInterviewDao.create(
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-05T18:00:00Z"), Instant.parse("2020-07-05T19:00:00Z")),
            googleSWE1.id(),
            googleSWE2QualPMInterviewer.id(),
            "meet_link",
            Job.SOFTWARE_ENGINEER,
            /*shadowId=*/ ""));
    getRequest.addParameter("timeZone", "America/New_York");
    getRequest.addParameter("userTime", "2020-07-05T22:00:00Z");
    scheduledInterviewServlet.doGet(getRequest, getResponse);
    List<ScheduledInterviewRequest> actual =
        (List<ScheduledInterviewRequest>) getRequest.getAttribute("scheduledInterviews");
    ScheduledInterviewRequest expectedInterview =
        new ScheduledInterviewRequest(
            actual.get(0).getId(),
            "Sunday, July 5, 2020 from 2:00 PM to 3:00 PM",
            googleSWE1.firstName(),
            googleSWE2QualPMInterviewer.firstName(),
            "Interviewer",
            true,
            "meet_link",
            "SOFTWARE_ENGINEER",
            "None");
    List<ScheduledInterviewRequest> expected = new ArrayList<ScheduledInterviewRequest>();
    expected.add(expectedInterview);
    Assert.assertEquals(expected, actual);
  }

  private String emailToId(String email) {
    return String.format("%d", email.hashCode());
  }

  @Test
  public void onlySchedulesWithQualifiedInterviewers() throws IOException {
    personDao.create(googleSWE1);
    availabilityDao.create(googleSWE1Avail1);
    availabilityDao.create(googleSWE1Avail2);
    availabilityDao.create(googleSWE1Avail3);
    availabilityDao.create(googleSWE1Avail4);
    personDao.create(googleSWE2QualPMInterviewer);
    availabilityDao.create(googleSWE2QualPMInterviewerAvail1);
    availabilityDao.create(googleSWE2QualPMInterviewerAvail2);
    availabilityDao.create(googleSWE2QualPMInterviewerAvail3);
    availabilityDao.create(googleSWE2QualPMInterviewerAvail4);
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(
        scheduledInterviewDao, availabilityDao, personDao, calendarAccess, emailSender, emailUtils);
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    String jsonString =
        "{\"company\":\"Google\",\"job\":\"SWE\",\"utcStartTime\":\"2020-07-20T12:45:00Z\",\"position\":\"PRODUCT_MANAGER\"}";
    postRequest.setContent(jsonString.getBytes(StandardCharsets.UTF_8));
    scheduledInterviewServlet.doPost(postRequest, postResponse);
    List<ScheduledInterview> actual =
        scheduledInterviewDao.getScheduledInterviewsInRangeForUser(
            googleSWE2QualPMInterviewer.id(),
            Instant.parse("2020-07-20T12:45:00Z"),
            Instant.parse("2020-07-20T13:45:00Z"));
    ScheduledInterview expected =
        ScheduledInterview.create(
            actual.get(0).id(),
            new TimeRange(
                Instant.parse("2020-07-20T12:45:00Z"), Instant.parse("2020-07-20T13:45:00Z")),
            googleSWE2QualPMInterviewer.id(),
            emailToId("user@company.org"),
            String.valueOf(actual.get(0).id()),
            Job.PRODUCT_MANAGER,
            /*shadowId=*/ "");
    Assert.assertEquals(expected, actual.get(0));
  }

  @Test
  public void shadowIsAddedToAViableInterview() throws IOException {
    Person interviewer =
        Person.create(
            "interviewer",
            "interviewer@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    Person interviewee =
        Person.create(
            "interviewee",
            "interviewee@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    Person shadow =
        Person.create(
            String.format("%d", "shadow@mail.com".hashCode()),
            "shadow@mail.com",
            "firstName",
            "lastName",
            "company",
            "job",
            "linkedIn",
            EnumSet.of(Job.NETWORK_ENGINEER),
            /*okShadow=*/ true);
    ScheduledInterview possibleInterview1 =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-07T16:00:00Z"), Instant.parse("2020-07-07T17:00:00Z")),
            interviewer.id(),
            interviewee.id(),
            "meet_link",
            Job.NETWORK_ENGINEER,
            /*shadowId=*/ "");
    ScheduledInterview possibleInterview2 =
        ScheduledInterview.create(
            /*id=*/ -1,
            new TimeRange(
                Instant.parse("2020-07-07T16:00:00Z"), Instant.parse("2020-07-07T17:00:00Z")),
            interviewer.id(),
            interviewee.id(),
            "meet_link",
            Job.NETWORK_ENGINEER,
            /*shadowId=*/ "");
    personDao.create(interviewer);
    personDao.create(interviewee);
    personDao.create(shadow);
    scheduledInterviewDao.create(possibleInterview1);
    scheduledInterviewDao.create(possibleInterview2);

    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    scheduledInterviewServlet.init(
        scheduledInterviewDao, availabilityDao, personDao, calendarAccess, emailSender, emailUtils);
    helper.setEnvIsLoggedIn(true).setEnvEmail(shadow.email()).setEnvAuthDomain("auth");
    MockHttpServletRequest putRequest = new MockHttpServletRequest();
    MockHttpServletResponse putResponse = new MockHttpServletResponse();
    String jsonString =
        "{\"company\":\"company\",\"job\":\"job\",\"utcStartTime\":\"2020-07-07T16:00:00Z\",\"position\":\"NETWORK_ENGINEER\"}";
    putRequest.setContent(jsonString.getBytes(StandardCharsets.UTF_8));
    scheduledInterviewServlet.doPut(putRequest, putResponse);
    List<ScheduledInterview> possibleInterviews =
        scheduledInterviewDao.getInRange(
            Instant.parse("2020-07-07T16:00:00Z"), Instant.parse("2020-07-07T17:00:00Z"));
    // The XOR operation is used to make sure the shadow is not added to both interviews.
    boolean oneOfTheInterviewsHasTheShadow =
        possibleInterviews.get(0).shadowId().equals(shadow.id())
            ^ possibleInterviews.get(1).shadowId().equals(shadow.id());
    Assert.assertTrue(oneOfTheInterviewsHasTheShadow);
  }
}

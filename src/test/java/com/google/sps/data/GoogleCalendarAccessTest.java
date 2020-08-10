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

import org.junit.Assert;
import org.junit.After;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolution;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import static com.google.common.truth.Truth.assertThat;

/** Tests event definition (but not event creation) in GoogleCalendarAccess. */
@RunWith(JUnit4.class)
public final class GoogleCalendarAccessTest {
  Instant TIME_430PM = Instant.parse("2020-08-07T16:30:10Z");
  Instant TIME_530PM = Instant.parse("2020-08-07T17:30:10Z");

  LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
  private final ScheduledInterview interview1 =
      ScheduledInterview.create(
          -1,
          new TimeRange(TIME_430PM, TIME_530PM),
          "interviewer_id",
          "interviewee_id",
          "meet_link",
          Job.SOFTWARE_ENGINEER,
          "shadow_id");
  PersonDao personDao = new FakePersonDao();
  Person interviewer =
      Person.create(
          "interviewer_id",
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
          "interviewee_id",
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
          "shadow_id",
          "shadow@mail.com",
          "firstName",
          "lastName",
          "company",
          "job",
          "linkedIn",
          EnumSet.of(Job.NETWORK_ENGINEER),
          /*okShadow=*/ true);

  @Before
  public void setUp() {
    helper.setUp();
    personDao.create(interviewer);
    personDao.create(interviewee);
    personDao.create(shadow);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void basic() throws Exception {
    Event observed = GoogleCalendarAccess.makeEvent(interview1, personDao);

    EventDateTime expStart =
        new EventDateTime().setDateTime(new DateTime(interview1.when().start().toString()));
    Assert.assertEquals(observed.getStart(), expStart);

    EventDateTime expEnd =
        new EventDateTime().setDateTime(new DateTime(interview1.when().end().toString()));
    Assert.assertEquals(observed.getEnd(), expEnd);

    CreateConferenceRequest expCreateRequest = new CreateConferenceRequest();
    expCreateRequest.setRequestId(String.valueOf(interview1.id()));
    expCreateRequest.setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"));
    Assert.assertEquals(
        observed.getConferenceData(), new ConferenceData().setCreateRequest(expCreateRequest));
  }

  @Test
  public void getThreeAttendees() throws Exception {
    List<EventAttendee> obs = GoogleCalendarAccess.getAttendees(interview1, personDao);
    List<EventAttendee> exp =
        Arrays.asList(
            new EventAttendee[] {
              new EventAttendee().setEmail("interviewer@mail.com"),
              new EventAttendee().setEmail("interviewee@mail.com"),
              new EventAttendee().setEmail("shadow@mail.com")
            });
    assertThat(obs).containsExactlyElementsIn(exp);
  }

  @Test
  public void getAttendeesNoShadow() throws Exception {
    List<EventAttendee> obs = GoogleCalendarAccess.getAttendees(interview1, new FakePersonDao());
  }
}

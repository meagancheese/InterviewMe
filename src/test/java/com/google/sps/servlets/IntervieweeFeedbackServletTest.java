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
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.sps.data.FakeScheduledInterviewDao;
import com.google.sps.data.FakePersonDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.TimeRange;
import com.google.sps.servlets.IntervieweeFeedbackServlet;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(JUnit4.class)
public final class IntervieweeFeedbackServletTest {
  LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalCapabilitiesServiceTestConfig());
  private FakeScheduledInterviewDao scheduledInterviewDao;
  private FakePersonDao personDao;
  private final ScheduledInterview scheduledInterview =
      ScheduledInterview.create(
          /*id=*/ (long) -1,
          /*when=*/ new TimeRange(
              Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
          /*interviewerId=*/ emailToId("user@company.org"),
          /*intervieweeId=*/ emailToId("user@mail.com"));

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

  // Tests that an error is returned when a scheduledInterview is not found.
  @Test
  public void interviewIdDoesNotExist() throws IOException {
    IntervieweeFeedbackServlet intervieweeFeedbackServlet = new IntervieweeFeedbackServlet();
    intervieweeFeedbackServlet.init(scheduledInterviewDao, personDao);
    scheduledInterviewDao.create(scheduledInterview);
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    postRequest.addParameter("interviewId", "1");
    postRequest.addParameter("questionCount", "11");
    intervieweeFeedbackServlet.doPost(postRequest, postResponse);
    Assert.assertEquals(404, postResponse.getStatus());
  }

  // Tests that users who are not the interviewer may not submit feedback for the interviewee.
  @Test
  public void invalidUser() throws IOException {
    IntervieweeFeedbackServlet intervieweeFeedbackServlet = new IntervieweeFeedbackServlet();
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@mail.com").setEnvAuthDomain("auth");
    intervieweeFeedbackServlet.init(scheduledInterviewDao, personDao);
    scheduledInterviewDao.create(scheduledInterview);
    List<ScheduledInterview> scheduledInterviews =
        scheduledInterviewDao.getForPerson(emailToId("user@mail.com"));
    MockHttpServletRequest postRequest = new MockHttpServletRequest();
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    postRequest.addParameter("interviewId", String.valueOf(scheduledInterviews.get(0).id()));
    postRequest.addParameter("questionCount", "11");
    intervieweeFeedbackServlet.doPost(postRequest, postResponse);
    Assert.assertEquals(401, postResponse.getStatus());
  }

  private String emailToId(String email) {
    return String.format("%d", email.hashCode());
  }
}

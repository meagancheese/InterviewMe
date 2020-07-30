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
import com.google.gson.reflect.TypeToken;
import com.google.sps.data.FakeScheduledInterviewDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.TimeRange;
import com.google.sps.servlets.FeedbackServlet;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.Test;
import com.google.gson.JsonSyntaxException;

@RunWith(JUnit4.class)
public final class FeedbackServletTest {
  LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalCapabilitiesServiceTestConfig());
  private FakeScheduledInterviewDao scheduledInterviewDao;
  private final ScheduledInterview scheduledInterview =
      ScheduledInterview.create(
          (long) -1,
          new TimeRange(
              Instant.parse("2020-07-06T17:00:10Z"), Instant.parse("2020-07-06T18:00:10Z")),
          emailToId("user@company.org"),
          emailToId("user@mail.com"));

  @Before
  public void setUp() {
    helper.setUp();
    scheduledInterviewDao = new FakeScheduledInterviewDao();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Tests that a NullPointerException is thrown when a scheduledInterviewId does not exist.
  @Test(expected = NullPointerException.class)
  public void interviewIdDoesNotExist() throws IOException {
    FeedbackServlet feedbackServlet = new FeedbackServlet();
    ScheduledInterviewServlet scheduledInterviewServlet = new ScheduledInterviewServlet();
    feedbackServlet.init(scheduledInterviewDao);
    scheduledInterviewDao.create(scheduledInterview);
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    getRequest.addParameter("interview", "-1");
    getRequest.addParameter("userTime", "2020-07-05T22:00:00Z");
    getRequest.addParameter("timeZone", "Etc/UCT");
    getRequest.addParameter("role", "Interviewer");
    feedbackServlet.doGet(getRequest, getResponse);
  }

  // Tests that a valid Feedback request was made.
  @Test
  public void validRequest() throws IOException {
    FeedbackServlet feedbackServlet = new FeedbackServlet();
    feedbackServlet.init(scheduledInterviewDao);
    scheduledInterviewDao.create(scheduledInterview);
    List<ScheduledInterview> scheduledInterviews =
        scheduledInterviewDao.getForPerson(emailToId("user@company.org"));
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@company.org").setEnvAuthDomain("auth");
    getRequest.addParameter("interview", String.valueOf(scheduledInterviews.get(0).id()));
    getRequest.addParameter("userTime", "2020-07-05T22:00:00Z");
    getRequest.addParameter("timeZone", "Etc/UCT");
    getRequest.addParameter("role", "Interviewer");
    feedbackServlet.doGet(getRequest, getResponse);

    Assert.assertEquals(200, getResponse.getStatus());
  }

  private String emailToId(String email) {
    return String.format("%d", email.hashCode());
  }
}

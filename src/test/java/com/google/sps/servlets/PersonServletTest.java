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

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.sps.data.FakePersonDao;
import com.google.sps.servlets.PersonServlet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Tests PersonServlet. */
@RunWith(JUnit4.class)
public final class PersonServletTest {
  LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // Two people, get the right one.
  @Test
  public void getOneOutOfTwo() throws IOException, UnsupportedEncodingException {
    String personA =
        "{\"email\": \"a@gmail.com\", \"firstName\": \"a\", \"lastName\": \"a\", \"company\": \"\", \"job\": \"\", \"linkedin\": \"\"}";
    String personB =
        "{\"email\": \"b@gmail.com\", \"firstName\": \"b\", \"lastName\": \"b\", \"company\": \"\", \"job\": \"\", \"linkedin\": \"\"}";

    // a is logged in.
    helper.setEnvIsLoggedIn(true).setEnvEmail("a@gmail.com").setEnvAuthDomain("auth");

    // Post person a.
    MockHttpServletRequest postRequest =
        post("/person").content(personA).buildRequest(new MockServletContext());
    PersonServlet personServlet = new PersonServlet();
    personServlet.init(new FakePersonDao());
    personServlet.doPost(postRequest, new MockHttpServletResponse());

    // b is logged in.
    helper.setEnvIsLoggedIn(true).setEnvEmail("b@gmail.com").setEnvAuthDomain("auth");
    // Post person b.
    postRequest = post("/person").content(personB).buildRequest(new MockServletContext());
    personServlet.doPost(postRequest, new MockHttpServletResponse());

    // b requests b.
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("email", "b@gmail.com");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personServlet.doGet(getRequest, getResponse);

    // b should be the result of the doGet().
    JsonObject person = new JsonParser().parse(getResponse.getContentAsString()).getAsJsonObject();
    assertEquals(person.get("email").getAsString(), "b@gmail.com");
    assertEquals(person.get("firstName").getAsString(), "b");
    assertEquals(person.get("lastName").getAsString(), "b");
  }

  // Get updated info.
  @Test
  public void getUpdatedInfo() throws IOException, UnsupportedEncodingException {
    String personA =
        "{\"email\": \"a@gmail.com\", \"firstName\": \"old\", \"lastName\": \"old\", \"company\": \"\", \"job\": \"\", \"linkedin\": \"\"}";

    // a is logged in.
    helper.setEnvIsLoggedIn(true).setEnvEmail("a@gmail.com").setEnvAuthDomain("auth");

    // Post person a.
    MockHttpServletRequest postRequest =
        post("/person").content(personA).buildRequest(new MockServletContext());
    PersonServlet personServlet = new PersonServlet();
    personServlet.init(new FakePersonDao());
    personServlet.doPost(postRequest, new MockHttpServletResponse());

    // Update person a.
    personA =
        "{\"email\": \"a@gmail.com\", \"firstName\": \"new\", \"lastName\": \"new\", \"company\": \"\", \"job\": \"\", \"linkedin\": \"\"}";
    MockHttpServletRequest putRequest =
        put("/person").content(personA).buildRequest(new MockServletContext());
    personServlet.doPut(putRequest, new MockHttpServletResponse());

    // a requests a.
    MockHttpServletRequest getRequest = new MockHttpServletRequest();
    getRequest.addParameter("email", "a@gmail.com");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personServlet.doGet(getRequest, getResponse);

    // a should have "new" as first and last name.
    JsonObject person = new JsonParser().parse(getResponse.getContentAsString()).getAsJsonObject();
    assertEquals(person.get("firstName").getAsString(), "new");
    assertEquals(person.get("lastName").getAsString(), "new");
  }

  // Someone trying to get someone else's info. 401 error is expected.
  @Test
  public void getRequesteeNotLoggedInUser() throws IOException, UnsupportedEncodingException {
    // a is logged in.
    helper.setEnvIsLoggedIn(true).setEnvEmail("a@gmail.com").setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();

    PersonServlet personServlet = new PersonServlet();
    personServlet.init(new FakePersonDao());

    // But a requests b's info.
    getRequest.addParameter("email", "b@gmail.com");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personServlet.doGet(getRequest, getResponse);

    assertEquals(getResponse.getStatus(), 401);
  }

  // Someone trying to post someone else's info. 401 error is expected.
  @Test
  public void postRequesteeNotLoggedInUser() throws IOException, UnsupportedEncodingException {
    // a is logged in.
    helper.setEnvIsLoggedIn(true).setEnvEmail("a@gmail.com").setEnvAuthDomain("auth");

    PersonServlet personServlet = new PersonServlet();
    personServlet.init(new FakePersonDao());

    // But a tries to post b's info.
    String personB =
        "{\"email\": \"b@gmail.com\", \"firstName\": \"b\", \"lastName\": \"b\", \"company\": \"\", \"job\": \"\", \"linkedin\": \"\"}";
    MockHttpServletRequest postRequest =
        put("/person").content(personB).buildRequest(new MockServletContext());
    MockHttpServletResponse postResponse = new MockHttpServletResponse();
    personServlet.doPost(postRequest, postResponse);

    assertEquals(postResponse.getStatus(), 401);
  }

  // Someone trying to update someone else's info. 401 error is expected.
  @Test
  public void putRequesteeNotLoggedInUser() throws IOException, UnsupportedEncodingException {
    // a is logged in.
    helper.setEnvIsLoggedIn(true).setEnvEmail("a@gmail.com").setEnvAuthDomain("auth");

    PersonServlet personServlet = new PersonServlet();
    personServlet.init(new FakePersonDao());

    // But a tries to update b's info.
    String personB =
        "{\"email\": \"b@gmail.com\", \"firstName\": \"b\", \"lastName\": \"b\", \"company\": \"\", \"job\": \"\", \"linkedin\": \"\"}";
    MockHttpServletRequest putRequest =
        put("/person").content(personB).buildRequest(new MockServletContext());
    MockHttpServletResponse putResponse = new MockHttpServletResponse();
    personServlet.doPut(putRequest, putResponse);

    assertEquals(putResponse.getStatus(), 401);
  }

  // First time user, not registered, not in database yet.
  @Test
  public void notInDatastore() throws IOException, UnsupportedEncodingException {
    // a is logged in.
    helper.setEnvIsLoggedIn(true).setEnvEmail("a@gmail.com").setEnvAuthDomain("auth");
    MockHttpServletRequest getRequest = new MockHttpServletRequest();

    PersonServlet personServlet = new PersonServlet();
    personServlet.init(new FakePersonDao());

    // a requests their info, but they aren't in database.
    getRequest.addParameter("email", "a@gmail.com");
    MockHttpServletResponse getResponse = new MockHttpServletResponse();
    personServlet.doGet(getRequest, getResponse);

    assertEquals(getResponse.getRedirectedUrl(), "/register.html");
  }
}

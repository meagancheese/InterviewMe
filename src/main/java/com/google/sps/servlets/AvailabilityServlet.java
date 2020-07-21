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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.sps.data.Availability;
import com.google.sps.data.AvailabilityDao;
import com.google.sps.data.DatastoreAvailabilityDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.PutAvailabilityRequest;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.TimeRange;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/availability")
public class AvailabilityServlet extends HttpServlet {
  private AvailabilityDao availabilityDao;
  private ScheduledInterviewDao scheduledInterviewDao;

  @Override
  public void init() {
    init(new DatastoreAvailabilityDao(), new DatastoreScheduledInterviewDao());
  }

  public void init(AvailabilityDao availabilityDao, ScheduledInterviewDao scheduledInterviewDao) {
    this.availabilityDao = availabilityDao;
    this.scheduledInterviewDao = scheduledInterviewDao;
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    BufferedReader reader = request.getReader();
    StringBuffer buffer = new StringBuffer();
    String payloadLine = null;

    while ((payloadLine = reader.readLine()) != null) buffer.append(payloadLine);
    String jsonString = buffer.toString();

    PutAvailabilityRequest utcEncodings;
    try {
      utcEncodings = new Gson().fromJson(jsonString, PutAvailabilityRequest.class);
    } catch (Exception JsonSyntaxException) {
      response.sendError(400);
      return;
    }
    if (!utcEncodings.allFieldsPopulated()) {
      response.sendError(400);
      return;
    }
    UserService userService = UserServiceFactory.getUserService();
    String email = userService.getCurrentUser().getEmail();
    Instant minTime = Instant.parse(utcEncodings.getFirstSlot());
    // The last slot for the week starts 15 minutes before the true end of the week.
    Instant maxTime = Instant.parse(utcEncodings.getLastSlot()).plus(15, ChronoUnit.MINUTES);
    availabilityDao.deleteInRangeForUser(email, minTime, maxTime);
    List<ScheduledInterview> scheduledInterviewsForUser =
        scheduledInterviewDao.getScheduledInterviewsInRangeForUser(email, minTime, maxTime);
    for (String markedSlot : utcEncodings.getMarkedSlots()) {
      createAndStoreAvailability(markedSlot, email, scheduledInterviewsForUser);
    }
  }

  private void createAndStoreAvailability(
      String utc, String email, List<ScheduledInterview> scheduledInterviews) {
    TimeRange when =
        new TimeRange(Instant.parse(utc), Instant.parse(utc).plus(15, ChronoUnit.MINUTES));
    boolean scheduled = false;
    for (ScheduledInterview interview : scheduledInterviews) {
      if (interview.when().contains(Instant.parse(utc))) {
        scheduled = true;
      }
    }
    Availability avail = Availability.create(email, when, -1, scheduled);
    availabilityDao.create(avail);
  }
}

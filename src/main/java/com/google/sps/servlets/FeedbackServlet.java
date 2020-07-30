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

import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

@WebServlet("/feedback")
public class FeedbackServlet extends HttpServlet {

  private ScheduledInterviewDao scheduledInterviewDao;

  @Override
  public void init() {
    init(new DatastoreScheduledInterviewDao());
  }

  public void init(ScheduledInterviewDao scheduledInterviewDao) {
    this.scheduledInterviewDao = scheduledInterviewDao;
  }

  // Opens the feedback form if the user's time is five minutes before the start of the interview.
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long scheduledInterviewId = Long.parseLong(request.getParameter("interview"));
    String userTime = request.getParameter("userTime");
    String timeZone = request.getParameter("timeZone");
    String role = request.getParameter("role");
    boolean feedbackOpen = feedbackIsOpen(userTime, timeZone, scheduledInterviewId);
    request.setAttribute("feedbackOpen", feedbackOpen);
    request.setAttribute("role", role);
    RequestDispatcher rd = request.getRequestDispatcher("/feedback.jsp");
    try {
      rd.forward(request, response);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }

  // Returns whether or not a scheduledInterviewId is found and the start time is at least five
  // minutes before the user's current time.
  private boolean feedbackIsOpen(
      String userTimeString, String timeZoneString, Long scheduledInterviewId) {
    ZoneId timeZoneId = ZoneId.of(timeZoneString);
    Instant userTime = Instant.parse(userTimeString);
    TimeRange scheduledInterviewRange =
        scheduledInterviewDao.get(scheduledInterviewId).map(ScheduledInterview::when).orElse(null);
    if (scheduledInterviewRange == null) {
      throw new NullPointerException("This scheduledInterviewId does not exist");
    }
    return scheduledInterviewRange.start().minus(5, ChronoUnit.MINUTES).isBefore(userTime);
  }
}

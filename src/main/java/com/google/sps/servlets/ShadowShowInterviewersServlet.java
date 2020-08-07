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
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.Job;
import com.google.sps.data.Person;
import com.google.sps.data.PersonDao;
import com.google.sps.data.PossibleInterviewer;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.TimeRange;
import java.io.IOException;
import java.io.BufferedReader;
import java.lang.Integer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.util.Optional;

@WebServlet("/shadow-show-interviewers")
public class ShadowShowInterviewersServlet extends HttpServlet {
  private ScheduledInterviewDao scheduledInterviewDao;
  private PersonDao personDao;

  @Override
  public void init() {
    init(new DatastoreScheduledInterviewDao(), new DatastorePersonDao());
  }

  public void init(ScheduledInterviewDao scheduledInterviewDao, PersonDao personDao) {
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.personDao = personDao;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String utcStartTime = request.getParameter("utcStartTime");
    TimeRange interviewTimeRange =
        new TimeRange(
            Instant.parse(utcStartTime), Instant.parse(utcStartTime).plus(1, ChronoUnit.HOURS));
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    String userId = userService.getCurrentUser().getUserId();
    // Since Users returned from the LocalUserService (in tests) do not have userIds, here we set
    // the userId equal to a hashcode.
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }
    String position = request.getParameter("position");
    Job selectedPosition = Job.valueOf(Job.class, position);
    List<ScheduledInterview> possibleInterviews =
        ShadowLoadInterviewsServlet.getPossibleInterviews(
            scheduledInterviewDao, selectedPosition, interviewTimeRange, personDao, userId);
    Set<PossibleInterviewer> possibleInterviewers = new HashSet<PossibleInterviewer>();
    for (ScheduledInterview interview : possibleInterviews) {
      String company = personDao.get(interview.interviewerId()).get().company();
      String job = personDao.get(interview.interviewerId()).get().job();
      possibleInterviewers.add(PossibleInterviewer.create(company, job));
    }
    request.setAttribute("interviewers", possibleInterviewers);
    RequestDispatcher rd = request.getRequestDispatcher("/possibleInterviewers.jsp");
    try {
      rd.forward(request, response);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }
}

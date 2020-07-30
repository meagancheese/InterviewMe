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

@WebServlet("/show-interviewers")
public class ShowInterviewersServlet extends HttpServlet {

  private AvailabilityDao availabilityDao;
  private PersonDao personDao;

  @Override
  public void init() {
    init(new DatastoreAvailabilityDao(), new DatastorePersonDao());
  }

  public void init(AvailabilityDao availabilityDao, PersonDao personDao) {
    this.availabilityDao = availabilityDao;
    this.personDao = personDao;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String utcStartTime = request.getParameter("utcStartTime");
    TimeRange interviewTimeRange =
        new TimeRange(
            Instant.parse(utcStartTime), Instant.parse(utcStartTime).plus(1, ChronoUnit.HOURS));
    List<Availability> availabilitiesInRange =
        availabilityDao.getInRangeForAll(interviewTimeRange.start(), interviewTimeRange.end());
    List<Person> possiblePeople =
        getPossiblePeople(personDao, availabilityDao, availabilitiesInRange, interviewTimeRange);
    Set<PossibleInterviewer> possibleInterviewers = peopleToPossibleInterviewers(possiblePeople);
    request.setAttribute("interviewers", possibleInterviewers);
    RequestDispatcher rd = request.getRequestDispatcher("/possibleInterviewers.jsp");
    try {
      rd.forward(request, response);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }

  static List<Person> getPossiblePeople(
      PersonDao personDao,
      AvailabilityDao availabilityDao,
      List<Availability> allAvailabilities,
      TimeRange range) {
    Set<String> allInterviewers = new HashSet<String>();
    for (Availability avail : allAvailabilities) {
      allInterviewers.add(avail.userId());
    }
    // We don't want to schedule an interview for a user with themself, so we are removing
    // the current user's id from the list.
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    String userId = userService.getCurrentUser().getUserId();
    // Since Users returned from the LocalUserService (in tests) do not have userIds, here we set
    // the userId equal to a hashcode.
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }
    allInterviewers.remove(userId);
    List<Person> possibleInterviewers = new ArrayList<Person>();
    for (String interviewer : allInterviewers) {
      if (personHasPossibleInterviewSlot(availabilityDao, interviewer, range)) {
        possibleInterviewers.add(personDao.get(interviewer).get());
      }
    }
    return possibleInterviewers;
  }

  private Set<PossibleInterviewer> peopleToPossibleInterviewers(List<Person> possiblePeople) {
    Set<PossibleInterviewer> possibleInterviewers = new HashSet<PossibleInterviewer>();
    for (Person person : possiblePeople) {
      possibleInterviewers.add(personToPossibleInterviewer(person));
    }
    return possibleInterviewers;
  }

  static boolean personHasPossibleInterviewSlot(
      AvailabilityDao availabilityDao, String userId, TimeRange range) {
    List<Availability> availabilities =
        availabilityDao.getInRangeForUser(userId, range.start(), range.end());
    availabilities.removeIf(avail -> avail.scheduled());
    // If they have 4 unscheduled availabilities during the hour-long range, then they have
    // a possible interview slot.
    return availabilities.size() == 4;
  }

  private PossibleInterviewer personToPossibleInterviewer(Person person) {
    return PossibleInterviewer.create(person.company(), person.job());
  }
}

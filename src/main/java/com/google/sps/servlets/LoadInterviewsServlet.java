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
import com.google.common.collect.ImmutableList;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.sps.data.Availability;
import com.google.sps.data.AvailabilityDao;
import com.google.sps.data.DatastoreAvailabilityDao;
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.Job;
import com.google.sps.data.PersonDao;
import com.google.sps.data.PossibleInterviewSlot;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.TimeRange;
import com.google.sps.data.TimeUtils;
import java.io.IOException;
import java.io.BufferedReader;
import java.lang.Integer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.util.Optional;

@WebServlet("/load-interviews")
public class LoadInterviewsServlet extends HttpServlet {

  private AvailabilityDao availabilityDao;
  private ScheduledInterviewDao scheduledInterviewDao;
  private PersonDao personDao;
  private Instant currentTime;
  private final int maxTimezoneOffsetMinutes = 720;
  private final int maxTimezoneOffsetHours = 12;

  @Override
  public void init() {
    init(
        new DatastoreAvailabilityDao(),
        new DatastoreScheduledInterviewDao(),
        new DatastorePersonDao(),
        Instant.now());
  }

  public void init(
      AvailabilityDao availabilityDao,
      ScheduledInterviewDao scheduledInterviewDao,
      PersonDao personDao,
      Instant currentTime) {
    this.availabilityDao = availabilityDao;
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.personDao = personDao;
    this.currentTime = currentTime;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int timezoneOffsetMinutes = Integer.parseInt(request.getParameter("timeZoneOffset"));
    Preconditions.checkArgument(
        Math.abs(timezoneOffsetMinutes) <= maxTimezoneOffsetMinutes,
        "Offset greater than %d minutes (%d hours): %d",
        maxTimezoneOffsetMinutes,
        maxTimezoneOffsetHours,
        timezoneOffsetMinutes);
    ZoneOffset timezoneOffset = TimeUtils.convertIntToOffset(timezoneOffsetMinutes);
    ZonedDateTime day = TimeUtils.generateDay(currentTime, timezoneOffsetMinutes);
    ZonedDateTime utcTime = day.withZoneSameInstant(ZoneOffset.UTC);
    // The user will be shown available interview times for the next four weeks, starting from the
    // current time.
    TimeRange interviewSearchTimeRange =
        new TimeRange(utcTime.toInstant(), utcTime.toInstant().plus(27, ChronoUnit.DAYS));
    String position = request.getParameter("position");
    Job selectedPosition = Job.valueOf(Job.class, position);
    List<PossibleInterviewSlot> possibleInterviews =
        getPossibleInterviewSlots(selectedPosition, interviewSearchTimeRange, timezoneOffset);
    List<ArrayList<PossibleInterviewSlot>> possibleInterviewsForMonth =
        orderPossibleInterviewSlotsIntoDays(possibleInterviews);
    request.setAttribute("monthList", possibleInterviewsForMonth);
    RequestDispatcher rd = request.getRequestDispatcher("/possibleInterviewTimes.jsp");
    try {
      rd.forward(request, response);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }

  private List<PossibleInterviewSlot> getPossibleInterviewSlots(
      Job position, TimeRange range, ZoneOffset timezoneOffset) {
    Set<String> interviewers = availabilityDao.getUsersAvailableInRange(range.start(), range.end());
    Set<PossibleInterviewSlot> possibleInterviews = new HashSet<PossibleInterviewSlot>();
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
    interviewers.remove(userId);
    // We need to check that the interviewers are qualified to give an interview for the specified
    // position.
    interviewers.removeIf(
        interviewer -> !personDao.get(interviewer).get().qualifiedJobs().contains(position));
    for (String interviewer : interviewers) {
      possibleInterviews.addAll(
          getPossibleInterviewSlotsForPerson(interviewer, range, timezoneOffset));
    }
    // We need to check that the person looking to schedule is not already scheduled during any of
    // the proposed times.
    List<ScheduledInterview> userScheduledInterviews =
        scheduledInterviewDao.getScheduledInterviewsInRangeForUser(
            userId, range.start(), range.end());
    Set<PossibleInterviewSlot> conflictingInterviews = new HashSet<PossibleInterviewSlot>();
    for (PossibleInterviewSlot slot : possibleInterviews) {
      for (ScheduledInterview userInterview : userScheduledInterviews) {
        if (userInterview.when().contains(Instant.parse(slot.utcEncoding()))) {
          conflictingInterviews.add(slot);
        }
      }
    }
    possibleInterviews.removeAll(conflictingInterviews);
    List<PossibleInterviewSlot> possibleInterviewList =
        new ArrayList<PossibleInterviewSlot>(possibleInterviews);
    sortInterviews(possibleInterviewList);
    return possibleInterviewList;
  }

  private List<PossibleInterviewSlot> getPossibleInterviewSlotsForPerson(
      String userId, TimeRange range, ZoneOffset timezoneOffset) {
    List<Availability> availabilities =
        availabilityDao.getInRangeForUser(userId, range.start(), range.end());
    availabilities.removeIf(avail -> avail.scheduled());
    List<PossibleInterviewSlot> possibleInterviewSlotsForPerson =
        new ArrayList<PossibleInterviewSlot>();
    int numberOfSlotsAfterFirstInAnHour = 3;
    int lastFirstSlotOfAnHour = availabilities.size() - numberOfSlotsAfterFirstInAnHour;
    for (int i = 0; i < lastFirstSlotOfAnHour; i++) {
      if (isAnHourWorthOfSlots(availabilities, i, numberOfSlotsAfterFirstInAnHour)) {
        possibleInterviewSlotsForPerson.add(
            PossibleInterviewSlot.create(
                availabilities.get(i).when().start().toString(),
                TimeUtils.getDate(availabilities.get(i).when().start(), timezoneOffset),
                TimeUtils.getTime(availabilities.get(i).when().start(), timezoneOffset)));
      }
    }
    return possibleInterviewSlotsForPerson;
  }

  // The current indexed slot is the start of an hour of availability if there are 3 other
  // slots right after the current slot.
  private boolean isAnHourWorthOfSlots(
      List<Availability> availabilities, int index, int numberOfSlotsAfterFirstInAnHour) {
    return availabilities
        .get(index)
        .when()
        .start()
        .plus(45, ChronoUnit.MINUTES)
        .equals(availabilities.get(index + numberOfSlotsAfterFirstInAnHour).when().start());
  }

  private void sortInterviews(List<PossibleInterviewSlot> possibleInterviewSlots) {
    possibleInterviewSlots.sort(
        (PossibleInterviewSlot p1, PossibleInterviewSlot p2) -> {
          Instant p1Instant = Instant.parse(p1.utcEncoding());
          Instant p2Instant = Instant.parse(p2.utcEncoding());
          if (p1Instant.equals(p2Instant)) {
            return 0;
          }
          if (p1Instant.isBefore(p2Instant)) {
            return -1;
          }
          return 1;
        });
  }

  static List<ArrayList<PossibleInterviewSlot>> orderPossibleInterviewSlotsIntoDays(
      List<PossibleInterviewSlot> possibleInterviews) {
    String date = possibleInterviews.isEmpty() ? "" : possibleInterviews.get(0).date();
    List<ArrayList<PossibleInterviewSlot>> possibleInterviewsForMonth =
        new ArrayList<ArrayList<PossibleInterviewSlot>>();
    // In order to separate the time slots by day, when the date changes a new dayOfSlots is
    // created and the previous dayOfSlots is added to possibleInterviewsForMonth.
    if (!possibleInterviews.isEmpty()) {
      ArrayList<PossibleInterviewSlot> dayOfSlots = new ArrayList<PossibleInterviewSlot>();
      for (PossibleInterviewSlot possibleInterview : possibleInterviews) {
        if (!possibleInterview.date().equals(date)) {
          possibleInterviewsForMonth.add(dayOfSlots);
          dayOfSlots = new ArrayList<PossibleInterviewSlot>();
          date = possibleInterview.date();
        }
        dayOfSlots.add(possibleInterview);
      }
      possibleInterviewsForMonth.add(dayOfSlots);
    }
    return possibleInterviewsForMonth;
  }
}

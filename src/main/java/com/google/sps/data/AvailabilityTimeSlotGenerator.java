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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.time.format.DateTimeFormatter;

/** A generator of a collection of AvailabilityTimeSlot Objects. */
public class AvailabilityTimeSlotGenerator {
  private static final int EARLIEST_HOUR = 8;
  private static final int LATEST_HOUR = 19;
  // A list of hours and minutes representing permitted time slots.
  private static final ImmutableList<HoursAndMinutes> ALL_HOURS_AND_MINUTES = allHoursAndMinutes();

  @AutoValue
  abstract static class HoursAndMinutes {
    abstract int hour();

    abstract int minute();

    static HoursAndMinutes create(int hour, int minute) {
      return new AutoValue_AvailabilityTimeSlotGenerator_HoursAndMinutes(hour, minute);
    }
  }

  private static ImmutableList<HoursAndMinutes> allHoursAndMinutes() {
    ImmutableList.Builder<HoursAndMinutes> hoursAndMinutes = ImmutableList.builder();
    for (int i = EARLIEST_HOUR; i <= LATEST_HOUR; i++) {
      hoursAndMinutes.add(HoursAndMinutes.create(i, 0));
      hoursAndMinutes.add(HoursAndMinutes.create(i, 15));
      hoursAndMinutes.add(HoursAndMinutes.create(i, 30));
      hoursAndMinutes.add(HoursAndMinutes.create(i, 45));
    }
    return hoursAndMinutes.build();
  }

  /**
   * Constructs a List of lists that represents a week's worth of AvailabilityTimeSlot objects. One
   * list corresponds to one day.
   *
   * @param instant An Instant on the first day of the week for which time slots are generated.
   * @param timezoneOffsetMinutes An int that represents the difference between UTC and the user's
   *     current timezone. Example: A user in EST has a timezoneOffsetMinutes of -240 which means
   *     that EST is 240 minutes behind UTC.
   * @param availabilityDao The AvailabilityDao that is used to get the selected Availabilities for
   *     the week.
   * @throws IllegalArgumentException if the magnitude of timezoneOffsetMinutes is greater than 720.
   */
  public static List<List<AvailabilityTimeSlot>> timeSlotsForWeek(
      Instant instant, int timezoneOffsetMinutes, AvailabilityDao availabilityDao) {
    Preconditions.checkArgument(
        Math.abs(timezoneOffsetMinutes) <= 720,
        "Offset greater than 720 minutes (12 hours): %s",
        timezoneOffsetMinutes);
    List<Instant> startAndEndOfWeek = getStartAndEndOfWeek(instant, timezoneOffsetMinutes);
    UserService userService = UserServiceFactory.getUserService();
    String email = userService.getCurrentUser().getEmail();
    List<Availability> userAvailabilityForWeek =
        availabilityDao.getInRangeForUser(
            email, startAndEndOfWeek.get(0), startAndEndOfWeek.get(1));
    Map<Instant, Availability> availabilityMap = new HashMap<Instant, Availability>();
    for (Availability avail : userAvailabilityForWeek) {
      availabilityMap.put(avail.when().start(), avail);
    }
    ImmutableList.Builder<List<AvailabilityTimeSlot>> weekList = ImmutableList.builder();
    for (int i = 0; i < 7; i++) {
      weekList.add(
          timeSlotsForDay(
              instant.plus(i, ChronoUnit.DAYS), timezoneOffsetMinutes, availabilityMap));
    }
    return weekList.build();
  }

  // Creates longs of milliseconds for the start and end points of the specified week.
  private static List<Instant> getStartAndEndOfWeek(Instant instant, int timezoneOffsetMinutes) {
    ZonedDateTime firstDay = generateDay(instant, timezoneOffsetMinutes);
    Instant startOfWeek = getStartOrEndOfWeek(firstDay, true);
    ZonedDateTime lastDay = firstDay.plus(6, ChronoUnit.DAYS);
    Instant endOfWeek = getStartOrEndOfWeek(lastDay, false);
    List<Instant> startAndEnd = new ArrayList<Instant>();
    startAndEnd.add(startOfWeek);
    startAndEnd.add(endOfWeek);
    return startAndEnd;
  }

  // Returns either the start or end point of the specified week.
  private static Instant getStartOrEndOfWeek(ZonedDateTime day, boolean start) {
    ZoneId zoneId = day.getZone();
    int year = day.getYear();
    int month = day.getMonthValue();
    int dayOfMonth = day.getDayOfMonth();
    int hour = 0;
    int minute = 0;
    if (start) {
      hour = ALL_HOURS_AND_MINUTES.get(0).hour();
      minute = ALL_HOURS_AND_MINUTES.get(0).minute();
    } else {
      hour = ALL_HOURS_AND_MINUTES.get(ALL_HOURS_AND_MINUTES.size() - 1).hour() + 1;
      minute = 0;
    }
    LocalDateTime localTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute);
    ZonedDateTime utcTime = ZonedDateTime.of(localTime, zoneId).withZoneSameInstant(ZoneOffset.UTC);
    return utcTime.toInstant();
  }

  /**
   * Constructs a List of a day's worth of AvailabilityTimeSlot objects.
   *
   * @param instant An Instant on the day for which time slots are generated.
   * @param timezoneOffsetMinutes An int that represents the difference between UTC and the user's
   *     current timezone. Example: A user in EST has a timezoneOffsetMinutes of -240 which means
   *     that EST is 240 minutes behind UTC.
   * @param userAvailabilityForWeek A hashmap of the selected Availabilities for the current user
   *     during the week that the specified day is a part of.
   */
  @VisibleForTesting
  static List<AvailabilityTimeSlot> timeSlotsForDay(
      Instant instant,
      int timezoneOffsetMinutes,
      Map<Instant, Availability> userAvailabilityForWeek) {
    ZonedDateTime day = generateDay(instant, timezoneOffsetMinutes);
    ZoneId zoneId = day.getZone();
    String dayOfWeek = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);
    int year = day.getYear();
    int month = day.getMonthValue();
    int dayOfMonth = day.getDayOfMonth();

    String formattedDate = formatDate(dayOfWeek, month, dayOfMonth);

    ImmutableList.Builder<AvailabilityTimeSlot> timeSlots = ImmutableList.builder();
    for (HoursAndMinutes hoursAndMinutes : ALL_HOURS_AND_MINUTES) {
      int hour = hoursAndMinutes.hour();
      int minute = hoursAndMinutes.minute();
      LocalDateTime localTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute);
      ZonedDateTime utcTime =
          ZonedDateTime.of(localTime, zoneId).withZoneSameInstant(ZoneOffset.UTC);
      String formattedUTCTime = utcTime.format(DateTimeFormatter.ISO_INSTANT);

      int standardHour = hour;
      if (hour > 12) {
        standardHour = hour - 12;
      }
      String formattedTime =
          String.format("%d:%02d %s", standardHour, minute, hour < 12 ? "AM" : "PM");

      timeSlots.add(
          AvailabilityTimeSlot.create(
              formattedUTCTime,
              formattedTime,
              formattedDate,
              getSelectedStatus(formattedUTCTime, userAvailabilityForWeek),
              getScheduledStatus(formattedUTCTime, userAvailabilityForWeek)));
    }

    return timeSlots.build();
  }

  // Uses an Instant and a timezoneOffsetMinutes int to create a ZonedDateTime instance.
  private static ZonedDateTime generateDay(Instant instant, int timezoneOffsetMinutes) {
    return instant.atZone(ZoneId.ofOffset("UTC", convertIntToOffset(timezoneOffsetMinutes)));
  }

  // This method takes the timezoneOffsetMinutes int and converts it
  // into a proper ZoneOffset instance.
  private static ZoneOffset convertIntToOffset(int timezoneOffsetMinutes) {
    return ZoneOffset.ofHoursMinutes((timezoneOffsetMinutes / 60), (timezoneOffsetMinutes % 60));
  }

  // Returns a readable date string such as "Tue 7/7".
  private static String formatDate(String dayOfWeek, int month, int dayOfMonth) {
    return String.format("%s %d/%d", dayOfWeek, month, dayOfMonth);
  }

  // This method tells whether or not a time slot has already been selected (Whether or not
  // it is already in datastore).
  private static boolean getSelectedStatus(
      String utcEncoding, Map<Instant, Availability> userAvailabilityForWeek) {
    Instant startTime = Instant.parse(utcEncoding);
    if (userAvailabilityForWeek.get(startTime) == null) {
      return false;
    }
    return true;
  }

  // This methods tells whether or not a time slot has already been scheduled over.
  private static boolean getScheduledStatus(
      String utcEncoding, Map<Instant, Availability> userAvailabilityForWeek) {
    if (!getSelectedStatus(utcEncoding, userAvailabilityForWeek)) {
      return false;
    }
    Instant startTime = Instant.parse(utcEncoding);
    if (userAvailabilityForWeek.get(startTime).scheduled()) {
      return true;
    }
    return false;
  }
}

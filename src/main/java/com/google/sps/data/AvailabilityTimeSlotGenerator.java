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

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
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
   * @throws IllegalArgumentException if the magnitude of timezoneOffsetMinutes is greater than 720.
   */
  public static List<List<AvailabilityTimeSlot>> timeSlotsForWeek(
      Instant instant, int timezoneOffsetMinutes) {
    Preconditions.checkArgument(
        Math.abs(timezoneOffsetMinutes) <= 720,
        "Offset greater than 720 minutes (12 hours): %s",
        timezoneOffsetMinutes);
    ImmutableList.Builder<List<AvailabilityTimeSlot>> weekList = ImmutableList.builder();
    for (int i = 0; i < 7; i++) {
      weekList.add(timeSlotsForDay(instant.plus(i, ChronoUnit.DAYS), timezoneOffsetMinutes));
    }
    return weekList.build();
  }

  /**
   * Constructs a List of a day's worth of AvailabilityTimeSlot objects.
   *
   * @param instant An Instant on the day for which time slots are generated.
   * @param timezoneOffsetMinutes An int that represents the difference between UTC and the user's
   *     current timezone. Example: A user in EST has a timezoneOffsetMinutes of -240 which means
   *     that EST is 240 minutes behind UTC.
   */
  @VisibleForTesting
  static List<AvailabilityTimeSlot> timeSlotsForDay(Instant instant, int timezoneOffsetMinutes) {
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
              formattedUTCTime, formattedTime, formattedDate, getSelectedStatus(formattedUTCTime)));
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

  // TODO: Access the time slot from data store using the utcEnconding to tell if it
  // is selected or not.
  // This method will tell whether or not a time slot has already been selected. (See
  // TODO above).
  private static boolean getSelectedStatus(String utcEncoding) {
    return false;
  }
}

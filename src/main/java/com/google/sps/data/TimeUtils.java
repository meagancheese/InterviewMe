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

import java.time.Instant;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;

/** A collection of methods that are useful when dealing with time. */
public class TimeUtils {
  /** Uses an Instant and a timezoneOffsetMinutes int to create a ZonedDateTime instance. */
  public static ZonedDateTime generateDay(Instant instant, int timezoneOffsetMinutes) {
    return instant.atZone(ZoneId.ofOffset("UTC", convertIntToOffset(timezoneOffsetMinutes)));
  }

  /** Converts the timezoneOffsetMinutes int into a proper ZoneOffset instance. */
  public static ZoneOffset convertIntToOffset(int timezoneOffsetMinutes) {
    return ZoneOffset.ofHoursMinutes((timezoneOffsetMinutes / 60), (timezoneOffsetMinutes % 60));
  }

  /** Returns a formatted date String in the specified timezone of the Instant. Ex: Wed 8/12 */
  public static String getDate(Instant instant, ZoneOffset timezoneOffset) {
    ZonedDateTime day = instant.atZone(ZoneId.ofOffset("UTC", timezoneOffset));
    String dayOfWeek = day.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
    int month = day.getMonthValue();
    int dayOfMonth = day.getDayOfMonth();
    return String.format("%s %d/%d", dayOfWeek, month, dayOfMonth);
  }

  /**
   * Returns a formatted time String of an hour in the specified timezone starting at the Instant.
   * Ex: 4:00 PM - 5:00 PM
   */
  public static String getTime(Instant instant, ZoneOffset timezoneOffset) {
    ZonedDateTime startTime = instant.atZone(ZoneId.ofOffset("UTC", timezoneOffset));
    ZonedDateTime endTime = startTime.plus(1, ChronoUnit.HOURS);
    return String.format("%s - %s", formatTime(startTime), formatTime(endTime));
  }

  private static String formatTime(ZonedDateTime time) {
    int hour = time.getHour();
    int minute = time.getMinute();
    int standardHour = hour;
    if (hour > 12) {
      standardHour = hour - 12;
    }
    return String.format("%d:%02d %s", standardHour, minute, hour < 12 ? "AM" : "PM");
  }
}

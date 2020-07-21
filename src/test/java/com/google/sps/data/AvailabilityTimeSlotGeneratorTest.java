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

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class AvailabilityTimeSlotGeneratorTest {

  LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void createADayOfUnselectedTimeSlots() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 10, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-4)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = -240;
    Map<Instant, Availability> userAvailabilityForWeek = new HashMap<Instant, Availability>();
    List<AvailabilityTimeSlot> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForDay(
            instant, timezoneOffsetMinutes, userAvailabilityForWeek);

    List<AvailabilityTimeSlot> expected = new ArrayList<AvailabilityTimeSlot>();

    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:00:00Z", "8:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:15:00Z", "8:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:30:00Z", "8:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:45:00Z", "8:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:00:00Z", "9:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:15:00Z", "9:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:30:00Z", "9:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:45:00Z", "9:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:00:00Z", "10:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:15:00Z", "10:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:30:00Z", "10:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:45:00Z", "10:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:00:00Z", "11:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:15:00Z", "11:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:30:00Z", "11:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:45:00Z", "11:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:00:00Z", "12:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:15:00Z", "12:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:30:00Z", "12:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:45:00Z", "12:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:00:00Z", "1:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:15:00Z", "1:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:30:00Z", "1:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:45:00Z", "1:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:00:00Z", "2:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:15:00Z", "2:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:30:00Z", "2:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:45:00Z", "2:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:00:00Z", "3:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:15:00Z", "3:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:30:00Z", "3:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:45:00Z", "3:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:00:00Z", "4:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:15:00Z", "4:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:30:00Z", "4:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:45:00Z", "4:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:00:00Z", "5:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:15:00Z", "5:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:30:00Z", "5:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:45:00Z", "5:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:00:00Z", "6:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:15:00Z", "6:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:30:00Z", "6:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:45:00Z", "6:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:00:00Z", "7:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:15:00Z", "7:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:30:00Z", "7:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:45:00Z", "7:45 PM", "Tue 7/7", false, false));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void createADayOfSelectedTimeSlots() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 10, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-4)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = -240;
    Map<Instant, Availability> userAvailabilityForWeek = new HashMap<Instant, Availability>();
    Availability availabilityOne =
        Availability.create(
            "user@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:45:00Z"), Instant.parse("2020-07-07T13:00:00Z")),
            -1,
            false);
    Availability availabilityTwo =
        Availability.create(
            "user@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T17:00:00Z"), Instant.parse("2020-07-07T17:15:00Z")),
            -1,
            false);
    userAvailabilityForWeek.put(availabilityOne.when().start(), availabilityOne);
    userAvailabilityForWeek.put(availabilityTwo.when().start(), availabilityTwo);

    List<AvailabilityTimeSlot> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForDay(
            instant, timezoneOffsetMinutes, userAvailabilityForWeek);

    List<AvailabilityTimeSlot> expected = new ArrayList<AvailabilityTimeSlot>();

    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:00:00Z", "8:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:15:00Z", "8:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:30:00Z", "8:30 AM", "Tue 7/7", false, false));
    // Selected Time Slot
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:45:00Z", "8:45 AM", "Tue 7/7", true, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:00:00Z", "9:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:15:00Z", "9:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:30:00Z", "9:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:45:00Z", "9:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:00:00Z", "10:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:15:00Z", "10:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:30:00Z", "10:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:45:00Z", "10:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:00:00Z", "11:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:15:00Z", "11:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:30:00Z", "11:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:45:00Z", "11:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:00:00Z", "12:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:15:00Z", "12:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:30:00Z", "12:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:45:00Z", "12:45 PM", "Tue 7/7", false, false));
    // Selected Time Slot
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:00:00Z", "1:00 PM", "Tue 7/7", true, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:15:00Z", "1:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:30:00Z", "1:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:45:00Z", "1:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:00:00Z", "2:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:15:00Z", "2:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:30:00Z", "2:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:45:00Z", "2:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:00:00Z", "3:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:15:00Z", "3:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:30:00Z", "3:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:45:00Z", "3:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:00:00Z", "4:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:15:00Z", "4:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:30:00Z", "4:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:45:00Z", "4:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:00:00Z", "5:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:15:00Z", "5:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:30:00Z", "5:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:45:00Z", "5:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:00:00Z", "6:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:15:00Z", "6:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:30:00Z", "6:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:45:00Z", "6:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:00:00Z", "7:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:15:00Z", "7:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:30:00Z", "7:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:45:00Z", "7:45 PM", "Tue 7/7", false, false));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void createADayOfScheduledTimeSlots() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 10, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-4)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = -240;
    Map<Instant, Availability> userAvailabilityForWeek = new HashMap<Instant, Availability>();
    Availability availabilityOne =
        Availability.create(
            "user@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T16:15:00Z"), Instant.parse("2020-07-07T16:30:00Z")),
            -1,
            true);
    Availability availabilityTwo =
        Availability.create(
            "user@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T22:45:00Z"), Instant.parse("2020-07-07T23:00:00Z")),
            -1,
            true);
    userAvailabilityForWeek.put(availabilityOne.when().start(), availabilityOne);
    userAvailabilityForWeek.put(availabilityTwo.when().start(), availabilityTwo);
    List<AvailabilityTimeSlot> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForDay(
            instant, timezoneOffsetMinutes, userAvailabilityForWeek);

    List<AvailabilityTimeSlot> expected = new ArrayList<AvailabilityTimeSlot>();

    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:00:00Z", "8:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:15:00Z", "8:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:30:00Z", "8:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:45:00Z", "8:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:00:00Z", "9:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:15:00Z", "9:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:30:00Z", "9:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:45:00Z", "9:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:00:00Z", "10:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:15:00Z", "10:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:30:00Z", "10:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:45:00Z", "10:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:00:00Z", "11:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:15:00Z", "11:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:30:00Z", "11:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:45:00Z", "11:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:00:00Z", "12:00 PM", "Tue 7/7", false, false));
    // Scheduled Time Slot
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:15:00Z", "12:15 PM", "Tue 7/7", true, true));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:30:00Z", "12:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:45:00Z", "12:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:00:00Z", "1:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:15:00Z", "1:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:30:00Z", "1:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:45:00Z", "1:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:00:00Z", "2:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:15:00Z", "2:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:30:00Z", "2:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:45:00Z", "2:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:00:00Z", "3:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:15:00Z", "3:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:30:00Z", "3:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:45:00Z", "3:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:00:00Z", "4:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:15:00Z", "4:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:30:00Z", "4:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:45:00Z", "4:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:00:00Z", "5:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:15:00Z", "5:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:30:00Z", "5:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:45:00Z", "5:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:00:00Z", "6:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:15:00Z", "6:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:30:00Z", "6:30 PM", "Tue 7/7", false, false));
    // Scheduled Time Slot
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:45:00Z", "6:45 PM", "Tue 7/7", true, true));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:00:00Z", "7:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:15:00Z", "7:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:30:00Z", "7:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:45:00Z", "7:45 PM", "Tue 7/7", false, false));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void createADayOfSelectedAndScheduledTimeSlots() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 10, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-4)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = -240;
    Map<Instant, Availability> userAvailabilityForWeek = new HashMap<Instant, Availability>();
    Availability availabilityOne =
        Availability.create(
            "user@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T15:15:00Z"), Instant.parse("2020-07-07T15:30:00Z")),
            -1,
            true);
    Availability availabilityTwo =
        Availability.create(
            "user@mail.com",
            new TimeRange(
                Instant.parse("2020-07-07T19:00:00Z"), Instant.parse("2020-07-07T19:15:00Z")),
            -1,
            false);
    userAvailabilityForWeek.put(availabilityOne.when().start(), availabilityOne);
    userAvailabilityForWeek.put(availabilityTwo.when().start(), availabilityTwo);
    List<AvailabilityTimeSlot> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForDay(
            instant, timezoneOffsetMinutes, userAvailabilityForWeek);

    List<AvailabilityTimeSlot> expected = new ArrayList<AvailabilityTimeSlot>();

    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:00:00Z", "8:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:15:00Z", "8:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:30:00Z", "8:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:45:00Z", "8:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:00:00Z", "9:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:15:00Z", "9:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:30:00Z", "9:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:45:00Z", "9:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:00:00Z", "10:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:15:00Z", "10:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:30:00Z", "10:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:45:00Z", "10:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:00:00Z", "11:00 AM", "Tue 7/7", false, false));
    // Scheduled Time Slot
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:15:00Z", "11:15 AM", "Tue 7/7", true, true));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:30:00Z", "11:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:45:00Z", "11:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:00:00Z", "12:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:15:00Z", "12:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:30:00Z", "12:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:45:00Z", "12:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:00:00Z", "1:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:15:00Z", "1:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:30:00Z", "1:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:45:00Z", "1:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:00:00Z", "2:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:15:00Z", "2:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:30:00Z", "2:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:45:00Z", "2:45 PM", "Tue 7/7", false, false));
    // Selected Time Slot
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:00:00Z", "3:00 PM", "Tue 7/7", true, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:15:00Z", "3:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:30:00Z", "3:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:45:00Z", "3:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:00:00Z", "4:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:15:00Z", "4:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:30:00Z", "4:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:45:00Z", "4:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:00:00Z", "5:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:15:00Z", "5:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:30:00Z", "5:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:45:00Z", "5:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:00:00Z", "6:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:15:00Z", "6:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:30:00Z", "6:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:45:00Z", "6:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:00:00Z", "7:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:15:00Z", "7:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:30:00Z", "7:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:45:00Z", "7:45 PM", "Tue 7/7", false, false));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void zeroOffset() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 10, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(0)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = 0;
    Map<Instant, Availability> userAvailabilityForWeek = new HashMap<Instant, Availability>();
    List<AvailabilityTimeSlot> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForDay(
            instant, timezoneOffsetMinutes, userAvailabilityForWeek);

    List<AvailabilityTimeSlot> expected = new ArrayList<AvailabilityTimeSlot>();

    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T08:00:00Z", "8:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T08:15:00Z", "8:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T08:30:00Z", "8:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T08:45:00Z", "8:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T09:00:00Z", "9:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T09:15:00Z", "9:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T09:30:00Z", "9:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T09:45:00Z", "9:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T10:00:00Z", "10:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T10:15:00Z", "10:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T10:30:00Z", "10:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T10:45:00Z", "10:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T11:00:00Z", "11:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T11:15:00Z", "11:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T11:30:00Z", "11:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T11:45:00Z", "11:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:00:00Z", "12:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:15:00Z", "12:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:30:00Z", "12:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:45:00Z", "12:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:00:00Z", "1:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:15:00Z", "1:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:30:00Z", "1:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:45:00Z", "1:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:00:00Z", "2:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:15:00Z", "2:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:30:00Z", "2:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:45:00Z", "2:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:00:00Z", "3:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:15:00Z", "3:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:30:00Z", "3:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:45:00Z", "3:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:00:00Z", "4:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:15:00Z", "4:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:30:00Z", "4:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:45:00Z", "4:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:00:00Z", "5:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:15:00Z", "5:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:30:00Z", "5:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:45:00Z", "5:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:00:00Z", "6:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:15:00Z", "6:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:30:00Z", "6:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:45:00Z", "6:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:00:00Z", "7:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:15:00Z", "7:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:30:00Z", "7:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:45:00Z", "7:45 PM", "Tue 7/7", false, false));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void midnightReturnsNextDay() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 0, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-4)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = -240;
    Map<Instant, Availability> userAvailabilityForWeek = new HashMap<Instant, Availability>();
    List<AvailabilityTimeSlot> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForDay(
            instant, timezoneOffsetMinutes, userAvailabilityForWeek);

    List<AvailabilityTimeSlot> expected = new ArrayList<AvailabilityTimeSlot>();

    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:00:00Z", "8:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:15:00Z", "8:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:30:00Z", "8:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T12:45:00Z", "8:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:00:00Z", "9:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:15:00Z", "9:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:30:00Z", "9:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T13:45:00Z", "9:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:00:00Z", "10:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:15:00Z", "10:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:30:00Z", "10:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T14:45:00Z", "10:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:00:00Z", "11:00 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:15:00Z", "11:15 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:30:00Z", "11:30 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T15:45:00Z", "11:45 AM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:00:00Z", "12:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:15:00Z", "12:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:30:00Z", "12:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T16:45:00Z", "12:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:00:00Z", "1:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:15:00Z", "1:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:30:00Z", "1:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T17:45:00Z", "1:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:00:00Z", "2:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:15:00Z", "2:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:30:00Z", "2:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T18:45:00Z", "2:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:00:00Z", "3:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:15:00Z", "3:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:30:00Z", "3:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T19:45:00Z", "3:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:00:00Z", "4:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:15:00Z", "4:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:30:00Z", "4:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T20:45:00Z", "4:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:00:00Z", "5:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:15:00Z", "5:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:30:00Z", "5:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T21:45:00Z", "5:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:00:00Z", "6:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:15:00Z", "6:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:30:00Z", "6:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T22:45:00Z", "6:45 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:00:00Z", "7:00 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:15:00Z", "7:15 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:30:00Z", "7:30 PM", "Tue 7/7", false, false));
    expected.add(
        AvailabilityTimeSlot.create("2020-07-07T23:45:00Z", "7:45 PM", "Tue 7/7", false, false));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void tooLargePositiveOffset() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          AvailabilityTimeSlotGenerator.timeSlotsForWeek(
              Instant.now(), 740, new FakeAvailabilityDao());
        });
  }

  @Test
  public void tooLargeNegativeOffset() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          AvailabilityTimeSlotGenerator.timeSlotsForWeek(
              Instant.now(), -740, new FakeAvailabilityDao());
        });
  }

  @Test
  public void createAWeekOfUnselectedTimeSlots() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 10, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-4)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = -240;
    // This is necessary for accessing the current user's Availability for the week.
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@gmail.com").setEnvAuthDomain("auth");
    List<List<AvailabilityTimeSlot>> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForWeek(
            instant, timezoneOffsetMinutes, new FakeAvailabilityDao());

    List<AvailabilityTimeSlot> actualFirstSlotsOfEachDay = new ArrayList<AvailabilityTimeSlot>();
    for (int i = 0; i < 7; i++) {
      actualFirstSlotsOfEachDay.add(actual.get(i).get(0));
    }

    List<AvailabilityTimeSlot> expectedFirstSlotsOfEachDay = new ArrayList<AvailabilityTimeSlot>();
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-07T12:00:00Z", "8:00 AM", "Tue 7/7", false, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-08T12:00:00Z", "8:00 AM", "Wed 7/8", false, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-09T12:00:00Z", "8:00 AM", "Thu 7/9", false, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-10T12:00:00Z", "8:00 AM", "Fri 7/10", false, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-11T12:00:00Z", "8:00 AM", "Sat 7/11", false, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-12T12:00:00Z", "8:00 AM", "Sun 7/12", false, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-13T12:00:00Z", "8:00 AM", "Mon 7/13", false, false));

    Assert.assertEquals(expectedFirstSlotsOfEachDay, actualFirstSlotsOfEachDay);
  }

  @Test
  public void createAWeekOfSelectedAndScheduledTimeSlots() {
    ZonedDateTime day =
        ZonedDateTime.of(2020, 7, 7, 10, 0, 0, 0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(-4)));
    Instant instant = day.toInstant();
    int timezoneOffsetMinutes = -240;
    // This is necessary for accessing the current user's Availability for the week.
    helper.setEnvIsLoggedIn(true).setEnvEmail("user@gmail.com").setEnvAuthDomain("auth");
    FakeAvailabilityDao dao = new FakeAvailabilityDao();
    dao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-07T12:00:00Z"), Instant.parse("2020-07-07T12:15:00Z")),
            -1,
            true));
    dao.create(
        Availability.create(
            "user@gmail.com",
            new TimeRange(
                Instant.parse("2020-07-09T12:00:00Z"), Instant.parse("2020-07-09T12:15:00Z")),
            -1,
            false));
    List<List<AvailabilityTimeSlot>> actual =
        AvailabilityTimeSlotGenerator.timeSlotsForWeek(instant, timezoneOffsetMinutes, dao);

    List<AvailabilityTimeSlot> actualFirstSlotsOfEachDay = new ArrayList<AvailabilityTimeSlot>();
    for (int i = 0; i < 7; i++) {
      actualFirstSlotsOfEachDay.add(actual.get(i).get(0));
    }

    List<AvailabilityTimeSlot> expectedFirstSlotsOfEachDay = new ArrayList<AvailabilityTimeSlot>();
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-07T12:00:00Z", "8:00 AM", "Tue 7/7", true, true));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-08T12:00:00Z", "8:00 AM", "Wed 7/8", false, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-09T12:00:00Z", "8:00 AM", "Thu 7/9", true, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-10T12:00:00Z", "8:00 AM", "Fri 7/10", false, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-11T12:00:00Z", "8:00 AM", "Sat 7/11", false, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-12T12:00:00Z", "8:00 AM", "Sun 7/12", false, false));
    expectedFirstSlotsOfEachDay.add(
        AvailabilityTimeSlot.create("2020-07-13T12:00:00Z", "8:00 AM", "Mon 7/13", false, false));

    Assert.assertEquals(expectedFirstSlotsOfEachDay, actualFirstSlotsOfEachDay);
  }
}

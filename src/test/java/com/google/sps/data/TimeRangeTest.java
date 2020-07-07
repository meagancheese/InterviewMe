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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class TimeRangeTest {
  Instant TIME_430PM = Instant.parse("2020-07-06T17:00:10.324978Z");
  Instant TIME_5PM = Instant.parse("2020-07-06T17:00:10.324978Z");
  Instant TIME_530PM = Instant.parse("2020-07-06T17:30:10.324978Z");
  Instant TIME_6PM = Instant.parse("2020-07-06T18:00:10.324978Z");
  Instant TIME_630PM = Instant.parse("2020-07-06T18:30:10.324978Z");
  Instant TIME_645PM = Instant.parse("2020-07-06T18:45:10.324978Z");
  Instant TIME_7PM = Instant.parse("2020-07-06T19:00:10.324978Z");
  Instant TIME_730PM = Instant.parse("2020-07-06T19:30:10.324978Z");
  Instant TIME_745PM = Instant.parse("2020-07-06T19:45:10.324978Z");
  Instant TIME_8PM = Instant.parse("2020-07-06T20:00:10.324978Z");
  Instant TIME_830PM = Instant.parse("2020-07-06T20:30:10.324978Z");
  Instant TIME_9PM = Instant.parse("2020-07-06T21:00:10.324978Z");
  Instant TIME_930PM = Instant.parse("2020-07-06T21:30:10.324978Z");

  // Tests whether or not two TimeRanges are the same
  @Test
  public void equality() {
    Assert.assertEquals(
        TimeRange.fromStartEnd(TIME_530PM, TIME_6PM), TimeRange.fromStartEnd(TIME_530PM, TIME_6PM));

    Assert.assertNotEquals(
        TimeRange.fromStartEnd(TIME_530PM, TIME_6PM),
        TimeRange.fromStartEnd(TIME_530PM, TIME_630PM));
  }

  // Tests whether or not a timerange contains a certain instant.
  @Test
  public void containsPoint() {
    TimeRange range = TimeRange.fromStartEnd(TIME_530PM, TIME_630PM);
    Assert.assertFalse(range.contains(TIME_7PM));
    Assert.assertTrue(range.contains(TIME_6PM));
  }

  // Tests that a TimeRange does not contain another TimeRange
  @Test
  public void contains_nonContaining() {
    TimeRange range = TimeRange.fromStartEnd(TIME_630PM, TIME_8PM);

    // |---|   |--range--|
    Assert.assertFalse(range.contains(TimeRange.fromStartEnd(TIME_430PM, TIME_5PM)));

    //     |--range--|
    // |---|
    Assert.assertFalse(range.contains(TimeRange.fromStartEnd(TIME_6PM, TIME_630PM)));

    //   |--range--|
    // |---|
    Assert.assertFalse(range.contains(TimeRange.fromStartEnd(TIME_6PM, TIME_645PM)));

    // |--range--|
    //         |---|
    Assert.assertFalse(range.contains(TimeRange.fromStartEnd(TIME_745PM, TIME_830PM)));

    // |--range--|
    //           |---|
    Assert.assertFalse(range.contains(TimeRange.fromStartEnd(TIME_8PM, TIME_830PM)));

    // |--range--| |---|
    Assert.assertFalse(range.contains(TimeRange.fromStartEnd(TIME_9PM, TIME_930PM)));
  }

  // Tests that a TimeRange contains another TimeRange
  @Test
  public void contains_Containing() {
    TimeRange range = TimeRange.fromStartEnd(TIME_630PM, TIME_8PM);

    // |--range--|
    // |---|
    Assert.assertTrue(range.contains(TimeRange.fromStartEnd(TIME_630PM, TIME_7PM)));

    // |--range--|
    //    |---|
    Assert.assertTrue(range.contains(TimeRange.fromStartEnd(TIME_7PM, TIME_730PM)));

    // |--range--|
    //       |---|
    Assert.assertTrue(range.contains(TimeRange.fromStartEnd(TIME_730PM, TIME_8PM)));
  }

  // Tests that a TimeRange does not overlap another TimeRange
  @Test
  public void overlaps_nonOverlapping() {
    TimeRange range = TimeRange.fromStartEnd(TIME_6PM, TIME_730PM);

    // |---|   |--range--|
    Assert.assertFalse(range.overlaps(TimeRange.fromStartEnd(TIME_5PM, TIME_530PM)));

    //     |--range--|
    // |---|
    Assert.assertFalse(range.overlaps(TimeRange.fromStartEnd(TIME_530PM, TIME_6PM)));

    // |--range--|
    //           |---|
    Assert.assertFalse(range.overlaps(TimeRange.fromStartEnd(TIME_730PM, TIME_8PM)));

    // |--range--| |---|
    Assert.assertFalse(range.overlaps(TimeRange.fromStartEnd(TIME_830PM, TIME_9PM)));
  }

  // Tests that a TimeRange overlaps the start of another TimeRange
  @Test
  public void overlaps_start() {
    TimeRange range = TimeRange.fromStartEnd(TIME_6PM, TIME_730PM);

    //   |--range--|
    // |---|
    Assert.assertTrue(range.overlaps(TimeRange.fromStartEnd(TIME_5PM, TIME_645PM)));
  }

  // Tests that a TimeRange does overlaps the end another TimeRange
  @Test
  public void overlaps_end() {
    TimeRange range = TimeRange.fromStartEnd(TIME_6PM, TIME_730PM);

    // |--range--|
    //         |---|
    Assert.assertTrue(range.overlaps(TimeRange.fromStartEnd(TIME_7PM, TIME_8PM)));
  }

  // Tests that a TimeRange completely overlaps another TimeRange
  @Test
  public void overlaps_startAndEnd() {
    TimeRange range = TimeRange.fromStartEnd(TIME_6PM, TIME_730PM);

    // |--range--|
    // |---|
    Assert.assertTrue(range.overlaps(TimeRange.fromStartEnd(TIME_6PM, TIME_645PM)));

    // |--range--|
    //    |---|
    Assert.assertTrue(range.overlaps(TimeRange.fromStartEnd(TIME_630PM, TIME_7PM)));

    // |--range--|
    //       |---|
    Assert.assertTrue(range.overlaps(TimeRange.fromStartEnd(TIME_7PM, TIME_730PM)));
  }

  // Tests whether or not a TimeRange's duration is zero
  @Test
  public void rangeDurationIsZero() {
    TimeRange range = TimeRange.fromStartEnd(TIME_430PM, TIME_430PM);
    Assert.assertFalse(range.contains(range));
  }

  @Test
  public void rangeContainsSelf() {
    TimeRange range = TimeRange.fromStartEnd(TIME_7PM, TIME_730PM);
    Assert.assertTrue(range.contains(range));
  }
}

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
import java.time.Duration;
import java.util.Comparator;

/**
 * Class representing a span of time, enforcing properties (e.g. start comes before end) and
 * providing methods to make ranges easier to work with (e.g. {@code overlaps}).
 */
public final class TimeRange {

  /** A comparator for sorting ranges by their start time in ascending order. */
  public static final Comparator<TimeRange> ORDER_BY_START =
      new Comparator<TimeRange>() {
        @Override
        public int compare(TimeRange a, TimeRange b) {
          return a.start.compareTo(b.start);
        }
      };

  /** A comparator for sorting ranges by their end time in ascending order. */
  public static final Comparator<TimeRange> ORDER_BY_END =
      new Comparator<TimeRange>() {
        @Override
        public int compare(TimeRange a, TimeRange b) {
          return a.end().compareTo(b.end());
        }
      };

  private final Instant start, end;

  /** Creates a timerange with a start and end instant */
  public TimeRange(Instant start, Instant end) {
    this.start = start;
    this.end = end;
  }

  /** Returns the start of the range as an instant. */
  public Instant start() {
    return this.start;
  }

  /** Returns the end of the range as an instant. */
  public Instant end() {
    return this.end;
  }

  /** Returns the duration of the range. */
  public Duration duration() {
    return Duration.between(start, end);
  }

  /**
   * Checks if two ranges overlap. This means that at least some part of one range falls within the
   * bounds of another range.
   */
  public boolean overlaps(TimeRange other) {
    // For two ranges to overlap, one range must contain the start of another range.
    //
    // Case 1: |---| |---|
    //
    // Case 2: |---|
    //            |---|
    //
    // Case 3: |---------|
    //            |---|
    if (this.start.isBefore(other.start)) {
      return this.contains(other.start);
    }

    if (this.start.equals(other.start)) {
      return this.end.isAfter(other.end);
    }
    return other.contains(this.start);
  }

  /**
   * Checks if this range completely contains another range. This means that {@code other} is a
   * subset of this range. This is an inclusive bounds, meaning that if two ranges are the same,
   * they contain each other.
   */
  public boolean contains(TimeRange other) {
    /** If this range has no duration, it is irrelevant. */
    if (Duration.between(start, end).isZero()) {
      return false;
    }

    /**
     * If the ends of the range are equal, check whether the start of range comes after the other or
     * are equal.
     */
    if (other.end == this.end) {
      return other.start.isAfter(this.start) || other.start == this.start;
    }

    /**
     * If the start of the ranges are the same, check if one end is before the other or are equal.
     */
    if (other.start == this.start) {
      return other.end.isBefore(this.end) || other.end == this.end;
    }

    // We need the inclusive end for this check in order for this case to equal true:
    // |------|
    //     |--|
    Instant otherInclusiveEnd = other.end;
    return contains(this, other.start) && contains(this, otherInclusiveEnd);
  }

  /** Checks if a timerange contains a certain instant. */
  public boolean contains(Instant point) {
    return contains(this, point);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof TimeRange && equals(this, (TimeRange) other);
  }

  private static boolean contains(TimeRange range, Instant instant) {
    /** If a range has no duration, it cannot contain anything. */
    if (Duration.between(range.start, range.end).isZero()) {
      return false;
    }

    /** If the point comes before the start of the range, the range will not contain it. */
    if (instant.isBefore(range.start) || instant.isAfter(range.end)) {
      return false;
    }

    /** If the point is equal to the end of the range, the range will not contain it. */
    if (instant == range.end) {
      return false;
    }

    /**
     * If the point is on the end of the range. We don't count it as included in the range. For
     * example, if we have a range that starts at 8:00 and is 30 minutes long, it would end at 8:30.
     * But that range should not contain 8:30 because it would end just before 8:30 began.
     */
    return instant.isAfter(range.start);
  }

  /** Checks if two timeranges are the same. */
  private static boolean equals(TimeRange a, TimeRange b) {
    return a.start == b.start && a.end == b.end;
  }

  public static TimeRange fromStartEnd(Instant start, Instant end) {
    return new TimeRange(start, end);
  }
}

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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolution;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.common.annotations.VisibleForTesting;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Handles all things Google Calendar (for now just getting a Meet link).
public class GoogleCalendarAccess implements CalendarAccess {
  private Calendar service;
  private static final String CALENDAR_ID = "info@jqed.dev";

  // TODO: remember to write tests in the code that calls CalendarAccess() that handle what happens
  // per each exception
  public GoogleCalendarAccess(Calendar service) {
    this.service = service;
  }

  // Defines an event.
  @VisibleForTesting
  static Event makeEvent(ScheduledInterview interview, PersonDao personDao) {
    // TODO: add first names to summary
    Event event =
        new Event()
            .setSummary("InterviewMe Interview")
            .setDescription(
                "Use the Meet Link attached to this calendar to conduct the interview.");

    DateTime startDateTime = new DateTime(interview.when().start().toString());
    EventDateTime start = new EventDateTime().setDateTime(startDateTime);
    event.setStart(start);

    DateTime endDateTime = new DateTime(interview.when().end().toString());
    EventDateTime end = new EventDateTime().setDateTime(endDateTime);
    event.setEnd(end);

    event.setAttendees(getAttendees(interview, personDao));

    CreateConferenceRequest createRequest = new CreateConferenceRequest();
    createRequest.setRequestId(String.valueOf(interview.id()));
    createRequest.setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"));
    event.setConferenceData(new ConferenceData().setCreateRequest(createRequest));
    return event;
  }

  // Creates an event in the calendar CALENDAR_ID and returns the Meet Link associated with that
  // event.
  // TODO: update name of this function & use cases, also update emails to reflect participants
  // being invited to
  // an event.
  @Override
  public String getMeetLink(ScheduledInterview interview)
      throws IOException, GeneralSecurityException {
    Event event = makeEvent(interview, new DatastorePersonDao());
    event =
        service
            .events()
            .insert(CALENDAR_ID, event)
            .setConferenceDataVersion(1)
            .setSendUpdates("all")
            .setSupportsAttachments(true)
            .execute();
    return event.getConferenceData().getEntryPoints().get(0).getUri();
  }

  public static List<EventAttendee> getAttendees(
      ScheduledInterview interview, PersonDao personDao) {
    List<EventAttendee> attendees = new ArrayList<>();
    attendees = addAttendeeIfValidId(interview.interviewerId(), attendees, personDao);
    attendees = addAttendeeIfValidId(interview.intervieweeId(), attendees, personDao);
    attendees = addAttendeeIfValidId(interview.shadowId(), attendees, personDao);
    return attendees;
  }

  private static List<EventAttendee> addAttendeeIfValidId(
      String participantId, List<EventAttendee> attendees, PersonDao personDao) {
    if (participantId.isEmpty()) {
      return attendees;
    }
    String email = personDao.get(participantId).map(Person::email).orElse("");
    if (!email.isEmpty()) {
      attendees.add(new EventAttendee().setEmail(email));
    }
    return attendees;
  }

  // Makes a Calendar service.
  public static Calendar MakeCalendar(SecretFetcher secretFetcher)
      throws GeneralSecurityException, IOException {
    String key = secretFetcher.getSecretValue("SERVICE_ACCT_KEY");
    return new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JacksonFactory.getDefaultInstance(),
            GoogleCredential.fromStream(new ByteArrayInputStream(key.getBytes()))
                .createScoped(Collections.singletonList(CalendarScopes.CALENDAR)))
        .setApplicationName("Interview Me CalendarAccess")
        .build();
  }
}

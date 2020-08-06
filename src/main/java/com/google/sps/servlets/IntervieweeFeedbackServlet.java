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
import com.google.sps.data.DatastorePersonDao;
import com.google.sps.data.DatastoreScheduledInterviewDao;
import com.google.sps.data.EmailSender;
import com.google.sps.data.Person;
import com.google.sps.data.PersonDao;
import com.google.sps.data.ScheduledInterview;
import com.google.sps.data.ScheduledInterviewDao;
import com.google.sps.data.SendgridEmailSender;
import com.google.sps.utils.EmailUtils;
import com.google.sps.utils.SendgridEmailUtils;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

// Servlet that gets the feedback from an interviwer and sends it to an interviewee.
@WebServlet("/interviewee-feedback")
public class IntervieweeFeedbackServlet extends HttpServlet {
  private ScheduledInterviewDao scheduledInterviewDao;
  private PersonDao personDao;
  private EmailSender emailSender;
  private EmailUtils emailUtils;
  static final Email sender = new Email("interviewme.business@gmail.com");
  private Path emailsPath =
      Paths.get(
          System.getProperty("user.home") + "/InterviewMe/src/main/resources/templates/email");

  @Override
  public void init() {
    EmailSender emailSender;
    try {
      emailSender = new SendgridEmailSender(sender);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    init(
        new DatastoreScheduledInterviewDao(),
        new DatastorePersonDao(),
        emailSender,
        new SendgridEmailUtils());
  }

  public void init(
      ScheduledInterviewDao scheduledInterviewDao,
      PersonDao personDao,
      EmailSender emailSender,
      EmailUtils emailUtils) {
    this.scheduledInterviewDao = scheduledInterviewDao;
    this.personDao = personDao;
    this.emailSender = emailSender;
    this.emailUtils = emailUtils;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long scheduledInterviewId = Long.parseLong(request.getParameter("interviewId"));
    int numberOfQuestions = Integer.parseInt(request.getParameter("questionCount"));
    HashMap<String, String> answers = new HashMap<String, String>();
    for (int i = 1; i <= numberOfQuestions; i++) {
      String template = String.format("{{question_%s}}", i);
      String param = String.format("question%s", i);
      answers.put(template, request.getParameter(param));
    }

    String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    // Since Users returned from the LocalUserService (in tests) do not have userIds, here we set
    // the userId equal to a hashcode.
    if (userId == null) {
      userId = String.format("%d", userEmail.hashCode());
    }

    Optional<ScheduledInterview> scheduledInterviewOpt =
        scheduledInterviewDao.get(scheduledInterviewId);

    if (!scheduledInterviewOpt.isPresent()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    ScheduledInterview scheduledInterview = scheduledInterviewOpt.get();
    Optional<Person> intervieweeOpt = getInterviewee(scheduledInterview);
    answers.put("{{formatted_date}}", scheduledInterview.getDateString());

    if (!isInterviewer(scheduledInterview, userId)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    if (!intervieweeOpt.isPresent()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    Person interviewee = intervieweeOpt.get();
    try {
      sendFeedback(interviewee.email(), answers);
    } catch (Exception e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    response.sendRedirect("/scheduled-interviews.html");
    return;
  }

  private boolean isInterviewer(ScheduledInterview scheduledInterview, String userId) {
    return scheduledInterview.interviewerId().equals(userId);
  }

  private Optional<Person> getInterviewee(ScheduledInterview scheduledInterview) {
    return personDao.get(scheduledInterview.intervieweeId());
  }

  private void sendFeedback(String intervieweeEmail, HashMap<String, String> answers)
      throws IOException {
    String subject = "Your Interviewer has submitted feedback for your interview!";
    Email recipient = new Email(intervieweeEmail);
    String contentString =
        emailUtils.fileContentToString(emailsPath + "/feedbackToInterviewee.txt");
    Content content = new Content("text/plain", emailUtils.replaceAllPairs(answers, contentString));
    emailSender.sendEmail(recipient, subject, content);
  }
}

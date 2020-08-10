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

// using SendGrid's Java Library
// https://github.com/sendgrid/sendgrid-java
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;

// Handles sending emails.
@WebServlet("/email")
public class SendgridEmailSender implements EmailSender {
  private final Email sender;
  private final SendGrid sg;

  public SendgridEmailSender(Email sender) throws IOException {
    this.sender = sender;
    this.sg = new SendGrid(new SecretFetcher("interviewme2020").getSecretValue("SENDGRID_API_KEY"));
  }

  // Sends an email from the "sender" Email to the "recipient" Email, with specified subject and
  // content. Returns a response from the sendgrid email sending service.
  @Override
  public int sendEmail(Email recipient, String subject, Content content) throws IOException {
    Mail mail = new Mail(sender, subject, recipient, content);

    Request request = new Request();
    Response response;
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());
    response = sg.api(request);
    return response.getStatusCode();
  }
}

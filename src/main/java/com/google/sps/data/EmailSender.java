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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;

// Handles sending emails.
@WebServlet("/email")
public class EmailSender {
  // The email messages are being sent from.
  private final Email sender;
  private final SendGrid sg;

  public EmailSender(Email sender) throws Exception {
    this.sender = sender;
    this.sg =
        new SendGrid(
            new SecretFetcher("interview-me-step-2020").getSecretValue("SENDGRID_API_KEY"));
  }

  // Sends an email from the "from" Email to the "to" Email, with specified subject and content.
  public Response sendEmail(Email recipient, String subject, Content content)
      throws IOException, Exception {
    Mail mail = new Mail(sender, subject, recipient, content);

    Request request = new Request();
    Response response;
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());
    response = sg.api(request);
    return response;
  }

  // Returns the contents of the file specified at filePath as a String. Useful for converting
  // predifined email templates to text.
  public static String fileContentToString(String filePath) throws IOException {
    StringBuilder contentBuilder = new StringBuilder();
    try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }
    return contentBuilder.toString();
  }

  /**
   * Modifies and returns @param str. Replaces all occurences in @param str of each key in @param
   * toReplace with its corresponding value.
   */
  // Ex. str = "You will be mock interviewing {{interviewee_full_name}} on {{formatted_date}}."
  // toReplace = { ("{{interviewee_full_name}}","Tess"), ("{{formatted_date}}", "June 6, 2022") }
  // Returned: "You will be mock interviewing Tess on June 6, 2022."
  public static String replaceAllPairs(HashMap<String, String> toReplace, String str) {
    for (Map.Entry<String, String> entry : toReplace.entrySet()) {
      str = str.replace(entry.getKey(), entry.getValue());
    }
    return str;
  }
}

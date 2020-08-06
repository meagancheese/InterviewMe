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

import com.sendgrid.Response;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.util.HashMap;

/** EmailSender includes the basic methods anything managing sending emails must support. */
public interface EmailSender {

  // Sends an email to the "recipient" Email, with specified subject and content. Returns a response
  // from the sendgrid email sending service.
  public int sendEmail(Email recipient, String subject, Content content) throws IOException;
}

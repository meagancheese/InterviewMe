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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Servlet that handles logout. */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

  // Unsets the "logged in" cookies to "log the user out" of our site without logging them out of
  // Gmail. Reference:
  // http://ptspts.blogspot.com/2011/12/how-to-log-out-from-appengine-app-only.html
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Cookie[] cookies = request.getCookies();
    // This means user has not logged in, no cookies have been created.
    if (cookies == null) {
      return;
    }
    Set<String> cookiesToUnset =
        new HashSet<>(Arrays.asList("dev_appserver_login", "SACSID", "ACSID"));
    for (Cookie cookie : cookies) {
      if (cookiesToUnset.contains(cookie.getName())) {
        cookie.setValue("");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
      }
    }
    response.sendRedirect("/");
    return;
  }
}

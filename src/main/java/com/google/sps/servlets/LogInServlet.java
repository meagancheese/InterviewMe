package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LogInServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    response.getWriter().println(new Gson().toJson(getLoginInfo("/")));
  }

  // Returns a LoginInfo that represents the logged in status.
  public static LoginInfo getLoginInfo(String rootUrl) {
    UserService userService = UserServiceFactory.getUserService();
    boolean loggedIn = userService.isUserLoggedIn();
    LoginInfo toSend;
    if (loggedIn) {
      return new LoginInfo(
          loggedIn, userService.createLogoutURL(rootUrl), userService.getCurrentUser().getEmail());
    }
    return new LoginInfo(loggedIn, userService.createLoginURL(rootUrl), "");
  }

  // Represents the logged in status of the site.
  public static class LoginInfo {
    public boolean loggedIn;
    public final String changeLogInStatusURL;
    public final String email;

    public LoginInfo(boolean loggedIn, String changeLogInStatusURL, String email) {
      this.loggedIn = loggedIn;
      this.changeLogInStatusURL = changeLogInStatusURL;
      this.email = email;
    }
  }
}

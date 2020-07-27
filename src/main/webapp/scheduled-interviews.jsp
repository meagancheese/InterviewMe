<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@ page import="com.google.sps.data.ScheduledInterviewRequest" %>
<%
  List<ScheduledInterviewRequest> scheduledInterviews = (List<ScheduledInterviewRequest>) request.getAttribute("scheduledInterviews");
  pageContext.setAttribute("scheduledInterviews", scheduledInterviews);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:choose>
  <c:when test= "${empty scheduledInterviews}">
    <h2 style="text-align: center">No Scheduled Interviews</h2>
  </c:when>
  <c:otherwise>
    <c:forEach items= "${pageScope.scheduledInterviews}" var="scheduledInterview">
      <div data-id = "${scheduledInterview.getId()}" class="row">
        <div class="card w-75 scheduled-interview-card">
          <div class="card-body">
            <h5 class="card-title">Your Role: ${scheduledInterview.getRole()}</h5>
            <p class="card-text">${scheduledInterview.getDateString()}</p>
          </div>
          <ul class="list-group list-group-flush">
            <li class="list-group-item">Interviewee: ${scheduledInterview.getInterviewee()}</li>
            <li class="list-group-item">Interviewer: ${scheduledInterview.getInterviewer()}</li>
          </ul>
        </div>
      </div>
    </c:forEach>
  </c:otherwise>
</c:choose>
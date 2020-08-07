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
            <li class="list-group-item">Shadow: ${scheduledInterview.getShadow()}</li>
            <li class="list-group-item">
              <a href=${scheduledInterview.getMeetLink()} target="_blank">
                Meet Link
              </a>
            </li>
          </ul>
          <c:choose>
            <c:when test ="${scheduledInterview.getRole().equals('Interviewer') || scheduledInterview.getRole().equals('Interviewee')}">
              <c:choose>
                <c:when test= "${scheduledInterview.getHasStarted()}">
                  <a href="feedback.html?interview=${scheduledInterview.getId()}&role=${scheduledInterview.getRole()}" style="text-align:center">
                    <button class="btn btn-primary" type="button">Submit Feedback</button>
                  </a>
                </c:when>
                <c:otherwise>
                  <p style="text-align:center">You may begin feedback 5 minutes after the interview has started.</p>
                </c:otherwise>
              </c:choose>
            </c:when>
          </c:choose>
        </div>
      </div>
    </c:forEach>
  </c:otherwise>
</c:choose>
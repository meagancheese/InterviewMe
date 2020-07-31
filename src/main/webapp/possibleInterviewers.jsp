<%@ page import="java.util.Set" %>
<%@ page import="com.google.sps.data.PossibleInterviewer" %>
<%
  pageContext.setAttribute("set", (Set<PossibleInterviewer>) request.getAttribute("interviewers"));
  pageContext.setAttribute("utc", request.getParameter("utcStartTime"));
  pageContext.setAttribute("time", request.getParameter("time"));
  pageContext.setAttribute("date", request.getParameter("date"));
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<table class="table">
  <thead>
     <tr>
        <th scope="col">Company</th>
        <th scope="col">Job</th>
     </tr>
  </thead>
  <tbody>
    <c:forEach items = "${pageScope.set}" var = "interviewer">
      <tr>
        <td class="check-specified">${interviewer.company()}</td>
        <td class="check-specified">${interviewer.job()}</td>
        <td>
          <button type="button" class="btn btn-primary" 
              data-company="${interviewer.company()}" data-job="${interviewer.job()}" 
              data-utc="${pageScope.utc}" data-time="${pageScope.time}"
              data-date="${pageScope.date}" onclick="selectInterview(this)">
            Select
          </button>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>

<%@ page import="com.google.sps.data.AvailabilityTimeSlotGenerator" %>
<%@ page import="com.google.sps.data.AvailabilityTimeSlot" %>
<%@ page import="com.google.sps.data.DatastoreAvailabilityDao" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.Instant" %>
<%@ page import="java.time.temporal.ChronoUnit" %>
<%@ page import="java.lang.Integer" %>
<%
  int timeZoneOffset = Integer.parseInt(request.getParameter("timeZoneOffset"));
  int daysOffset = Integer.parseInt(request.getParameter("page")) * 7;
  List<List<AvailabilityTimeSlot>> list = 
      AvailabilityTimeSlotGenerator.timeSlotsForWeek(
          Instant.now().plus(daysOffset, ChronoUnit.DAYS), 
          timeZoneOffset, 
          new DatastoreAvailabilityDao());
  pageContext.setAttribute("list", list);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<table class="table table-sm text-center">
  <thead>
    <tr>
      <c:forEach items = "${pageScope.list}" var = "day">
        <th scope="col">${day.get(0).date()}</th>
      </c:forEach>
    </tr>
  </thead>
  <tbody>
    <!-- TODO: Allow clicking and scrolling over multiple slots to select them.-->
    <!-- TODO: Change page format so that it is vertically condensed.-->
    <c:forEach var = "i" begin = "0" end = "${pageScope.list.get(0).size() - 1}">
      <tr>
        <c:forEach items = "${pageScope.list}" var = "day">
          <td onclick="toggleTile(this)" data-utc="${day.get(i).utcEncoding()}" 
              class="${day.get(i).getClassList()}">
            ${day.get(i).time()}
          </td>
        </c:forEach>
      </tr>
    </c:forEach>
  </tbody>
</table>

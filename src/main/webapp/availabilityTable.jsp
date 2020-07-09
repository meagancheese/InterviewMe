<%@ page import="com.google.sps.data.AvailabilityTimeSlotGenerator" %>
<%@ page import="com.google.sps.data.AvailabilityTimeSlot" %>
<%@ page import="java.util.List,java.time.Instant" %>
<%@ page import="java.lang.Integer" %>
<%
  List<AvailabilityTimeSlot> list = AvailabilityTimeSlotGenerator
    .timeSlotsForDay(Instant.now(), Integer.parseInt(request
    .getParameter("timeZoneOffset")));
  pageContext.setAttribute("list", list);
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<table class="table table-sm text-center">
  <thead>
    <tr>
      <th scope="col">${list.get(0).date()}</th>
    </tr>
  </thead>
  <tbody>
    <!-- TODO: Allow clicking and scrolling over multiple slots to select them.-->
    <!-- TODO: Change page format so that it is vertically condensed.-->
    <c:forEach items = "${pageScope.list}" var = "timeSlot">
      <tr>
        <td onclick="toggleTile(this)" data-utc="${timeSlot.utcEncoding()}" 
          class="${timeSlot.selected() ? 'table-success' : ''}">
          ${timeSlot.time()}
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>

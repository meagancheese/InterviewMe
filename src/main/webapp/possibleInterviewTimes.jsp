<%@ page import="java.util.List" %>
<%@ page import="com.google.sps.data.PossibleInterviewSlot" %>
<%
  pageContext.setAttribute("list", (List<List<PossibleInterviewSlot>>) request.getAttribute("monthList"));
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:choose>
  <c:when test= "${empty pageScope.list}">
    <h2 style="text-align: center">No interviews are available at this time.</h2>
  </c:when>
  <c:otherwise>
    <c:forEach items = "${pageScope.list}" var = "day">
      <form>
        <div class="form-row">
          <div class="col-3 date-label">
            <label>${day.get(0).date()}</label>
          </div>
          <div class="col-5">
            <select class="form-control" id="${day.get(0).date()}">
              <c:forEach items = "${day}" var = "slot">
                <option value="${slot.utcEncoding()}" data-date="${slot.date()}">
                  ${slot.time()}
                </option>
              </c:forEach>
            </select>
          </div>
          <div class="col-4">
            <button type="button" class="btn btn-primary mb-2"
              onclick="showInterviewers(this)" data-date="${day.get(0).date()}">
              Select
            </button>
          </div>
        </div>
      </form>
      <br>
    </c:forEach>
  </c:otherwise>
</c:choose>  

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
  boolean canOpen = (boolean) request.getAttribute("feedbackOpen");
  String role = (String) request.getAttribute("role");
  pageContext.setAttribute("feedbackOpen", canOpen);
  pageContext.setAttribute("role", role); 
%>

<c:choose>
  <c:when test= "${canOpen == false}">
    <h2 style="text-align: center">You may not submit feedback yet.</h2>
  </c:when>
  <c:otherwise>
    <c:choose>
      <c:when test= "${role == 'Interviewee'}">
        <h1 class="text-center">Feedback</h1>
        <h4 class="text-center">Please submit your feedback for your interviewer below.</h4>
        <h5 class="text-center lead">Please select an option for each prompt.</h5>
        <form
          novalidate
          name="feedbackForm" method="POST"
          onSubmit="addScheduledInterviewId()"
          action="/interviewer-feedback"
          class="needs-validation"
          onkeydown="return event.key != 'Enter';"
        >
          <div class="form-group">
            <div class="form-group form-inline"> 
              <label for="question1">I was comfortable during the interview:</label><br>
              <select class="form-control" name="question1">
                  <option>Strongly Agree</option>
                  <option>Agree</option>
                  <option>Neutral</option>
                  <option>Disagree</option>
                  <option>Strongly Disagree</option>
                </select><br>
            </div>
            <div class="form-group form-inline">
              <label for="question2">My interviewer moved at a decent pace:</label><br>
              <select class="form-control" name="question2">
                  <option>Strongly Agree</option>
                  <option>Agree</option>
                  <option>Neutral</option>
                  <option>Disagree</option>
                  <option>Strongly Disagree</option>
                </select><br>
            </div>
            <div class="form-group form-inline">
              <label for="question3">My interviewer was able to stay on topic:</label><br>
                <select class="form-control" name="question3">
                  <option>Strongly Agree</option>
                  <option>Agree</option>
                  <option>Neutral</option>
                  <option>Disagree</option>
                  <option>Strongly Disagree</option>
                </select><br>
            </div>
            <div class="form-group form-inline">
              <label for="question4">My interviewer was able to provide advice or suggestions:</label><br>
              <select class="form-control" name="question4">
                  <option>Strongly Agree</option>
                  <option>Agree</option>
                  <option>Neutral</option>
                  <option>Disagree</option>
                  <option>Strongly Disagree</option>
                </select><br>
            </div>
            <div class="form-group form-inline">
              <label for="question5">My interviewer answered my questions to the best of their ability:</label><br>
              <select class="form-control" name="question5">
                  <option>Strongly Agree</option>
                  <option>Agree</option>
                  <option>Neutral</option>
                  <option>Disagree</option>
                  <option>Strongly Disagree</option>
                </select><br>
            </div>
            <div class="form-group form-inline">
              <label for="question6">I felt my interviewer came prepared:</label><br>
              <select class="form-control" name="question6">
                  <option>Strongly Agree</option>
                  <option>Agree</option>
                  <option>Neutral</option>
                  <option>Disagree</option>
                  <option>Strongly Disagree</option>
                </select><br>
            </div>
            <div class="form-group form-inline">
              <label for="question7">This InterviewMe experience was helpful:</label><br>
              <select class="form-control" name="question7">
                  <option>Strongly Agree</option>
                  <option>Agree</option>
                  <option>Neutral</option>
                  <option>Disagree</option>
                  <option>Strongly Disagree</option>
                </select><br>
            </div>
            <label for="question8">Is there anything in particular that your interviewer did to improve your overall experience?</label><br>
            <textarea rows="4" cols="50" class="form-control" id="question8" name="question8" required></textarea><br>
            <label for="question9">What is something that you think your interviewer could have done better?</label><br>
            <textarea rows="4" cols="50" class="form-control" id="question9" name="question9"></textarea><br>
            <input type="hidden" id="questionCount" name="questionCount" value="9">
            <input type="hidden" id="interviewId" name="interviewId" value="">
          </div>
          <div style="text-align: center">
            <button class="btn btn-primary" type="submit" style="text-align: center">Submit</button>
          </div>
        </form>
      </c:when>
      <c:otherwise>
        <c:choose>
          <c:when test= "${role == 'Interviewer'}">
            <h1 class="text-center">Feedback</h1>
            <h5 class="text-center">Please submit your feedback for your interviewee below.</h4>
            <h5 class="text-center lead">Please select an option for each prompt.</h5>
            <form
              novalidate
              name="feedbackForm"
              method="POST"
              onSubmit="addScheduledInterviewId()"
              action="/interviewee-feedback"
              class="needs-validation"
              onkeydown="return event.key != 'Enter';"
            >
              <div class="form-group">
                <div class="form-group form-inline"> 
                  <label for="question1">The interviewee communicated their thought process as they went along:</label><br>
                  <select class="form-control" name="question1">
                    <option>Strongly Agree</option>
                    <option>Agree</option>
                    <option>Neutral</option>
                    <option>Disagree</option>
                    <option>Strongly Disagree</option>
                </select><br>
                </div>
                <div class="form-group form-inline"> 
                  <label for="question2">The interviewee understood the time complexity of their solution:</label><br>
                  <select class="form-control" name="question2">
                    <option>Strongly Agree</option>
                    <option>Agree</option>
                    <option>Neutral</option>
                    <option>Disagree</option>
                    <option>Strongly Disagree</option>
                </select><br>
                </div>
                <div class="form-group form-inline">
                  <label for="question3">The interviewee took the time to consider better solutions:</label><br>
                  <select class="form-control" name="question3">
                    <option>Strongly Agree</option>
                    <option>Agree</option>
                    <option>Neutral</option>
                    <option>Disagree</option>
                    <option>Strongly Disagree</option>
                </select><br>
                </div>
                <div class="form-group form-inline">
                  <label for="question4">The interviewee came up with an example that they used to test their solution:</label><br>
                  <select class="form-control" name="question4">
                    <option>Strongly Agree</option>
                    <option>Agree</option>
                    <option>Neutral</option>
                    <option>Disagree</option>
                    <option>Strongly Disagree</option>
                </select><br>
                </div>
                <div class="form-group form-inline">
                  <label for="question5">The interviewee listed a handful of edge cases and accounted for their behaviour:</label><br>
                  <select class="form-control" name="question5">
                    <option>Strongly Agree</option>
                    <option>Agree</option>
                    <option>Neutral</option>
                    <option>Disagree</option>
                    <option>Strongly Disagree</option>
                </select><br>
                </div>
                <div class="form-group form-inline">
                  <label for="question6">The interviewee asked clarifying questions:</label><br>
                  <select class="form-control" name="question6">
                    <option>Strongly Agree</option>
                    <option>Agree</option>
                    <option>Neutral</option>
                    <option>Disagree</option>
                    <option>Strongly Disagree</option>
                </select><br>
                </div>
                <div class="form-group form-inline">
                  <label for="question7">This InterviewMe experience was helpful:</label><br>
                  <select class="form-control" name="question7">
                    <option>Strongly Agree</option>
                    <option>Agree</option>
                    <option>Neutral</option>
                    <option>Disagree</option>
                    <option>Strongly Disagree</option>
                </select><br>
                </div>
                <label for="question8">What is the interviewee's strongest skill?</label><br>
                <textarea rows="4" cols="50" class="form-control" id="question8" name="question8" required></textarea><br>
                <label for="question9">What is one skill they should work on improving in order to become a better candidate?</label><br>
                <textarea rows="4" cols="50" class="form-control" id="question9" name="question9" required></textarea><br>
                <label for="question10">Evaluate this candidate's solution (Keep to three sentences or less).</label><br>
                <textarea rows="4" cols="50" class="form-control" id="question10" name="question10" required></textarea><br>
                <label for="question11">Notes:</label><br>
                <textarea rows="4" cols="50" class="form-control" id="question11" name="question11" required></textarea><br>
                <input type="hidden" id="questionCount" name="questionCount" value="11">
                <input type="hidden" id="interviewId" name="interviewId" value="">
              </div>
              <div style="text-align: center">
                <button class="btn btn-primary" type="submit" style="text-align: center">Submit</button>
              </div>
            </form>
          </c:when>
        </c:choose> 
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>

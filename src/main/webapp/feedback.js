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

function onFeedbackLoad() {
  const loginInfo = getLoginInfo();
  loginInfo.then(supplyLogoutLinkOrRedirectHome); 
  loginInfo.then(getUserOrRedirectRegistration);
  loadFeedback(); 
}

// Calls the feedback servlet which determines whether or not the feedback form is submitted or not.
function loadFeedback() {
  fetch(`/feedback?timeZone=${getBrowserTimeZone()}&userTime=${getCurrentTime()}&interview=${getScheduledInterviewId()}&role=${getRole()}`)
    .then(response => response.text())
    .then(form => {
      document.getElementById('feedBackForm').innerHTML = form;
    }); 
}

// Returns the id of the interview that feedback is for.
function getScheduledInterviewId() {
  return new URLSearchParams(window.location.search).get('interview');  
}

// Returns the role of a particular user. 
function getRole() {
  return new URLSearchParams(window.location.search).get('role'); 
}

// Adds the scheduledInterviewId to the request when the form is submitted.
function addScheduledInterviewId() {
  document.feedbackForm.interviewId.value = getScheduledInterviewId();
}

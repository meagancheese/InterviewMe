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

function onSearchInterviewLoad() {
  supplyLogoutLink();
}

// Should query Datastore for appropriate interviews and render them on the
// page.
function loadInterviews() {
  // TODO: fetch and get all interviews instead of unhiding something hidden
  const searchResultsDiv = document.getElementById("search-results");
  searchResultsDiv.removeAttribute("hidden");
}

// Confirms interview selection with user and sends this selection to Datastore 
// if confirmed.
function selectInterview(interviewer) {
  if (confirm(
    // TOOD: fill in these times dynamically from ids in the .jsp file.
      `You selected: Sunday 7/5 from 6:30 PM - 7:30 PM with a ` +
      `${interviewer.getAttribute("data-company")} ` +
      `${interviewer.getAttribute('data-job')}. ` +
      `Click OK if you wish to proceed.`)) {
    alert(
      `You have scheduled an interview on Sunday 7/5 from 6:30 PM - 7:30 ` +
      `PM with a ${interviewer.getAttribute('data-company')} ` +
      `${interviewer.getAttribute('data-job')}. Check your email for more ` +
      `information.`);
    // TODO: Call a servlet to save this selection.
    location.reload();
  }
}

// Fills in the modal with interviewer info from Datastore and shows it.
function showInterviewers(selectButton) {
  fetch('/search-interview-interviewers.jsp').then(response => response.text())
    .then(table => {
      $('#modal-body').html(table);
      // TODO: format the date as June 5, 2020 not 7/5/2020 (more readable).
      const date = selectButton.getAttribute("id");
      const time = document.getElementById(date + '-options').value;
      $('#modal-title').text(`Interviewers Information for ${date} at ${time}`);
    });
  $('#interviewer-modal').modal('show');
}

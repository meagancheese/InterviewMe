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
  const loginInfo = getLoginInfo();
  loginInfo.then(ifLoggedOutRedirectHome); 
  loginInfo.then(getUserOrRedirectRegistration);
}

// Queries Datastore for available interview times and renders them on the
// page.
function loadInterviews() {
  const searchResultsDiv = document.getElementById('search-results');
  searchResultsDiv.removeAttribute('hidden');
  const role = selectedRole();
  let servlet = '';
  if (role === 'Interviewee') {
    servlet = 'load-interviews';
  } else if (role === 'Shadow') {
    servlet = 'shadow-load-interviews';
  }
  fetch(`/${servlet}?timeZoneOffset=${browserTimezoneOffset()}&position=${selectedEnumPosition()}`)
    .then(response => response.text())
    .then(interviewTimes => {
      interviewTimesDiv().innerHTML = interviewTimes;
    });
}

function selectedRole() {
  return document.getElementById('role').value;
}

function selectedEnumPosition() {
  let position = document.getElementById('position').value;
  return position.toUpperCase().replace(' ', '_');
}

function interviewTimesDiv() {
  return document.getElementById('interview-times-container');
}

// Confirms interview selection with user and sends this selection to Datastore 
// if confirmed.
function selectInterview(interviewer) {
  let date = interviewer.getAttribute('data-date');
  let time = interviewer.getAttribute('data-time');
  let company = interviewer.getAttribute('data-company');
  let job = interviewer.getAttribute('data-job');
  if (company === '') {
    company = '<Not specified>';
  }
  if (job === '') {
    job = '<Not specified>';
  }
  let utcStartTime = interviewer.getAttribute('data-utc');
  let position = document.getElementById('position').value;
  let role = selectedRole();
  if (role === 'Interviewee') {
    if (confirm(
        `You selected: ${date} from ${time} with a ` +
        `${company} ${job}. ` +
        `Click OK if you wish to proceed.`)) {
      alert(
        `You have scheduled a ${position} interview on ${date}` +
        ` from ${time} with a ${company} ` +
        `${job}. Check your email for more ` +
        `information.`);
      updateDatastore('POST', company, job, utcStartTime);
    }
  } else if (role === 'Shadow') {
    if (confirm(
        `You selected: ${date} from ${time} with a ` +
        `${company} ${job}. ` +
        `Click OK if you wish to proceed.`)) {
      alert(
        `You will shadow a ${position} interview on ${date}` +
        ` from ${time} with a ${company} ` +
        `${job}. Check your email for more ` +
        `information.`);
      updateDatastore('PUT', company, job, utcStartTime);
    }
  }
}

function updateDatastore(method, company, job, utcStartTime) {
  if (company === '<Not specified>') {
    company = '';
  }
  if (job === '<Not specified>') {
    job = '';
  }
  let requestObject = {
    company: company,
    job: job,
    utcStartTime: utcStartTime,
    position: selectedEnumPosition()
  };
  let requestBody = JSON.stringify(requestObject);
  let request = new Request('/scheduled-interviews', {method: method, body: requestBody});
  fetch(request).then(() => {window.location.replace('/scheduled-interviews.html');});
}

// Fills in the modal with interviewer info from Datastore and shows it.
function showInterviewers(selectButton) {
  const date = selectButton.getAttribute('data-date');
  const select = document.getElementById(date);
  const time = select.options[select.selectedIndex].text;
  const reformattedTime = time.replace('-', 'to');
  const utc = select.value;
  const role = selectedRole();
  let servlet = '';
  if (role === 'Interviewee') {
    servlet = 'show-interviewers';
  } else if (role === 'Shadow') {
    servlet = 'shadow-show-interviewers';
  }
  fetch(`/${servlet}?utcStartTime=${utc}&date=${date}&time=${reformattedTime}&position=${selectedEnumPosition()}`)
    .then(response => response.text())
    .then(interviewers => {
      $('#modal-body').html(interviewers);
      $('#modal-title').text(`Qualified Interviewers Information for ${date} from ${reformattedTime}`);
      $('#interviewer-modal').modal('show');
      checkIfSpecified();
    });
}

function checkIfSpecified() {
  $('.check-specified').each((index, el) => {
    if (el.innerHTML === '') {
      el.innerHTML = '&lt;Not specified&gt;';
    }
  });
}

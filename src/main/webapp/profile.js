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

function onProfileLoad() {
  const loginInfo = getLoginInfo();
  loginInfo.then(supplyLogoutLinkOrRedirectHome);  
  loginInfo.then(getUserOrRedirectRegistration).then((person) => {
    autofillForm(person);      
  });
  prepareFormValidation();
}

// Submits profile form to Datastore.
function submitProfileForm(methodType, redirectUrl) {
  const personJson = {
    email: $('#user-email').val(),
    firstName: $('#first-name-field').val(),
    lastName: $('#last-name-field').val(),
    company: $('#company-field').val(),
    job: $('#job-field').val(),
    linkedin: $('#linkedin-field').val()
  };
  fetch('/person',{
    method: methodType,
    body: JSON.stringify(personJson)
  }).then(window.location.replace(redirectUrl))
    .catch((error) => {
      alert('Error: ' + error + '\nThere was an error submitting your information.' +
      ' Please try again.');
    });
}

// Fills in the profile form with data from Datastore.
function autofillForm(person) {
  document.getElementById('user-email').value = person.email;
  document.getElementById('first-name-field').value = person.firstName;
  document.getElementById('last-name-field').value = person.lastName;
  document.getElementById('company-field').value = person.company;
  document.getElementById('job-field').value = person.job;
  document.getElementById('linkedin-field').value = person.linkedIn;    
}

// Allows certain fields in the profile to be edited, hides edit button, and displays update button. 
function makeEditable() {
  const editButton = document.getElementById('edit-button');
  const updateButton = document.getElementById('update-button');
  editButton.setAttribute('hidden', true);
  updateButton.removeAttribute('hidden');
  
  const editableFields = document.getElementsByClassName('editable');
  Array.from(editableFields).forEach(function(editableField) {
    editableField.removeAttribute('readonly');
    editableField.classList.remove('form-control-plaintext');
    editableField.classList.add('form-control');
  });
  
  const currentJob = document.getElementById('current-job');
  currentJob.setAttribute('hidden', true);
  const jobField = document.getElementById('job-field');
  jobField.removeAttribute('hidden');
}

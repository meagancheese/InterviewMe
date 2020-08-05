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
  loginInfo.then(ifLoggedOutRedirectHome); 
  loginInfo.then(getUserOrRedirectRegistration).then((person) => {
    autofillForm(person);      
  });
}

// Submits profile form to Datastore.
function submitProfileForm(methodType, redirectUrl) {
  if(!validateProfileForm()) {
    return;
  }
  document.getElementById('company-field').placeholder = '';
  document.getElementById('linkedin-field').placeholder = '';
  let possibleQualifiedJobs = document.getElementsByClassName('qualification');
  let qualifiedJobs = [];
  for (let job of possibleQualifiedJobs) {
    if (job.checked) {
      qualifiedJobs.push(job.getAttribute('data-enum-name'));
    }
  }
  const personJson = {
    firstName: $('#first-name-field').val(),
    lastName: $('#last-name-field').val(),
    company: $('#company-field').val(),
    job: $('#job-field').val(),
    linkedIn: $('#linkedin-field').val(),
    qualifiedJobs: qualifiedJobs
  };
  fetch('/person',{
    method: methodType,
    body: JSON.stringify(personJson)
  }).then(() => window.location.replace(redirectUrl))
    .catch((error) => {
      alert('Error: ' + error + '\nThere was an error submitting your information.' +
      ' Please try again.');
    });
}

function validateProfileForm() {
  const form = document.getElementById('profile-form');
  form.classList.add('was-validated');
  return form.checkValidity();
}

// Fills in the profile form with data from Datastore.
function autofillForm(person) {
  document.getElementById('user-email').value = person.email;
  document.getElementById('first-name-field').value = person.firstName;
  document.getElementById('last-name-field').value = person.lastName;
  document.getElementById('company-field').value = person.company;
  document.getElementById('current-job').textContent = person.job;
  document.getElementById('job-field').value = person.job;
  document.getElementById('linkedin-field').value = person.linkedIn;
  for (let qualifiedJob of person.qualifiedJobs) {
    document.getElementById(enumNameToId(qualifiedJob)).checked = true;
  }
}

function enumNameToId(enumName) {
  return enumName.toLowerCase().replace('_', '-').concat('-check');
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
  
  document.getElementById('company-field').placeholder = 'Company';
  document.getElementById('linkedin-field').placeholder = 'LinkedIn';
  
  const currentJob = document.getElementById('current-job');
  currentJob.setAttribute('hidden', true);
  const jobField = document.getElementById('job-field');
  jobField.removeAttribute('hidden');
  
  const checkboxes = document.getElementsByClassName('check-editable');
  Array.from(checkboxes).forEach(function(checkbox) {
    checkbox.removeAttribute('disabled');
  });
}

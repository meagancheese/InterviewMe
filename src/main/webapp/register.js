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

function onRegisterLoad() {
  const loginInfo = getLoginInfo();
  loginInfo.then(supplyLogoutLinkOrRedirectHome); 
  loginInfo.then(autofillEmail).then(prepareFormValidation);
}

// Autofills email on the registration form of the email of the logged in user.
function autofillEmail(loginInfo) {
  $('#user-email').val(loginInfo.email);
} 

// Finds fields that need an input and makes sure that submit won't go through if they are empty. 
// Straight from Bootstrap.
function prepareFormValidation() {
  // Fetch all the forms we want to apply custom Bootstrap validation styles to
  var forms = document.getElementsByClassName('needs-validation');
  // Loop over them and prevent submission
  Array.prototype.filter.call(forms, function(form) {
    form.addEventListener('submit', function(event) {
      if (form.checkValidity() === false) {
        event.preventDefault();
        event.stopPropagation();
      }
      form.classList.add('was-validated');      
    }, false);
  });
}

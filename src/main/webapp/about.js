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

// Login status decisions should follow same flow as on home page, since this is publicly accessible.
function onAboutLoad() {
  checkLogin();
}

// Disables / enables navbar links and adjusts login / logout tab depending on if user is logged in 
// or out.
// If user is logged in but not registered, redirects to registration.
function checkLogin() {
  fetch('/login').then(response => response.json()).then(status => {
    if(status.loggedIn){
      restrictedTabs = document.getElementsByClassName('restricted-tab');
      for(let element of restrictedTabs) {
        element.classList.remove('disabled');
      }
      document.getElementById('logout-tab').removeAttribute('hidden');
      // If not registered, redirect to registration.
      let loginInfo = getLoginInfo();
      loginInfo.then(getUserOrRedirectRegistration);
    } else {
      restrictedTabs = document.getElementsByClassName('restricted-tab');
      for(let element of restrictedTabs) {
        element.classList.add('disabled');
      }
      document.getElementById('login-tab').removeAttribute('hidden');
      document.getElementById('login-tab').href = status.loginUrl;
    }
  });
}

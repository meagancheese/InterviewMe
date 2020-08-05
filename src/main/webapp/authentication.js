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

// If user is logged in, returns a logout link. If not, redirects to home page.
function ifLoggedOutRedirectHome(loginInfo) {
  if (!loginInfo.loggedIn) {
    window.location.replace('/');
  }
}

function getLoginInfo() {
  return fetch('/login').then(response => {
    return response.json();
  });
}

function logout(){
  fetch('/logout').then(response => {
    if(response.redirected) {
      window.location.href = response.url;
    }
  });
}

// Returns a Person if they registered in the past. If not, redirect to  
// registration page.
function getUserOrRedirectRegistration(loginInfo){
  return fetch('/person')
    .then(response => {
      if (response.redirected) {
        window.location.href = response.url;
        return;
      }
      return response.json();
    });
}

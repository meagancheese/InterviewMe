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

let mousedown = false;

function onAvailabilityLoad() {
  const loginInfo = getLoginInfo();
  loginInfo.then(ifLoggedOutRedirectHome); 
  loginInfo.then(getUserOrRedirectRegistration);
  loadAvailabilityTable(availabilityTableDiv(), browserTimezoneOffset());
}

function markMouseDown() {
  mousedown = true;
}

function markMouseUp() {
  mousedown = false;
}

// Toggles a tile from selected (green) to un-selected (white) and vice versa when clicked.
// Scheduled tiles (red) remain unaffected.
function toggleTile(tile) {
  // Should only toggle if the mouse is down and is on this tile.
  if (!mousedown) {
    return;
  }
  let classList = tile.classList;
  if (classList.contains('selected-time-slot')) {
    classList.remove('table-success', 'selected-time-slot');
    return;
  }
  if (!classList.contains('scheduled-time-slot')) {
    classList.add('table-success', 'selected-time-slot');
  }
}

function loadAvailabilityTable(tableDiv, timezoneOffset) {
  fetch(`/availabilityTable.jsp?timeZoneOffset=${timezoneOffset}&page=${page}`)
    .then(response => response.text())
    .then(tableContents => {
      tableDiv.innerHTML = tableContents;
    });
}

function availabilityTableDiv() {
  return document.getElementById('table-container');
}

function updateAvailability(reload) {
  let selectedSlots = document.getElementsByClassName('selected-time-slot');
  let scheduledSlots = document.getElementsByClassName('scheduled-time-slot');
  let markedSlots = Array.from(selectedSlots).concat(Array.from(scheduledSlots));
  let firstSlot = document.getElementsByTagName('tbody').item(0)
    .firstElementChild.firstElementChild.nextElementSibling.getAttribute('data-utc');
  let lastSlot = document.getElementsByTagName('tbody').item(0)
    .lastElementChild.lastElementChild.getAttribute('data-utc');
  let requestObject = {
    firstSlot: firstSlot,
    lastSlot: lastSlot,
    markedSlots: markedSlots.map(s => s.getAttribute('data-utc')),
  };
  let requestBody = JSON.stringify(requestObject);
  let request = new Request('/availability', {method: 'PUT', body: requestBody});
  fetch(request).then(() => {
      if (reload) {
        location.reload();
      }
    }).catch((error) => {
    alert('Error: ' + error + '\nThere was an error submitting your availability.' +
      ' Please try again.');
    });
}

let page = 0;
const maxWeeksAhead = 3;

function goBack() {
  if (page <= 0) {
    page = 0;
    return;
  }
  updateAvailability(false);
  page -= 1;
  loadAvailabilityTable(availabilityTableDiv(), browserTimezoneOffset());
}

function goForward() {
  if (page >= maxWeeksAhead) {
    page = maxWeeksAhead;
    return;
  }
  updateAvailability(false);
  page += 1;
  loadAvailabilityTable(availabilityTableDiv(), browserTimezoneOffset());
}

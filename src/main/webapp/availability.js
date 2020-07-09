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

function onAvailabilityLoad() {
  supplyLogoutLink();
  loadAvailabilityTable(availabilityTableDiv(), browserTimezoneOffset());
}

// Toggles a tile from green to white and vice versa when clicked.
function toggleTile(tile) {
  tile.classList.toggle('table-success');
}

function loadAvailabilityTable(tableDiv, timezoneOffset) {
  fetch(`/availabilityTable.jsp?timeZoneOffset=${timezoneOffset}`)
    .then(response => response.text())
    .then(tableContents => {
      tableDiv.innerHTML = tableContents;
    });
}

function browserTimezoneOffset() {
  let date = new Date();
  return (-1) * date.getTimezoneOffset();
}

function availabilityTableDiv() {
  return document.getElementById('table-container');
}

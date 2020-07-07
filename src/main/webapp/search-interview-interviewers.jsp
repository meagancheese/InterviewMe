<table class="table">
  <thead>
     <tr>
        <th scope="col">Company</th>
        <th scope="col">Job</th>
     </tr>
  </thead>
  <tbody>
     <tr>
       <!-- TODO: get this info from a servlet -->
        <td>Facebook</td>
        <td>Program Manager</td>
        <td>
          <button type="button" class="btn btn-primary" 
          data-company="Facebook" data-job="Program Manager" 
          data-email="fbpm@gmail.com" onclick="selectInterview(this)">
            Select
          </button>
        </td>
     </tr>
     <tr>
        <td>Google</td>
        <td>Software Engineer</td>
        <td> 
          <button type="button" class="btn btn-primary" data-company="Google" 
          data-job="Software Engineer" data-email="gswe@gmail.com" 
          onclick="selectInterview(this)">
            Select
          </button>
        </td>
     </tr>
  </tbody>
</table>

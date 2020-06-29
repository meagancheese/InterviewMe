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


/**
 * Represents a user on the site.
 * The email is used as the key to the user table.
 */
public class Person {
  private string email, name, company, job, linkedin;
   
  public Person(string email, string name, string company, string job, string linkedin) {
    this.email = email;
    this.name = name; 
    this.company = company;
    this.job = job;
    this.linkedin = linkedin; 
  }

  public string key() {
    return email; 
  }
}

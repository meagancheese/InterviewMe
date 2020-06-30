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

package com.google.sps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Accesses Datastore to support managing Person entities. */
public class DatastorePersonDao implements PersonDao {
  // @param datastore the DatastoreService we're using to interact with Datastore.
  private DatastoreService datastore;

  /** Initializes the fields for PersonDatastoreDAO. */
  public DatastorePersonDao() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  /* We make an entity in Datastore with person's fields as properties and their email as the key. */
  public void put(Person person) {
    datastore.put(personToEntity(person));
  }

  private static Entity personToEntity(Person person) {
    Entity personEntity = new Entity("Person", person.key());
    personEntity.setProperty("firstName", person.getFirstName());
    personEntity.setProperty("lastName", person.getLastName());
    personEntity.setProperty("company", person.getCompany());
    personEntity.setProperty("job", person.getJob());
    personEntity.setProperty("linkedIn", person.getLinkedIn());
    return personEntity;
  }

  /* Since email is a personEntity's key, we retrieve the corresponding personEntity from Datastore and return it as a Person object. */
  public Person get(String email)
      throws com.google.appengine.api.datastore.EntityNotFoundException {
    // TODO: test this with the profile page
    Key key = KeyFactory.createKey("Person", email);
    Entity personEntity = datastore.get(key);
    return entityToPerson(personEntity);
  }

  private static Person entityToPerson(Entity personEntity) {
    return new Person(
        (String) personEntity.getProperty("email"),
        (String) personEntity.getProperty("firstName"),
        (String) personEntity.getProperty("lastName"),
        (String) personEntity.getProperty("company"),
        (String) personEntity.getProperty("job"),
        (String) personEntity.getProperty("linkedIn"));
  }
}

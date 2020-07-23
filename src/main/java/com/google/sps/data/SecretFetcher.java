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

package com.google.sps.data;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

// Handles getting secrets we define in our GCP's SecretManager.
public class SecretFetcher {
  // The ID of the project secrets are being fetched from.
  private final String projectId;

  public SecretFetcher(String projectId) {
    this.projectId = projectId;
  }

  public String getSecretValue(String secretId) throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName name = SecretVersionName.of(projectId, secretId, "latest");
      AccessSecretVersionRequest request =
          AccessSecretVersionRequest.newBuilder().setName(name.toString()).build();
      AccessSecretVersionResponse response = client.accessSecretVersion(request);

      // WARNING: Do not print the secret in a production environment - this
      // snippet is showing how to access the secret material.
      String data = response.getPayload().getData().toStringUtf8();
      client.close();
      return data;
    }
  }
}

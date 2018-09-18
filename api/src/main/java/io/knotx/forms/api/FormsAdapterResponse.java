/*
 * Copyright (C) 2018 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.knotx.forms.api;

import com.google.common.base.Objects;
import io.knotx.dataobjects.ClientResponse;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

@DataObject(generateConverter = true)
public class FormsAdapterResponse {

  private ClientResponse response;

  private String signal;

  public FormsAdapterResponse() {
    //Empty Reponse object
  }


  public FormsAdapterResponse(JsonObject json) {
    FormsAdapterResponseConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    FormsAdapterResponseConverter.toJson(this, json);
    return json;
  }

  public ClientResponse getResponse() {
    return response;
  }

  public FormsAdapterResponse setResponse(ClientResponse response) {
    this.response = response;
    return this;
  }

  public String getSignal() {
    return signal;
  }

  public FormsAdapterResponse setSignal(String signal) {
    this.signal = signal;

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FormsAdapterResponse)) {
      return false;
    }
    FormsAdapterResponse that = (FormsAdapterResponse) o;
    return Objects.equal(signal, that.signal) &&
        response.equals(that.response);
  }

  @Override
  public int hashCode() {
    return 31 * response.hashCode() + signal.hashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("response", response)
        .append("signal", signal)
        .toString();
  }
}

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
import io.knotx.dataobjects.ClientRequest;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@DataObject(generateConverter = true)
public class FormsAdapterRequest {

  private ClientRequest request;

  private JsonObject params;

  private JsonObject adapterParams;

  public FormsAdapterRequest() {
    //Empty object
  }

  public FormsAdapterRequest(JsonObject json) {
    FormsAdapterRequestConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    FormsAdapterRequestConverter.toJson(this, json);
    return json;
  }

  /**
   * @return the client request object representing HTTP request
   */
  public ClientRequest getRequest() {
    return request;
  }

  /**
   * Set the client request
   *
   * @param request - ClientRequest object
   * @return a reference to this, so the API can be used fluently
   */
  public FormsAdapterRequest setRequest(ClientRequest request) {
    this.request = request;
    return this;
  }

  /**
   * @return the JsonObject with request params
   */
  public JsonObject getParams() {
    return params;
  }

  /**
   * Set the request params
   *
   * @param adapterParams - JsonObject consists of additional adapter parameters
   *                      that can be set in the form as data-knotx-adapter-params
   * @return a reference to this, so the API can be used fluently
   */
  public FormsAdapterRequest setAdapterParams(JsonObject adapterParams) {
    this.adapterParams = adapterParams;
    return this;
  }

  /**
   * @return the JsonObject with request params
   */
  public JsonObject getAdapterParams() {
    return adapterParams;
  }

  /**
   * Set the request params
   *
   * @param params - JsonObject consists of request params
   * @return a reference to this, so the API can be used fluently
   */
  public FormsAdapterRequest setParams(JsonObject params) {
    this.params = params;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FormsAdapterRequest)) {
      return false;
    }
    FormsAdapterRequest that = (FormsAdapterRequest) o;
    return Objects.equal(params, that.params) &&
        Objects.equal(adapterParams, that.adapterParams) &&
        request.equals(that.request);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(params)
        .append(request)
        .append(adapterParams)
        .build();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("request", request)
        .append("params", params)
        .append("adapterParams", adapterParams)
        .toString();
  }
}

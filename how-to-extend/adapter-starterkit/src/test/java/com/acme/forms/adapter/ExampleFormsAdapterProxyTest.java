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
package com.acme.forms.adapter;

import static io.knotx.junit5.util.RequestUtil.subscribeToResult_shouldSucceed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.knotx.forms.api.FormsAdapterRequest;
import io.knotx.forms.api.FormsAdapterResponse;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.reactivex.forms.api.FormsAdapterProxy;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class ExampleFormsAdapterProxyTest {

  private static final String KNOTX_FORMS_CUSTOM = "knotx.forms.custom";

  @Test
  @KnotxApplyConfiguration("application.conf")
  public void callCustomFormsAdapter_withCorrectPath(
      VertxTestContext context, Vertx vertx) {

    callWithAssertions(context, vertx, "/path/to/correct/service",
        response -> {
          assertEquals(0, getValidationErrors(response).size());
          assertEquals("ok", response.getSignal());
        });
  }

  @Test
  @KnotxApplyConfiguration("application.conf")
  public void callCustomFormsAdapter_withIncorrectPath(
      VertxTestContext context, Vertx vertx) {

    callWithAssertions(context, vertx, "/path/to/incorrect/service",
        response -> {
          assertEquals(1, getValidationErrors(response).size());
          assertEquals("error", response.getSignal());
        });
  }

  private void callWithAssertions(
      VertxTestContext context, Vertx vertx, String path,
      Consumer<FormsAdapterResponse> onSuccess) {
    FormsAdapterRequest request = prepareRequest(path);

    rxProcessWithAssertions(context, vertx, onSuccess, request);
  }


  private void rxProcessWithAssertions(VertxTestContext context, Vertx vertx,
      Consumer<FormsAdapterResponse> onSuccess, FormsAdapterRequest request) {
    FormsAdapterProxy service = FormsAdapterProxy.createProxy(vertx,
        KNOTX_FORMS_CUSTOM);
    Single<FormsAdapterResponse> adapterResponse = service.rxProcess(request);

    subscribeToResult_shouldSucceed(context, adapterResponse, onSuccess);
  }

  private FormsAdapterRequest prepareRequest(String path) {
    return new FormsAdapterRequest()
        .setParams(prepareParams(path));
  }

  private JsonObject prepareParams(String message) {
    return new JsonObject().put("path", message);
  }

  private JsonArray getValidationErrors(FormsAdapterResponse response) {
    return response.getResponse()
                   .getBody()
                   .toJsonObject()
                   .getJsonArray("validationErrors");
  }
}

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
package io.knotx.forms.http;

import static io.knotx.junit5.util.RequestUtil.subscribeToResult_shouldSucceed;

import io.knotx.forms.api.FormsAdapterRequest;
import io.knotx.forms.api.FormsAdapterResponse;
import io.knotx.dataobjects.ClientRequest;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.util.FileReader;
import io.knotx.reactivex.forms.api.FormsAdapterProxy;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
public class HttpFormsAdapterProxyTest {

  private final static String ADAPTER_ADDRESS = "knotx.forms.http";

  @Test
  @KnotxApplyConfiguration("knotx-datasource-http-test.json")
  public void callNonExistingService_expectBadRequestResponse(
      VertxTestContext context, Vertx vertx) {
    callAdapterServiceWithAssertions(context, vertx, "not/existing/service/address",
        adapterResponse -> {
          Assertions.assertEquals(adapterResponse.getResponse().getStatusCode(),
              HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        }
    );
  }

  @Test
  @KnotxApplyConfiguration("knotx-datasource-http-test.json")
  public void callExistingService_expectOKResponseWithServiceDataProvidedByService1(
      VertxTestContext context, Vertx vertx) throws Exception {
    final String expected = FileReader.readText("first-response.json");

    callAdapterServiceWithAssertions(context, vertx, "/service/mock/first.json",
        adapterResponse -> {
          Assertions.assertEquals(adapterResponse.getResponse().getStatusCode(),
              HttpResponseStatus.OK.code());

          JsonObject serviceResponse = new JsonObject(
              adapterResponse.getResponse().getBody().toString());
          JsonObject expectedResponse = new JsonObject(expected);

          Assertions.assertEquals(serviceResponse, expectedResponse);
        }
    );
  }

  private void callAdapterServiceWithAssertions(
      VertxTestContext context,
      Vertx vertx,
      String servicePath,
      Consumer<FormsAdapterResponse> onSuccess) {
    FormsAdapterRequest message = payloadMessage(servicePath);

    FormsAdapterProxy service = FormsAdapterProxy.createProxy(vertx, ADAPTER_ADDRESS);

    Single<FormsAdapterResponse> adapterResponseSingle = service.rxProcess(message);

    subscribeToResult_shouldSucceed(context, adapterResponseSingle, onSuccess);
  }

  private FormsAdapterRequest payloadMessage(String servicePath) {
    return new FormsAdapterRequest().setRequest(new ClientRequest())
        .setParams(new JsonObject().put("path", servicePath));
  }

}

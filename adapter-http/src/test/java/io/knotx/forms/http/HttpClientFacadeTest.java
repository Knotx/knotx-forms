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

import static io.knotx.junit5.util.RequestUtil.subscribeToResult_shouldFail;
import static io.knotx.junit5.util.RequestUtil.subscribeToResult_shouldSucceed;

import com.google.common.collect.Lists;
import io.knotx.dataobjects.ClientRequest;
import io.knotx.dataobjects.ClientResponse;
import io.knotx.forms.api.FormsAdapterRequest;
import io.knotx.forms.http.common.configuration.HttpFormsAdapterOptions;
import io.knotx.forms.http.common.configuration.HttpFormsSettings;
import io.knotx.forms.http.common.exception.UnsupportedFormsException;
import io.knotx.forms.http.common.http.HttpClientFacade;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.util.FileReader;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@ExtendWith(KnotxExtension.class)
public class HttpClientFacadeTest {

  // Configuration

  private static final Integer PORT = 3000;

  private static final String DOMAIN = "localhost";

  private static final String PATH = "/services/mock.*";

  // Request payload

  private static final String REQUEST_PATH = "/services/mock/first.json";

  private static final List<Pattern> PATTERNS = Collections
      .singletonList(Pattern.compile("X-test*"));


  @Test
  @KnotxApplyConfiguration("knotx-datasource-http-test.json")
  public void whenSupportedStaticPathServiceRequested_expectRequestExecutedAndResponseOKWithBody(
      VertxTestContext context, Vertx vertx) throws Exception {
    // given
    final WebClient mockedWebClient = Mockito.spy(webClient(vertx));
    HttpClientFacade clientFacade = new HttpClientFacade(mockedWebClient,
        getConfiguration());
    final JsonObject expectedResponse = new JsonObject(
        FileReader.readText("first-response.json"));

    // when
    Single<ClientResponse> result = clientFacade
        .process(payloadMessage(REQUEST_PATH, new ClientRequest()), HttpMethod.GET);

    // then
    subscribeToResult_shouldSucceed(context, result, response -> {
      Assertions.assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
      Assertions.assertEquals(expectedResponse, response.getBody().toJsonObject());
      Mockito.verify(mockedWebClient, Mockito.times(1))
          .request(HttpMethod.GET, PORT, DOMAIN, REQUEST_PATH);
    });
  }

  @Test
  @KnotxApplyConfiguration("knotx-datasource-http-test.json")
  public void whenSupportedDynamicPathServiceRequested_expectRequestExecutedAndResponseOKWithBody(
      VertxTestContext context, Vertx vertx) throws Exception {
    // given
    final WebClient mockedWebClient = Mockito.spy(webClient(vertx));
    HttpClientFacade clientFacade = new HttpClientFacade(mockedWebClient,
        getConfiguration());
    final JsonObject expectedResponse = new JsonObject(
        FileReader.readText("first-response.json"));
    final ClientRequest request = new ClientRequest()
        .setParams(MultiMap.caseInsensitiveMultiMap().add("dynamicValue", "first"));

    // when
    Single<ClientResponse> result =
        clientFacade.process(payloadMessage("/services/mock/{param.dynamicValue}.json", request),
            HttpMethod.GET);

    // then
    subscribeToResult_shouldSucceed(context, result, response -> {
      Assertions.assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
      Assertions.assertEquals(expectedResponse, response.getBody().toJsonObject());
      Mockito.verify(mockedWebClient, Mockito.times(1))
          .request(HttpMethod.GET, PORT, DOMAIN, REQUEST_PATH);
    });
  }

  @Test
  @KnotxApplyConfiguration("knotx-datasource-http-test.json")
  public void whenServiceRequestedWithoutPathParam_expectNoServiceRequestAndBadRequest(
      VertxTestContext context, Vertx vertx) {
    // given
    final WebClient mockedWebClient = Mockito.spy(webClient(vertx));
    HttpClientFacade clientFacade = new HttpClientFacade(mockedWebClient,
        getConfiguration());

    // when
    Single<ClientResponse> result = clientFacade
        .process(new FormsAdapterRequest(), HttpMethod.GET);

    // then
    subscribeToResult_shouldFail(context, result, error -> {
      Assertions.assertEquals(error.getClass().getSimpleName(),
          IllegalArgumentException.class.getSimpleName());
      Mockito.verify(mockedWebClient, Mockito.times(0))
          .request(ArgumentMatchers.any(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyString(),
              ArgumentMatchers.anyString());
    });
  }

  @Test
  @KnotxApplyConfiguration("knotx-datasource-http-test.json")
  public void whenUnsupportedPathServiceRequested_expectNoServiceRequestAndBadRequest(
      VertxTestContext context, Vertx vertx) {
    // given
    final WebClient mockedWebClient = Mockito.spy(webClient(vertx));
    HttpClientFacade clientFacade = new HttpClientFacade(mockedWebClient,
        getConfiguration());

    // when
    Single<ClientResponse> result =
        clientFacade
            .process(payloadMessage("/not/supported/path", new ClientRequest()), HttpMethod.GET);

    // then
    subscribeToResult_shouldFail(context, result, error -> {
      Assertions.assertEquals(UnsupportedFormsException.class, error.getClass());
      Mockito.verify(mockedWebClient, Mockito.times(0))
          .request(ArgumentMatchers.any(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyString(),
              ArgumentMatchers.anyString());
    });
  }

  @Test
  @KnotxApplyConfiguration("knotx-datasource-http-test.json")
  public void whenServiceEmptyResponse_expectNoFailure(
      VertxTestContext context, Vertx vertx) {
    // given
    final WebClient mockedWebClient = Mockito.spy(webClient(vertx));
    HttpClientFacade clientFacade = new HttpClientFacade(mockedWebClient,
        getConfiguration());

    // when
    Single<ClientResponse> result = clientFacade
        .process(payloadMessage("/services/mock/empty.json", new ClientRequest()), HttpMethod.GET);

    // then
    subscribeToResult_shouldSucceed(context, result, response -> {
      Assertions.assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
      Assertions.assertEquals((Integer) 0,
          Integer.valueOf(response.getHeaders().get("Content-Length")));
      Mockito.verify(mockedWebClient, Mockito.times(1))
          .request(HttpMethod.GET, PORT, DOMAIN, "/services/mock/empty.json");
    });
  }

  private WebClient webClient(Vertx vertx) {
    return WebClient.create(vertx);
  }

  private FormsAdapterRequest payloadMessage(String servicePath, ClientRequest request) {
    return new FormsAdapterRequest().setRequest(request)
        .setParams(new JsonObject().put("path", servicePath));
  }

  private HttpFormsAdapterOptions getConfiguration() {
    return new HttpFormsAdapterOptions().setServices(getServiceConfigurations());
  }

  private List<HttpFormsSettings> getServiceConfigurations() {
    return Lists.newArrayList(
        new HttpFormsSettings()
            .setPort(PORT)
            .setDomain(DOMAIN)
            .setPath(PATH)
            .setAllowedRequestHeaderPatterns(PATTERNS));
  }

}

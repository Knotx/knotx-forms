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
package io.knotx.forms.test.integration;

import static io.knotx.junit5.util.RequestUtil.subscribeToResult_shouldSucceed;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.collect.Lists;
import io.knotx.dataobjects.ClientResponse;
import io.knotx.dataobjects.Fragment;
import io.knotx.dataobjects.KnotContext;
import io.knotx.forms.api.FormsAdapterProxy;
import io.knotx.forms.api.FormsAdapterRequest;
import io.knotx.forms.api.FormsAdapterResponse;
import io.knotx.forms.test.integration.util.KnotContextFactory;
import io.knotx.http.MultiMapCollector;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.util.FileReader;
import io.knotx.reactivex.proxy.KnotProxy;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
public class FormsIntegrationTest {

  private static final String KNOT_TRANSITION = "next";
  private final static String ADDRESS = "knotx.knot.forms";
  private final static String HIDDEN_INPUT_TAG_NAME = "snippet-identifier";
  private static final String FRAGMENT_KNOTS = "data-knotx-knots";
  private final static String FRAGMENT_REDIRECT_IDENTIFIER = "someId123";
  private final static Fragment FIRST_FRAGMENT = Fragment.raw("<html><head></head><body>");
  private final static Fragment LAST_FRAGMENT = Fragment.raw("</body></html>");
  private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings()
      .escapeMode(Entities.EscapeMode.xhtml)
      .indentAmount(0)
      .prettyPrint(false);

  @Test
  @KnotxApplyConfiguration("formsStackTest.conf")
  public void callGetWithNoFormsFragments_expectResponseOkNoFragmentChanges(
      VertxTestContext context, Vertx vertx) throws Exception {
    String expectedTemplatingFragment = FileReader.readText("fragments/templating_out.html");
    KnotContext knotContext = createKnotContext(FIRST_FRAGMENT, LAST_FRAGMENT,
        "fragments/templating_in.html");
    knotContext.getClientRequest().setMethod(HttpMethod.GET);

    callFormsKnotWithAssertions(context, vertx, knotContext,
        clientResponse -> {
          assertEquals(HttpResponseStatus.OK.code(),
              clientResponse.getClientResponse().getStatusCode());
          assertNotNull(clientResponse.getTransition());
          assertEquals(KNOT_TRANSITION, clientResponse.getTransition());
          assertNotNull(clientResponse.getFragments());

          List<Fragment> fragments = clientResponse.getFragments();
          assertEquals(FIRST_FRAGMENT.content(), fragments.get(0).content());
          assertEquals(expectedTemplatingFragment, fragments.get(1).content());
          assertEquals(LAST_FRAGMENT.content(), fragments.get(2).content());
        });
  }

  @Test
  @KnotxApplyConfiguration("formsStackTest.conf")
  public void callGetWithTwoFormsFragments_expectResponseOkTwoFragmentChanges(
      VertxTestContext context, Vertx vertx) throws Exception {
    String expectedRedirectFormFragment = FileReader.readText("fragments/form_redirect_out.html");
    String expectedSelfFormFragment = FileReader.readText("fragments/form_self_out.html");
    KnotContext knotContext = createKnotContext(FIRST_FRAGMENT, LAST_FRAGMENT,
        "fragments/form_redirect_in.html", "fragments/form_self_in.html");
    knotContext.getClientRequest().setMethod(HttpMethod.GET);

    callFormsKnotWithAssertions(context, vertx, knotContext,
        clientResponse -> {
          assertEquals(HttpResponseStatus.OK.code(),
              clientResponse.getClientResponse().getStatusCode());
          assertNotNull(clientResponse.getTransition());
          assertEquals(KNOT_TRANSITION, clientResponse.getTransition());
          assertNotNull(clientResponse.getFragments());

          List<Fragment> fragments = clientResponse.getFragments();
          assertEquals(FIRST_FRAGMENT.content(), fragments.get(0).content());
          assertEquals(clean(expectedRedirectFormFragment), clean(fragments.get(1).content()));
          assertEquals(clean(expectedSelfFormFragment), clean(fragments.get(2).content()));
          assertEquals(LAST_FRAGMENT.content(), fragments.get(3).content());
        });
  }

  @Test
  @KnotxApplyConfiguration("formsStackTest.conf")
  public void callGetWithFormsFragmentWithoutIdentifier_expectResponseOkWithOneFragmentChanges(
      VertxTestContext context, Vertx vertx) throws Exception {
    KnotContext knotContext = createKnotContext("fragments/form_no_identifier_in.html");
    String expectedFragmentHtml = FileReader.readText("fragments/form_no_identifier_out.html");
    knotContext.getClientRequest().setMethod(HttpMethod.GET);

    callFormsKnotWithAssertions(context, vertx, knotContext,
        clientResponse -> {
          assertEquals(HttpResponseStatus.OK.code(),
              clientResponse.getClientResponse().getStatusCode());
          assertNotNull(clientResponse.getTransition());
          assertEquals(KNOT_TRANSITION, clientResponse.getTransition());
          assertNotNull(clientResponse.getFragments());

          List<Fragment> fragments = clientResponse.getFragments();
          assertEquals(fragments.size(), 1);
          assertEquals(clean(expectedFragmentHtml), clean(fragments.get(0).content()));
        });
  }

  @Test
  @KnotxApplyConfiguration("formsStackTest.conf")
  public void callGetWithFormsFragmentFormsHandlerNotExists_expectStatusCode500(
      VertxTestContext context, Vertx vertx) throws Exception {
    KnotContext knotContext = createKnotContext("fragments/form_forms_handler_not_exists_in.html");
    knotContext.getClientRequest().setMethod(HttpMethod.GET);

    callFormsKnotWithAssertions(context, vertx, knotContext,
        clientResponse -> {
          assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
              clientResponse.getClientResponse().getStatusCode());
          assertNull(clientResponse.getTransition());
          assertNull(clientResponse.getFragments());
        });
  }

  @Test
  @KnotxApplyConfiguration("formsStackTest.conf")
  public void callPostWithTwoFormsFragments_expectResponseOkWithTransitionStep2(
      VertxTestContext context, Vertx vertx) throws Exception {
    createMockAdapter(vertx, "address-redirect", "", "step2");
    KnotContext knotContext = createKnotContext("fragments/form_redirect_in.html",
        "fragments/form_self_in.html");
    knotContext.getClientRequest()
        .setMethod(HttpMethod.POST)
        .setFormAttributes(MultiMap.caseInsensitiveMultiMap()
            .add(HIDDEN_INPUT_TAG_NAME, FRAGMENT_REDIRECT_IDENTIFIER));

    callFormsKnotWithAssertions(context, vertx, knotContext,
        clientResponse -> {
          assertEquals(HttpResponseStatus.MOVED_PERMANENTLY.code(),
              clientResponse.getClientResponse().getStatusCode());
          assertEquals("/content/form/step2.html",
              clientResponse.getClientResponse().getHeaders().get("Location"));
          assertNull(clientResponse.getTransition());
          assertNull(clientResponse.getFragments());
        });
  }

  @Test
  @KnotxApplyConfiguration("formsStackTest.conf")
  public void callPostWithFormsFragmentWithoutRequestedFragmentIdentifier_expectStatusCode500(
      VertxTestContext context, Vertx vertx) throws Exception {
    KnotContext knotContext = createKnotContext("fragments/form_incorrect_identifier_in.html");
    knotContext.getClientRequest().setMethod(HttpMethod.POST);

    callFormsKnotWithAssertions(context, vertx, knotContext,
        clientResponse -> {
          assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
              clientResponse.getClientResponse().getStatusCode());
          assertNull(clientResponse.getFragments());
          assertNull(clientResponse.getTransition());
        });
  }

  @Test
  @KnotxApplyConfiguration("formsStackTest.conf")
  public void callPostWithFormsFragmentWithIncorrectSnippetId_expectStatusCode500(
      VertxTestContext context, Vertx vertx) throws Exception {
    KnotContext knotContext = createKnotContext("fragments/form_redirect_in.html");
    knotContext.getClientRequest().setMethod(HttpMethod.POST)
        .setFormAttributes(
            MultiMap.caseInsensitiveMultiMap().add(HIDDEN_INPUT_TAG_NAME, "snippet_id_not_exists"));

    callFormsKnotWithAssertions(context, vertx, knotContext,
        clientResponse -> {
          assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
              clientResponse.getClientResponse().getStatusCode());
          assertNull(clientResponse.getFragments());
          assertNull(clientResponse.getTransition());
        });
  }

  private void callFormsKnotWithAssertions(
      VertxTestContext context, Vertx vertx, KnotContext knotContext,
      Consumer<KnotContext> onSuccess) {
    KnotProxy formsKnot = KnotProxy.createProxy(vertx, ADDRESS);

    Single<KnotContext> knotContextSingle = formsKnot.rxProcess(knotContext);

    subscribeToResult_shouldSucceed(context, knotContextSingle, onSuccess);
  }

  private KnotContext createKnotContext(String... snippetFilenames) throws Exception {
    return createKnotContext(null, null, snippetFilenames);
  }

  private KnotContext createKnotContext(Fragment firstFragment, Fragment lastFragment,
      String... snippetFilenames) throws Exception {
    List<Fragment> fragments = Lists.newArrayList();
    Optional.ofNullable(firstFragment).ifPresent(fragments::add);
    for (String file : snippetFilenames) {
      String fileContent = FileReader.readText(file);
      String fragmentIdentifiers = Jsoup.parse(fileContent).getElementsByAttribute(FRAGMENT_KNOTS)
          .attr(
              FRAGMENT_KNOTS);
      fragments.add(Fragment.snippet(Arrays.asList(fragmentIdentifiers.split(",")), fileContent));
    }
    Optional.ofNullable(lastFragment).ifPresent(fragments::add);

    return KnotContextFactory.empty(fragments);
  }

  private String clean(String text) {
    String cleanText = text.replace("\n", "").replaceAll(">(\\s)+<", "><")
        .replaceAll(">(\\s)+\\{", ">{").replaceAll("\\}(\\s)+<", "}<");
    return Jsoup.parse(cleanText, "UTF-8", Parser.xmlParser())
        .outputSettings(OUTPUT_SETTINGS)
        .html()
        .trim();
  }

  private void createMockAdapter(Vertx vertx, String address, String addToBody, String signal) {
    createMockAdapter(vertx, address, addToBody, signal, Collections.emptyMap());
  }

  private void createMockAdapter(Vertx vertx, String address, String addToBody, String signal,
      Map<String, List<String>> headers) {
    Function<FormsAdapterRequest, FormsAdapterResponse> adapter = adapterRequest -> {
      ClientResponse response = new ClientResponse();
      response.setStatusCode(HttpResponseStatus.OK.code());
      response.setBody(Buffer.buffer().appendString(addToBody));
      response.setHeaders(
          headers.keySet().stream().collect(MultiMapCollector.toMultiMap(o -> o, headers::get)));
      return new FormsAdapterResponse().setResponse(response).setSignal(signal);
    };

    new ServiceBinder(vertx.getDelegate())
        .setAddress(address)
        .register(FormsAdapterProxy.class, new MockAdapterImpl(adapter));
  }

  private class MockAdapterImpl implements FormsAdapterProxy {

    private final Function<FormsAdapterRequest, FormsAdapterResponse> adapter;

    private MockAdapterImpl(Function<FormsAdapterRequest, FormsAdapterResponse> adapter) {
      this.adapter = adapter;
    }

    @Override
    public void process(FormsAdapterRequest request,
        Handler<AsyncResult<FormsAdapterResponse>> result) {
      result.handle(Future.succeededFuture(adapter.apply(request)));
    }
  }

}

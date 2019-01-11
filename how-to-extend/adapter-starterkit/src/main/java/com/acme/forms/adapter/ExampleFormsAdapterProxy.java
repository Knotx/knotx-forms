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

import io.knotx.dataobjects.ClientResponse;
import io.knotx.forms.api.FormsAdapterRequest;
import io.knotx.forms.api.FormsAdapterResponse;
import io.knotx.forms.api.reactivex.AbstractFormsAdapterProxy;
import io.reactivex.Single;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ExampleFormsAdapterProxy extends AbstractFormsAdapterProxy {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleFormsAdapterProxy.class);
  private static final String CORRECT_PATH = "/path/to/correct/service";
  @Override
  protected Single<FormsAdapterResponse> processRequest(FormsAdapterRequest request) {
    final String path = request.getParams()
                                  .getString("path");
    LOGGER.info("Processing request with path: `{}`", path);
    return prepareResponse(path);
  }

  private Single<FormsAdapterResponse> prepareResponse(String path) {
    if(CORRECT_PATH.equals(path)){
      return Single.just(prepareFormsAdapterResponse(correctJsonBody(), "ok"));
    }

    return Single.just(prepareFormsAdapterResponse(incorrectJsonBody(), "error"));
  }

  private FormsAdapterResponse prepareFormsAdapterResponse(String body, String signal){
    final FormsAdapterResponse response = new FormsAdapterResponse();
    final ClientResponse clientResponse = new ClientResponse();
    clientResponse.setBody(Buffer.buffer(body));
    response.setResponse(clientResponse);
    response.setSignal(signal);
    return response;
  }

  private String correctJsonBody(){
    return "{\"validationErrors\":[]}";
  }

  private String incorrectJsonBody(){
    return "{\"validationErrors\":[{\"message\":\"Not supported path\"}]}";
  }
}

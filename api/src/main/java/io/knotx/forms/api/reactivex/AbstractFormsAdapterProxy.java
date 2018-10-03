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
package io.knotx.forms.api.reactivex;

import io.knotx.forms.api.FormsAdapterProxy;
import io.knotx.forms.api.FormsAdapterRequest;
import io.knotx.forms.api.FormsAdapterResponse;
import io.knotx.dataobjects.ClientResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public abstract class AbstractFormsAdapterProxy implements FormsAdapterProxy {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFormsAdapterProxy.class);

  protected abstract Single<FormsAdapterResponse> processRequest(FormsAdapterRequest message);

  public void process(FormsAdapterRequest request, Handler<AsyncResult<FormsAdapterResponse>> result) {
    processRequest(request)
        .subscribe(
            adapterResponse -> result.handle(Future.succeededFuture(adapterResponse)),
            error -> {
              LOGGER.error("Error happened during Adapter Request processing", error);
              result.handle(Future.succeededFuture(getErrorResponse(error)));
            }
        );
  }

  /**
   * Method generates error {@link FormsAdapterResponse} in case of processing failure.
   *
   * @param error - error that occurred.
   * @return - error response (e.g. with 500 status code and other info).
   */
  protected FormsAdapterResponse getErrorResponse(Throwable error) {
    return new FormsAdapterResponse().setResponse(new ClientResponse()
        .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
        .setBody(Buffer.buffer(error.getMessage())));
  }
}

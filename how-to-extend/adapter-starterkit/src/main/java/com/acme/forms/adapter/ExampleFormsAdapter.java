package com.acme.forms.adapter;

import io.knotx.forms.api.FormsAdapterProxy;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleFormsAdapter extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleFormsAdapter.class);

  private MessageConsumer<JsonObject> consumer;

  private ExampleFormsAdapterOptions configuration;

  private ServiceBinder serviceBinder;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    configuration = new ExampleFormsAdapterOptions(config());
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("Starting <{}>", this.getClass()
                                     .getSimpleName());

    //register the service proxy on event bus
    serviceBinder = new ServiceBinder(getVertx());
    consumer = serviceBinder
        .setAddress(configuration.getAddress())
        .register(FormsAdapterProxy.class, new ExampleFormsAdapterProxy());
  }

  @Override
  public void stop() throws Exception {
    serviceBinder.unregister(consumer);
  }
}

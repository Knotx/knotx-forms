package com.acme.forms.adapter;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true, publicConverter = false)
public class ExampleFormsAdapterOptions {

  private String address;

  public ExampleFormsAdapterOptions(JsonObject config) {
    ExampleFormsOptionsConverter.fromJson(config, this);
  }

  public String getAddress() {
    return address;
  }

  public ExampleFormsAdapterOptions setAddress(String address) {
    this.address = address;
    return this;
  }

  /**
   * Convert to JSON
   *
   * @return the JSON
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    ExampleFormsOptionsConverter.toJson(this, json);
    return json;
  }
}

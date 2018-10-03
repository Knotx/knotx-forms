/*
 * Copyright (C) 2016 Cognifide Limited
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
package io.knotx.forms.core;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Describes an Action Knot configuration options
 */
@DataObject(generateConverter = true, publicConverter = false)
public class FormsKnotOptions {

  /**
   * Default EB address of the verticle
   */
  public final static String DEFAULT_ADDRESS = "knotx.knot.action";

  private String address;
  private List<FormsDefinition> adapters;
  private String formIdentifierName;
  private DeliveryOptions deliveryOptions;

  /**
   * Default constructor
   */
  public FormsKnotOptions() {
    init();
  }

  /**
   * Copy constructor
   *
   * @param other the instance to copy
   */
  public FormsKnotOptions(FormsKnotOptions other) {
    this.address = other.address;
    this.adapters = new ArrayList<>(other.adapters);
    this.formIdentifierName = other.formIdentifierName;
    this.deliveryOptions = new DeliveryOptions(other.deliveryOptions);
  }

  /**
   * Create an settings from JSON
   *
   * @param json the JSON
   */
  public FormsKnotOptions(JsonObject json) {
    init();
    FormsKnotOptionsConverter.fromJson(json, this);
  }

  /**
   * Convert to JSON
   *
   * @return the JSON
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    FormsKnotOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    address = DEFAULT_ADDRESS;
    adapters = new ArrayList<>();
    formIdentifierName = StringUtils.EMPTY;
    deliveryOptions = new DeliveryOptions();
  }

  /**
   * @return EB address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Sets the EB address of the verticle
   *
   * @param address EB address of the verticle
   * @return a reference to this, so the API can be used fluently
   */
  public FormsKnotOptions setAddress(String address) {
    this.address = address;
    return this;
  }

  /**
   * @return list of {@link FormsDefinition}
   */
  public List<FormsDefinition> getAdapters() {
    return adapters;
  }

  /**
   * Sets the adapters that will be responsible for communicating with external services in order to
   * process the request.
   *
   * @param adapters of {@link FormsDefinition} objects representing service
   * @return a reference to this, so the API can be used fluently
   */
  public FormsKnotOptions setAdapters(List<FormsDefinition> adapters) {
    this.adapters = adapters;
    return this;
  }

  /**
   * @return EB {@link DeliveryOptions}
   */
  public DeliveryOptions getDeliveryOptions() {
    return deliveryOptions;
  }

  /**
   * Sets the Vert.x Event Bus Delivery Options
   *
   * @param deliveryOptions EB {@link DeliveryOptions}
   * @return a reference to this, so the API can be used fluently
   */
  public FormsKnotOptions setDeliveryOptions(
      DeliveryOptions deliveryOptions) {
    this.deliveryOptions = deliveryOptions;
    return this;
  }

  public String getFormIdentifierName() {
    return formIdentifierName;
  }

  /**
   * Sets the name of the hidden input tag which is added by Action Knot.
   *
   * @param formIdentifierName the form identifier name
   */
  public FormsKnotOptions setFormIdentifierName(String formIdentifierName) {
    this.formIdentifierName = formIdentifierName;
    return this;
  }
}

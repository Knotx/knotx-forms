# Forms Knot
Forms Knot is an Knot implementation responsible for forms submissions handling. It supports
simple (without file upload) forms including redirection to successful pages and multi-step forms flows.
It provides also a service error handling mechanism.

## How does it work?
Forms Knot is used with default Knot.x settings while both GET and POST client request processing.
It transforms a form template to Knot.x agnostic one for GET requests. When client submits the form
Forms Knot calls configured Adapter and based on its response redirect the client to a
successful / error / next step page.

Current implementation of Forms module enables to use only a single `<form>` within one Fragment (snippet).
However, you may have as many snippets with forms as you want in a single page template.

Let's describe Forms Knot behaviour with following example.

### Example
FormsKnot processes Fragments having `form-{NAME}` in `data-knotx-knots` attribute,
where `{NAME}` is a unique name of a form (assuming there may be more than one form on a single page
it is used to distinguish a requested snippet). {NAME} can contain only small and capital letters. So
Knot Election Rule for Forms Knot is pattern `form-[a-zA-Z]`.

The client opens a `/content/local/login/step1.html` page. The final form markup returned by Knot.x looks like:

```html
<form method="post">
  <input name="_frmId" value="1" type="hidden">
  <input name="email" value="" type="email">
  <input value="Submit" type="submit">
 </form><p>Please provide your email address</p>

 <div>
  <strong>Pro tip: All emails that starts with <kbd>john.doe</kbd> will be accepted.</strong>
 </div>
```

There are no Knot.x specific attributes in a final markup besides one **hidden input tag**.

This is how form looks in the repository:

```html
<script data-knotx-knots="form-1" type="text/knotx-snippet">
  {{#if action._result.validationErrors}}
  <p class="bg-danger">Email address does not exists</p>
  {{/if}}
  <p>Please provide your email address</p>
  <form data-knotx-forms-adapter-name="step1" data-knotx-forms-on-success="/content/local/login/step2.html" data-knotx-forms-on-error="_self" data-knotx-forms-adapter-name-params='{"myKey":"myValue"}' method="post">
    <input type="email" name="email" value="{{#if action._result.validationError}} {{action._result.form.email}} {{/if}}" />
    <input type="submit" value="Submit"/>
  </form>
  <div>
    <strong>Pro tip: All emails that starts with <kbd>john.doe</kbd> will be accepted.</strong>
  </div>
</script>
```

Now we can explain how and why this additional hidden input `_frmId` with a value `1` appears . It
is automatically added by Forms Knot and is used to distinguish a requested form during submission process
(there could be more than one form at the same template). Its value comes from a script's `data-knotx-knots`
attribute - it retrieve a `{NAME}` value from `data-knotx-knots="form-{NAME}"`.

Following data attributes are available in the `<form>` tag with described purpose:
- `data-knotx-forms-adapter-name` - this is a name of an Forms Adapter that will be used to handle submitted data.
It is similar concept as `data-knotx-databridge-{NAME}` in [Data Bridge](https://github.com/Knotx/knotx-data-bridge). In the example,
Forms Handler registered under name `step1` will handle this form data submission.
- `data-knotx-forms-on-{SIGNAL}` - name of a [Signal](#Signal) that should be applied. In the example
there is one signal success with the value `'/content/local/login/step2.html'` and one signal error
with the value `'_self'`. Signal `'_self'` means that after error response (error signal returned)
the client will stay on the same page.
- `data-knotx-forms-adapter-params` - JSON Object that can be passed to the corresponding `Adapter`. It will be
available in `AdapterRequest` as `adapterParams`. 


### Signal
Signal is basically a decision about further request processing. Value of the signal can be either:
- `path` of a page that user should be redirected to after processing form submit,
- `_self` - that indicates that there will not be redirect, instead current page will be processed (generated view for instance).
In other words, the page processing will be delegated to next Knot in the graph.

## How to configure?
For all configuration fields and their defaults consult FormsKnotOptions. 
In folder `conf` there are also example configuration files for FormsKnot.

In short, by default, server:
- Listens on event bus address `knotx.knot.forms` on messages to process
- It communicates with the Forms Adapter on event bus address `test` for processing POST requests to the services
  - It pass the example parameter to the adapter
  - It pass `Cookie` request header to the adapter
  - It returns `Set-Cookie` response header from adapter
- It uses `snippet-identifier` value as hidden field name that's used by Forms Knot to identify form that sent POST request

### Vert.x Event Bus delivery options

While HTTP request processing, Forms Knot calls Adapter using 
[Vert.x Event Bus](http://vertx.io/docs/apidocs/io/vertx/core/eventbus/EventBus.html). The `config` field can contain 
[Vert.x Delivery Options](http://vertx.io/docs/apidocs/io/vertx/core/eventbus/DeliveryOptions.html) related to the event 
bus. It can be used to control the low level aspects of the event bus communication like timeouts, headers, message 
codec names.

The `deliveryOptions` need to be added in the following place, of the Forms Knot configuration (`includes/actionKnot.conf`) to define the 
timeout for the Adapter response.
```hocon
deliveryOptions.timeout: 15000
```

# Forms Adapter
Forms Adapter is Component of a system, that mediates communication between Knot.x Forms Knot
and external Services that are responsible for handling form submissions.


## How does it work?
Forms Adapter accepts message with the following data:

- `clientRequest` - object with all data of an original request,
- `params` - JSON object that contains additional parameters, among those parameter mandatory 
[`path`](#service-path) 

#### Service path
`path` parameter is a mandatory parameter that must be passed to Forms Adapter. 
It defines request path.

### Adapter Response
Result generated by Forms Adapter must be a JsonObject with the fields as below:
- `clientResponse` - Json Object, `body` field of this response is suppose to carry on the actual 
response from the mocked service,
- `signal` - string that defines how original request processing should be handled.

Find out more about contract described above in Forms Knot.

## How to configure?
Configuration of Forms Adapter is specific to its implementation. You may see example configuration 
of Http Service Adapter.

## How to implement your own Forms Adapter?
Implementing Forms Adapter that meets your project requirements allows 
you to adopt request from Knot.x into request understandable by an endpoint service, and adopts 
responses from that service into unified message understandable by Knot.x.

Please check the [Adapter Starterkit](https://github.com/Knotx/knotx-forms/how-to-extend/adapter-starterkit) as a starting point for your implementation.

For you convenience, Knot.x is shipped with a example [Forms Adapter](https://github.com/Knotx/knotx-example-project/tree/master/acme-forms-adapter-http).

Writing custom Forms Adapter requires fulfilling Forms Knot.

| ! Note |
|:------ |
| Besides Verticle implementation itself, a custom implementation of your Forms Adapter must be build as Knot.x module in order to be deployed as part of Knot.x. Follow the [Knot.x Modules](https://github.com/Cognifide/knotx/wiki/KnotxModules) in order to see how to make your Forms Adapter a module. | 

## Building
To build this module use gradle with following settings:
```gradle
 org.gradle.daemon=false
 org.gradle.parallel=false
```
This is temporary and will be changed as soon as tests that runs on the same ports will be fixed.

## Community
Knot.x gives one communication channel that is described [here](https://github.com/Cognifide/knotx#community).

## Bugs
All feature requests and bugs can be filed as issues on [Gitub](https://github.com/Knotx/knotx-data-bridge/issues).
Do not use Github issues to ask questions, post them on the [User Group](https://groups.google.com/forum/#!forum/knotx) or [Gitter Chat](https://gitter.im/Knotx/Lobby).

## Licence
**Knot.x modules** are licensed under the [Apache License, Version 2.0 (the "License")](https://www.apache.org/licenses/LICENSE-2.0.txt)

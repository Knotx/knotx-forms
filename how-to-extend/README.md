# How to extend
You can find here separate modules with basic code example of extending _Knot.x Forms_

## Adapter starterkit

Module with very basic implementation of Forms adapter.

 - `ExampleFormsAdapter` [_Verticle_](http://vertx.io/docs/apidocs/io/vertx/core/Verticle.html) implementation to integrate with _Knot.x_
 - `ExampleFormsAdapterProxy` Proxy to handle incoming request
 - `ExampleFormsAdapterOptions` Java model to read the configuration
 - `ExampleFormsAdapterProxyTest` Test class for testing contract between _Knot.x Forms_ and adapter implementation
 
For example implementation of Form Adapter please check [Knot.x example project](https://github.com/Knotx/knotx-example-project/tree/master/acme-forms-adapter-http) 

# Telemetry

Make your app talk…and talk…and talk. Inspired by Google's [Dapper](http://research.google.com/pubs/pub36356.html).

Here is a sample trace view generated by the
[TracingBundle](telemetry-dropwizard/src/main/java/com/yammer/telemetry/dropwizard/TracingBundle.java) of the
execution of the [TracedResource](telemetry-example/src/main/java/com/yammer/telemetry/example/resources/TracedResource.java) in the telemetry-example application.

![Sample Span](/telemetry-service/screenshot.png "Sample Span View")

## IDs

Trace and span IDs will be expressed as [UUIDs](https://en.wikipedia.org/wiki/Universally_unique_identifier).

## Trace/Span Passing

In order to trace across hosts trace ID and span ID information must be passed from one host to the next.

### Over HTTP

Clients will pass the current trace ID and the current span ID to downstream services via HTTP request headers:

    X-Telemetry-TraceId: {current trace ID}
    X-Telemetry-Parent-SpanId: {current span ID}

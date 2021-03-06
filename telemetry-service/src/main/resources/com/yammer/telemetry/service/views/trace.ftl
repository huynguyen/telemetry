<#-- @ftlvariable name="" type="com.yammer.telemetry.service.views.TraceView" -->
<html>
<head>
    <title>Trace - ${trace.root.name}</title>
    <style>
        .trace {
            width: 100%;
        }

        .span {
            position: relative;
            border: 1px solid #ff1493;
            background: #ffc0cb;
        }

        .annotationMarker {
            position: relative;
            border: 1px solid #000000;
            background: #444444;
            width: 1px;
        }
    </style>
    <script src="/webjars/jquery/1.10.2/jquery.js"></script>
    <script src="/webjars/jquery-ui/1.10.2/ui/jquery-ui.js"></script>
    <script>
        $(function() {
            $(document).tooltip();
        })
    </script>
</head>
<body>
<h1>Trace - ${trace.root.name}</h1>
<div id="trace-${trace.id}" class="trace">
    (start = ${trace.startTimeNanos} ; duration = ${trace.duration})
    <@renderSpan span=trace.root trace=trace/>
</div>
</body>
</html>

<#macro renderSpan span trace>
<#-- @ftlvariable name="span" type="com.yammer.telemetry.tracing.Span" -->
<#-- @ftlvariable name="trace" type="com.yammer.telemetry.tracing.Trace" -->
<div id="span-${span.id}" class="span" style="left: ${((span.startTimeNanos - trace.startTimeNanos) / trace.duration) * 100}%; width: ${(span.duration / trace.duration) * 100}%;">
    <#list trace.getAnnotations(span) as annotation>
    <div class="annotationMarker" style="left: ${((annotation.startTimeNanos - span.startTimeNanos) / span.duration) * 100}%;" title="${annotation.name} - ${annotation.message!"null"} @ ${annotation.startTimeNanos}">&nbsp;</div>
    </#list>
    ${span.name} (start = ${span.startTimeNanos}; duration = ${span.duration})
</div>
<#list trace.getChildren(span) as child>
    <@renderSpan span=child trace=trace/>
</#list>
</#macro>
package com.yammer.telemetry.agent.handlers;

import com.google.common.io.Resources;
import javassist.*;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class ApacheHttpClientClassHandler implements ClassInstrumentationHandler {
    private static final Logger LOGGER = Logger.getLogger(ApacheHttpClientClassHandler.class.getName());
    private boolean enabled = true;

    @Override
    public boolean transformed(CtClass cc, ClassPool pool) {
        if (!enabled) {
            // Handler was disabled earlier for reasons.
            return false;
        }

        try {
            if (cc.subtypeOf(pool.get("org.apache.http.client.HttpClient"))) {
                boolean transformedOneMethod = false;

                for (CtMethod method : cc.getDeclaredMethods()) {
                    if ("execute".equals(method.getName()) && !Modifier.isAbstract(method.getModifiers())) {
                        String hostParameter = "null";
                        String requestParameter = "null";
                        String requestUriParameter = "null";
                        String handlerParameter = "null";
                        CtClass[] parameterTypes = method.getParameterTypes();
                        for (int i = 0; i < parameterTypes.length; i++) {
                            if (parameterTypes[i].subtypeOf(pool.get("org.apache.http.HttpHost"))) {
                                hostParameter = "$" + (i + 1);
                            } else if (parameterTypes[i].subtypeOf(pool.get("org.apache.http.client.methods.HttpUriRequest"))) {
                                requestParameter = "$" + (i + 1);
                                requestUriParameter = "$" + (i + 1);
                            } else if (parameterTypes[i].subtypeOf(pool.get("org.apache.http.HttpRequest"))) {
                                requestParameter = "$" + (i + 1);
                            }
                        }

                        // Copy the method to a uniquely named location that won't conflict with anything.
                        CtMethod copiedMethod = CtNewMethod.copy(method, cc.makeUniqueName("execute"), cc, null);
                        copiedMethod.setModifiers(javassist.Modifier.PRIVATE);
                        cc.addMethod(copiedMethod);

                        final String source = Resources.toString(Resources.getResource(getClass(), "HttpClient_execute.javassist"), Charset.forName("utf-8"));
                        pool.importPackage("java.net");
                        pool.importPackage("org.apache.http");
                        pool.importPackage("org.apache.http.client.methods");
                        pool.importPackage("com.yammer.telemetry.tracing");
                        pool.importPackage("com.yammer.telemetry.agent.handlers");
                        String body = source.replace("%HTTP_HOST_PARAM%", hostParameter)
                                            .replace("%HTTP_REQUEST_PARAM%", requestParameter)
                                            .replace("%HTTP_REQUEST_URI_PARAM%", requestUriParameter)
                                            .replace("%RESPONSE_HANDLER_PARAM%", handlerParameter);
                        method.setBody(body, "this", copiedMethod.getName());

                        transformedOneMethod = true;
                    }
                }

                return transformedOneMethod;
            }
        } catch (NotFoundException | CannotCompileException | IOException e) {
            // Can't find the servlet classes…maybe this isn't a web application.
            // Or maybe we couldn't copy the method or compile some expressions, which is un-awesome.
            // Or maybe we couldn't load the source template from the JAR file. This is also un-awesome.
            // Disable the handler for the remainder.
            LOGGER.warning("Error instrumenting Apache HttpClient: " + e.toString());
            enabled = false;
        }

        return false;
    }
}

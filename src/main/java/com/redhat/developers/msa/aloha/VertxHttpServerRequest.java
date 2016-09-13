/*
 * Copyright 2016 Juraci Paixão Kröhling
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.developers.msa.aloha;

import java.net.URI;
import java.net.URISyntaxException;

import com.github.kristofa.brave.http.HttpServerRequest;

/**
 * @author Juraci Paixão Kröhling
 */
public class VertxHttpServerRequest implements HttpServerRequest {
    private io.vertx.core.http.HttpServerRequest backingRequest;

    public VertxHttpServerRequest(io.vertx.core.http.HttpServerRequest request) {
        this.backingRequest = request;
    }

    @Override public String getHttpHeaderValue(String headerName) {
        return backingRequest.headers().get(headerName);
    }

    @Override public URI getUri() {
        try {
            return new URI(backingRequest.uri());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override public String getHttpMethod() {
        return backingRequest.method().name();
    }
}

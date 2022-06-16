/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.reactive.function.client;

import java.net.URI;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.transport.http.context.HttpClientContext;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WebClientKeyValuesProvider}.
 *
 * @author Brian Clozel
 */
class WebClientKeyValuesProviderTests {

	private WebClientKeyValuesProvider keyValuesProvider = new WebClientKeyValuesProvider();

	@Test
	void shouldOnlySupportHttpClientContext() {
		assertThat(this.keyValuesProvider.supportsContext(new HttpClientContext())).isTrue();
		assertThat(this.keyValuesProvider.supportsContext(new Observation.Context())).isFalse();
	}

	@Test
	void shouldAddKeyValuesForNullExchange() {
		HttpClientContext context = new HttpClientContext();
		assertThat(this.keyValuesProvider.getLowCardinalityKeyValues(context)).hasSize(5)
				.contains(KeyValue.of("method", "None"), KeyValue.of("uri.template", "None"), KeyValue.of("status", "CLIENT_ERROR"),
						KeyValue.of("exception", "None"), KeyValue.of("outcome", "UNKNOWN"));
		assertThat(this.keyValuesProvider.getHighCardinalityKeyValues(context)).hasSize(2)
				.contains(KeyValue.of("clientName", "None"), KeyValue.of("uri", ""));
	}

	@Test
	void shouldAddKeyValuesForExchangeWithException() {
		HttpClientContext context = new HttpClientContext();
		context.setError(new IllegalStateException("Could not create client request"));
		assertThat(this.keyValuesProvider.getLowCardinalityKeyValues(context)).hasSize(5)
				.contains(KeyValue.of("method", "None"), KeyValue.of("uri.template", "None"), KeyValue.of("status", "CLIENT_ERROR"),
						KeyValue.of("exception", "IllegalStateException"), KeyValue.of("outcome", "UNKNOWN"));
		assertThat(this.keyValuesProvider.getHighCardinalityKeyValues(context)).hasSize(2)
				.contains(KeyValue.of("clientName", "None"), KeyValue.of("uri", ""));
	}

	@Test
	void shouldAddKeyValuesForRequestWithUriTemplate() {
		ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/resource/42"))
				.attribute(WebClient.class.getName() + ".uri.template", "/resource/{id}").build();
		HttpClientContext context = createContext(request);
		// TODO
		//context.seturi.template("/resource/{id}");
		assertThat(this.keyValuesProvider.getLowCardinalityKeyValues(context))
				.contains(KeyValue.of("exception", "None"), KeyValue.of("method", "GET"), KeyValue.of("uri.template", "/resource/{id}"),
						KeyValue.of("status", "200"), KeyValue.of("outcome", "SUCCESS"));
		assertThat(this.keyValuesProvider.getHighCardinalityKeyValues(context)).hasSize(2)
				.contains(KeyValue.of("clientName", "None"), KeyValue.of("uri", "/resource/42"));
	}

	@Test
	void shouldAddKeyValuesForRequestWithoutUriTemplate() {
		HttpClientContext context = createContext(ClientRequest.create(HttpMethod.GET, URI.create("/resource/42")).build());
		assertThat(this.keyValuesProvider.getLowCardinalityKeyValues(context))
				.contains(KeyValue.of("method", "GET"), KeyValue.of("uri.template", "None"));
		assertThat(this.keyValuesProvider.getHighCardinalityKeyValues(context)).hasSize(2).contains(KeyValue.of("uri", "/resource/42"));
	}

	@Test
	void shouldAddClientNameKeyValueForRequestWithHost() {
		HttpClientContext context = createContext(ClientRequest.create(HttpMethod.GET, URI.create("https://localhost:8080/resource/42")).build());
		assertThat(this.keyValuesProvider.getHighCardinalityKeyValues(context)).contains(KeyValue.of("clientName", "localhost"));
	}

	private HttpClientContext createContext(ClientRequest request) {
		HttpClientContext context = new HttpClientContext();
		context.setRequest(new DefaultWebClient.ObservableRequest(request));
		context.setResponse(new DefaultWebClient.ObservableResponse(ClientResponse.create(HttpStatus.OK).build()));
		return context;
	}

}

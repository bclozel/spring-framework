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

package org.springframework.http.client;


import java.io.IOException;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpMethod;
import org.springframework.web.testfixture.http.client.MockClientHttpRequest;
import org.springframework.web.testfixture.http.client.MockClientHttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ClientHttpKeyValuesProvider}.
 *
 * @author Brian Clozel
 */
class ClientHttpKeyValuesProviderTests {

	private ClientHttpKeyValuesProvider keyValuesProvider = new ClientHttpKeyValuesProvider();

	@Test
	void shouldOnlySupportClientHttpObservationContext() {
		assertThat(this.keyValuesProvider.supportsContext(new ClientHttpObservationContext())).isTrue();
		assertThat(this.keyValuesProvider.supportsContext(new Observation.Context())).isFalse();
	}

	@Test
	void shouldAddKeyValuesForNullExchange() {
		ClientHttpObservationContext context = new ClientHttpObservationContext();
		assertThat(this.keyValuesProvider.getLowCardinalityKeyValues(context)).hasSize(5)
				.contains(KeyValue.of("method", "None"), KeyValue.of("uri.template", "None"), KeyValue.of("status", "CLIENT_ERROR"),
						KeyValue.of("exception", "None"), KeyValue.of("outcome", "UNKNOWN"));
		assertThat(this.keyValuesProvider.getHighCardinalityKeyValues(context)).hasSize(2)
				.contains(KeyValue.of("client.name", "None"), KeyValue.of("uri", ""));
	}

	@Test
	void shouldAddKeyValuesForExchangeWithException() {
		ClientHttpObservationContext context = new ClientHttpObservationContext();
		context.setError(new IllegalStateException("Could not create client request"));
		assertThat(this.keyValuesProvider.getLowCardinalityKeyValues(context)).hasSize(5)
				.contains(KeyValue.of("method", "None"), KeyValue.of("uri.template", "None"), KeyValue.of("status", "CLIENT_ERROR"),
						KeyValue.of("exception", "IllegalStateException"), KeyValue.of("outcome", "UNKNOWN"));
		assertThat(this.keyValuesProvider.getHighCardinalityKeyValues(context)).hasSize(2)
				.contains(KeyValue.of("client.name", "None"), KeyValue.of("uri", ""));
	}

	@Test
	void shouldAddKeyValuesForRequestWithUriTemplate() {
		ClientHttpObservationContext context = createContext(
				new MockClientHttpRequest(HttpMethod.GET, "/resource/{id}", 42), new MockClientHttpResponse());
		context.setUriTemplate("/resource/{id}");
		assertThat(this.keyValuesProvider.getLowCardinalityKeyValues(context))
				.contains(KeyValue.of("exception", "None"), KeyValue.of("method", "GET"), KeyValue.of("uri.template", "/resource/{id}"),
						KeyValue.of("status", "200"), KeyValue.of("outcome", "SUCCESS"));
		assertThat(this.keyValuesProvider.getHighCardinalityKeyValues(context)).hasSize(2)
				.contains(KeyValue.of("client.name", "None"), KeyValue.of("uri", "/resource/42"));
	}

	@Test
	void shouldAddKeyValuesForRequestWithoutUriTemplate() {
		ClientHttpObservationContext context = createContext(
				new MockClientHttpRequest(HttpMethod.GET, "/resource/42"), new MockClientHttpResponse());
		assertThat(this.keyValuesProvider.getLowCardinalityKeyValues(context))
				.contains(KeyValue.of("method", "GET"), KeyValue.of("uri.template", "None"));
		assertThat(this.keyValuesProvider.getHighCardinalityKeyValues(context)).hasSize(2).contains(KeyValue.of("uri", "/resource/42"));
	}

	@Test
	void shouldAddClientNameForRequestWithHost() {
		ClientHttpObservationContext context = createContext(
				new MockClientHttpRequest(HttpMethod.GET, "https://localhost:8080/resource/42"),
				new MockClientHttpResponse());
		assertThat(this.keyValuesProvider.getHighCardinalityKeyValues(context)).contains(KeyValue.of("client.name", "localhost"));
	}

	@Test
	void shouldAddKeyValueForNonResolvableStatus() throws Exception {
		ClientHttpObservationContext context = new ClientHttpObservationContext();
		ClientHttpResponse response = mock(ClientHttpResponse.class);
		context.setResponse(response);
		given(response.getStatusCode()).willThrow(new IOException("test error"));
		assertThat(this.keyValuesProvider.getLowCardinalityKeyValues(context)).contains(KeyValue.of("status", "IO_ERROR"));
	}

	private ClientHttpObservationContext createContext(ClientHttpRequest request, ClientHttpResponse response) {
		ClientHttpObservationContext context = new ClientHttpObservationContext();
		context.setRequest(request);
		context.setResponse(response);
		return context;
	}

}

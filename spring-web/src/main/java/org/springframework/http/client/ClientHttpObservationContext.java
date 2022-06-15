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

import io.micrometer.observation.Observation;

import org.springframework.lang.Nullable;

/**
 * Context that holds information for metadata collection
 * during the {@link org.springframework.web.client.RestTemplate} observations.
 *
 * @author Brian Clozel
 * @since 6.0
 */
public class ClientHttpObservationContext extends Observation.Context {

	@Nullable
	private String uriTemplate;

	@Nullable
	private ClientHttpRequest request;

	@Nullable
	private ClientHttpResponse response;

	//

	/**
	 * Return the URI template used for the current client exchange, {@code null} if none was used.
	 */
	@Nullable
	public String getUriTemplate() {
		return this.uriTemplate;
	}

	/**
	 * Set the URI template used for the current client exchange.
	 */
	public void setUriTemplate(@Nullable String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}

	/**
	 * Return the client request for the current observation.
	 * Can be {@code null} if an error is thrown during request creation.
	 */
	@Nullable
	public ClientHttpRequest getRequest() {
		return this.request;
	}

	/**
	 * Set the client request for the current observation.
	 * @param request the current request
	 */
	public void setRequest(@Nullable ClientHttpRequest request) {
		this.request = request;
	}

	/**
	 * Return the client response for the current observation.
	 * Can be {@code null} if no response was received.
	 */
	@Nullable
	public ClientHttpResponse getResponse() {
		return this.response;
	}

	/**
	 * Set the client response for the current observation.
	 * @param response the current response
	 */
	public ClientHttpObservationContext setResponse(@Nullable ClientHttpResponse response) {
		this.response = response;
		return this;
	}
}

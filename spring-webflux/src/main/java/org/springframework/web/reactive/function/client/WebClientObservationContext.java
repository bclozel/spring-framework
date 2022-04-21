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

import io.micrometer.observation.Observation;

import org.springframework.lang.Nullable;

/**
 * Context that holds information for metadata collection
 * during the {@link WebClient} observations.
 *
 * @author Brian Clozel
 * @since 6.0
 */
public class WebClientObservationContext extends Observation.Context {

	@Nullable
	private String uriTemplate;

	@Nullable
	private ClientRequest request;

	@Nullable
	private ClientResponse response;


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
	 * Can be {@code null} if an error is thrown during the connection phase.
	 */
	@Nullable
	public ClientRequest getRequest() {
		return this.request;
	}

	/**
	 * Set the client request for the current observation.
	 * @param request the current request
	 */
	public void setRequest(@Nullable ClientRequest request) {
		this.request = request;
	}

	/**
	 * Return the client response for the current observation.
	 * Can be {@code null} if no response was received.
	 */
	@Nullable
	public ClientResponse getResponse() {
		return this.response;
	}

	/**
	 * Set the client response for the current observation.
	 * @param response the current response
	 */
	public void setResponse(@Nullable ClientResponse response) {
		this.response = response;
	}
}


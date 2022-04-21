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

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.docs.DocumentedObservation;


/**
 * Documented {@link io.micrometer.common.KeyValue key values} for the HTTP client observations,
 * with {@code RestTemplate} and {@code WebClient}.
 * <p>This class is used by automated tools to document KeyValues attached to the HTTP client observations.
 *
 * @author Brian Clozel
 * @since 6.0
 */
public enum ClientHttpObservation implements DocumentedObservation {

	/**
	 * Observation created for a WebClient HTTP exchange.
	 */
	HTTP_REQUEST {
		@Override
		public String getName() {
			return "http.client.requests";
		}

		@Override
		public KeyName[] getLowCardinalityKeyNames() {
			return LowCardinalityKeyNames.values();
		}

		@Override
		public KeyName[] getHighCardinalityKeyNames() {
			return HighCardinalityKeyNames.values();
		}

	};

	public enum LowCardinalityKeyNames implements KeyName {

		/**
		 * Name of HTTP request method or {@code "None"} if the request could not be created.
		 */
		METHOD {
			@Override
			public String getKeyName() {
				return "method";
			}

		},

		/**
		 * URI template used for HTTP request, or {@code ""} if none was provided.
		 */
		URI_TEMPLATE {
			@Override
			public String getKeyName() {
				return "uri.template";
			}
		},

		/**
		 * HTTP response raw status code, or {@code "IO_ERROR"} in case of {@code IOException},
		 * or {@code "CLIENT_ERROR"} if no response was received.
		 */
		STATUS {
			@Override
			public String getKeyName() {
				return "status";
			}
		},

		/**
		 * Name of the exception thrown during the exchange, or {@code "None"} if no exception happened.
		 */
		EXCEPTION {
			@Override
			public String getKeyName() {
				return "exception";
			}
		},

		/**
		 * Outcome of the HTTP client exchange.
		 */
		OUTCOME {
			@Override
			public String getKeyName() {
				return "outcome";
			}
		}

	}

	public enum HighCardinalityKeyNames implements KeyName {

		/**
		 * HTTP request URI.
		 */
		URI {
			@Override
			public String getKeyName() {
				return "uri";
			}
		},

		/**
		 * Client name derived from the request URI host.
		 */
		CLIENT_NAME {
			@Override
			public String getKeyName() {
				return "client.name";
			}
		}

	}
}

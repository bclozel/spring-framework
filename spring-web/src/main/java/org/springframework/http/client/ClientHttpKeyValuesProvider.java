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
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.transport.http.HttpClientResponse;
import io.micrometer.observation.transport.http.context.HttpClientContext;
import io.micrometer.observation.transport.http.tags.Outcome;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Default implementation for a {@code RestTemplate} {@link Observation.KeyValuesProvider},
 * extracting information from the {@link HttpClientContext}.
 *
 * @author Brian Clozel
 * @since 6.0
 */
public class ClientHttpKeyValuesProvider implements Observation.KeyValuesProvider<HttpClientContext> {

	private static final KeyValue URITEMPLATE_NONE = KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.URI_TEMPLATE.getKeyName(), "None");

	private static final KeyValue METHOD_NONE = KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.METHOD.getKeyName(), "None");

	private static final KeyValue EXCEPTION_NONE = KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.EXCEPTION.getKeyName(), "None");


	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof HttpClientContext;
	}

	@Override
	public KeyValues getLowCardinalityKeyValues(HttpClientContext context) {
		return KeyValues.of(uriTemplate(context), method(context), status(context), exception(context), outcome(context));
	}

	protected KeyValue uriTemplate(HttpClientContext context) {
		if (context.getRequest() != null) {
			return KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.URI_TEMPLATE.getKeyName(), context.getRequest().route());
		}
		return URITEMPLATE_NONE;
	}

	protected KeyValue method(HttpClientContext context) {
		if (context.getRequest() != null) {
			return KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.METHOD.getKeyName(), context.getRequest().method());
		}
		else {
			return METHOD_NONE;
		}
	}

	protected KeyValue status(HttpClientContext context) {
		return KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.STATUS.getKeyName(), getStatusMessage(context.getResponse()));
	}

	private String getStatusMessage(@Nullable HttpClientResponse response) {
		try {
			if (response == null) {
				return "CLIENT_ERROR";
			}
			return String.valueOf(response.statusCode());
		}
		catch (RuntimeException ex) {
			return "IO_ERROR";
		}
	}

	protected KeyValue exception(HttpClientContext context) {
		return context.getError().map(exception -> {
			String simpleName = exception.getClass().getSimpleName();
			return KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.EXCEPTION.getKeyName(),
					StringUtils.hasText(simpleName) ? simpleName : exception.getClass().getName());
		}).orElse(EXCEPTION_NONE);
	}

	protected static KeyValue outcome(HttpClientContext context) {
		if (context.getResponse() != null) {
			Outcome outcome = Outcome.forStatus(context.getResponse().statusCode());
			return KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.OUTCOME.getKeyName(), outcome.name());
		}
		return Outcome.UNKNOWN.asTag();
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(HttpClientContext context) {
		return KeyValues.of(requestUri(context), clientName(context));
	}

	protected KeyValue requestUri(HttpClientContext context) {
		if (context.getRequest() != null) {
			return KeyValue.of(ClientHttpObservation.HighCardinalityKeyNames.URI.getKeyName(), context.getRequest().url());
		}
		return KeyValue.of(ClientHttpObservation.HighCardinalityKeyNames.URI.getKeyName(), "");
	}

	protected KeyValue clientName(HttpClientContext context) {
		// TODO: how can we get the http host?
		return KeyValue.of(ClientHttpObservation.HighCardinalityKeyNames.CLIENT_NAME.getKeyName(), "None");
	}

}

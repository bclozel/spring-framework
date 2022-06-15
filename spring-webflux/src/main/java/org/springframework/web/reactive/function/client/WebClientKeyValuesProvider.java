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

import java.io.IOException;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.transport.http.tags.Outcome;

import org.springframework.http.client.ClientHttpObservation;
import org.springframework.util.StringUtils;

/**
 **
 * Default implementation for a {@code WebClient} {@link Observation.KeyValuesProvider},
 * extracting information from the {@link WebClientObservationContext}.
 *
 * @author Brian Clozel
 * @since 6.0
 */
public class WebClientKeyValuesProvider implements Observation.KeyValuesProvider<WebClientObservationContext> {

	private static final KeyValue URITEMPLATE_NONE = KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.URI_TEMPLATE.getKeyName(), "None");

	private static final KeyValue METHOD_NONE = KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.METHOD.getKeyName(), "None");

	private static final KeyValue EXCEPTION_NONE = KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.EXCEPTION.getKeyName(), "None");

	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof WebClientObservationContext;
	}

	@Override
	public KeyValues getLowCardinalityKeyValues(WebClientObservationContext context) {
		return KeyValues.of(uriTemplate(context), method(context), status(context), exception(context), outcome(context));
	}

	protected KeyValue uriTemplate(WebClientObservationContext context) {
		if (context.getUriTemplate() != null) {
			return KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.URI_TEMPLATE.getKeyName(), context.getUriTemplate());
		}
		return URITEMPLATE_NONE;
	}

	protected KeyValue method(WebClientObservationContext context) {
		if (context.getRequest() != null) {
			return KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.METHOD.getKeyName(), context.getRequest().method().name());
		}
		else {
			return METHOD_NONE;
		}
	}

	protected KeyValue status(WebClientObservationContext context) {
		return KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.STATUS.getKeyName(), getStatusMessage(context));
	}

	private String getStatusMessage(WebClientObservationContext context) {
		if (context.getResponse() != null) {
			return String.valueOf(context.getResponse().statusCode().value());
		}
		if (context.getError().isPresent()) {
			return (context.getError().get() instanceof IOException) ? "IO_ERROR" : "CLIENT_ERROR";
		}
		return "CLIENT_ERROR";
	}

	protected KeyValue exception(WebClientObservationContext context) {
		return context.getError().map(exception -> {
			String simpleName = exception.getClass().getSimpleName();
			return KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.EXCEPTION.getKeyName(),
					StringUtils.hasText(simpleName) ? simpleName : exception.getClass().getName());
		}).orElse(EXCEPTION_NONE);
	}

	protected static KeyValue outcome(WebClientObservationContext context) {
		if (context.getResponse() != null) {
			Outcome outcome = Outcome.forStatus(context.getResponse().statusCode().value());
			return KeyValue.of(ClientHttpObservation.LowCardinalityKeyNames.OUTCOME.getKeyName(), outcome.name());
		}
		return Outcome.UNKNOWN.asTag();
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(WebClientObservationContext context) {
		return KeyValues.of(requestUri(context), clientName(context));
	}

	protected KeyValue requestUri(WebClientObservationContext context) {
		if (context.getRequest() != null) {
			return KeyValue.of(ClientHttpObservation.HighCardinalityKeyNames.URI.getKeyName(), context.getRequest().url().toASCIIString());
		}
		return KeyValue.of(ClientHttpObservation.HighCardinalityKeyNames.URI.getKeyName(), "");
	}

	protected KeyValue clientName(WebClientObservationContext context) {
		String host = "None";
		if (context.getRequest() != null && context.getRequest().url().getHost() != null) {
			host = context.getRequest().url().getHost();
		}
		return KeyValue.of(ClientHttpObservation.HighCardinalityKeyNames.CLIENT_NAME.getKeyName(), host);
	}

}

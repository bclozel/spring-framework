/*
 * Copyright 2002-2024 the original author or authors.
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

package org.springframework.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Represents fields written in the trailer section of the HTTP entity, after the body.
 * <p>Field names are all set to their lower case variant. Fields that are required for
 * reading the message are not allowed in the trailer section and will be rejected
 * by this class.
 * @author Brian Clozel
 * @since 7.0
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-trailer-fields">RFC 9110</a>
 * @see HttpHeaders#setTrailerNames(List)
 */

// TODO: make immutable and mutable factory methods?

public class TrailerFields {

	private static final Set<String> ILLEGAL_FIELDS = Set.of("age", "cache-control", "content-encoding",
			"content-length", "content-range", "content-type", "date", "expires",
			"location", "retry-after", "trailer", "transfer-encoding", "vary", "warning");

	private final Map<String, String> trailerFields;

	/**
	 * Create a new Trailer fields container.
	 */
	public TrailerFields() {
		this.trailerFields = new LinkedHashMap<>(2);
	}

	/**
	 * Create a new immutable Trailer fields container with the entries of the given map.
	 */
	public TrailerFields(Map<String, String> fields) {
		Map<String, String> map = new LinkedHashMap<>(fields.size());
		fields.forEach((fieldName, fieldValue)
				-> map.put(toLowerCase(fieldName), fieldValue));
		this.trailerFields = Collections.unmodifiableMap(map);
	}

	/**
	 * Associate the given field name to the provided value.
	 * <p>Trailer fields must not use field names that describe message framing, routing, authentication,
	 * request modifiers, response controls, or content format. Those are reserved to HTTP headers.
	 * @param fieldName the name of the field
	 * @param fieldValue value to be associated with the given field name
	 * @throws IllegalArgumentException if the field name is not allowed
	 */
	public void set(String fieldName, String fieldValue) {
		String name = toLowerCase(fieldName);
		Assert.isTrue(!ILLEGAL_FIELDS.contains(name), () -> "Illegal name for trailer field:" + name);
		this.trailerFields.put(name, fieldValue);
	}

	/**
	 * Merge the given list of values into a single comma-delimited String and associate
	 * it with the given field name.
	 * <p>Trailer fields must not use field names that describe message framing, routing, authentication,
	 * request modifiers, response controls, or content format. Those are reserved to HTTP headers.
	 * @param fieldName the name of the field
	 * @param fieldValues the list of values for this field
	 * @throws IllegalArgumentException if the field name is not allowed
	 */
	public void set(String fieldName, List<String> fieldValues) {
		this.set(fieldName, StringUtils.collectionToCommaDelimitedString(fieldValues));
	}

	/**
	 * Return the field value associated with the given name.
	 * @return the field value or {@code null}.
	 */
	@Nullable
	public String get(String fieldName) {
		return this.trailerFields.get(toLowerCase(fieldName));
	}

	/**
	 * Remove the field associated with the given name.
	 * @return the field value that associated or {@code null}.
	 */
	@Nullable
	public String remove(String fieldName) {
		return this.trailerFields.remove(toLowerCase(fieldName));
	}

	/**
	 * Return the list of available field names.
	 */
	public Set<String> fieldNames() {
		return this.trailerFields.keySet();
	}

	/**
	 * Return the trailer fields container as a map.
	 */
	public Map<String, String> asMap() {
		return Collections.unmodifiableMap(this.trailerFields);
	}

	private String toLowerCase(String fieldName) {
		return fieldName.toLowerCase(Locale.ROOT);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		this.trailerFields.forEach((fieldName, fieldValue) ->
				builder.append(fieldName).append(':').append('"').append(fieldValue).append('"'));
		builder.append(']');
		return builder.toString();
	}

}

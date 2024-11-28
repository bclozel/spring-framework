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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link TrailerFields}.
 */
class TrailerFieldsTests {

	private final TrailerFields trailerFields = new TrailerFields();

	@ParameterizedTest
	@ValueSource(strings = {"age", "cache-control", "content-encoding",
			"content-length", "content-range", "content-type", "date", "expires",
			"location", "retry-after", "trailer", "transfer-encoding", "vary", "warning"})
	void shouldRejectInvalidFieldNames(String fieldName) {
		assertThatIllegalArgumentException().isThrownBy(() -> trailerFields.set(fieldName, "value"));
	}

	@Test
	void shouldLowerCaseFieldNames() {
		this.trailerFields.set("Some-Header", "value");
		assertThat(this.trailerFields.asMap()).containsOnlyKeys("some-header");
	}

	@Test
	void shouldCollectValuesAsList() {
		this.trailerFields.set("some-header", List.of("first", "second", "third"));
		assertThat(this.trailerFields.asMap().get("some-header")).isEqualTo("first,second,third");
	}

}

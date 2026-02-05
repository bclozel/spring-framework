/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.http.converter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.testfixture.http.MockHttpInputMessage;
import org.springframework.web.testfixture.http.MockHttpOutputMessage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.MULTIPART_MIXED;
import static org.springframework.http.MediaType.MULTIPART_RELATED;

/**
 * Tests for {@link FormHttpMessageConverter}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @author Sebastien Deleuze
 */
class FormHttpMessageConverterTests {

	private static final ResolvableType LINKED_MULTI_VALUE_MAP =
			ResolvableType.forClassWithGenerics(LinkedMultiValueMap.class, String.class, String.class);

	private static final ResolvableType MULTI_VALUE_MAP =
			ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, String.class);

	private final FormHttpMessageConverter converter = new FormHttpMessageConverter();



	@Test
	void canRead() {
		assertCanRead(APPLICATION_FORM_URLENCODED);
		assertCanRead(LINKED_MULTI_VALUE_MAP, APPLICATION_FORM_URLENCODED);
		assertCanRead(ResolvableType.forClass(LinkedMultiValueMap.class), APPLICATION_FORM_URLENCODED);

		ResolvableType mapStringObject = ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, Object.class);
		assertCannotRead(mapStringObject, null);
		assertCannotRead(mapStringObject, APPLICATION_FORM_URLENCODED);
	}

	@Test
	void cannotReadMultipart() {
		// Without custom multipart types supported
		asssertCannotReadMultipart();

		// Should still be the case with custom multipart types supported
		asssertCannotReadMultipart();
	}

	@Test
	void canWrite() {
		assertCanWrite(APPLICATION_FORM_URLENCODED);
		assertCanWrite(MediaType.ALL);
		assertCanWrite(LINKED_MULTI_VALUE_MAP, APPLICATION_FORM_URLENCODED);
		assertCanWrite(ResolvableType.forClass(LinkedMultiValueMap.class), APPLICATION_FORM_URLENCODED);
	}

	@Test
	void cannotWriteMultipart() {
		assertCannotWrite(MULTIPART_FORM_DATA);
		assertCannotWrite(MULTIPART_MIXED);
		assertCannotWrite(MULTIPART_RELATED);
		assertCannotWrite(new MediaType("multipart", "form-data", UTF_8));
		assertCannotWrite(null);
	}

	@Test
	void readForm() throws Exception {
		String body = "name+1=value+1&name+2=value+2%2B1&name+2=value+2%2B2&name+3";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(StandardCharsets.ISO_8859_1));
		inputMessage.getHeaders().setContentType(
				new MediaType("application", "x-www-form-urlencoded", StandardCharsets.ISO_8859_1));
		MultiValueMap<String, String> result = this.converter.read(null, inputMessage);

		assertThat(result).as("Invalid result").hasSize(3);
		assertThat(result.getFirst("name 1")).as("Invalid result").isEqualTo("value 1");
		List<String> values = result.get("name 2");
		assertThat(values).as("Invalid result").containsExactly("value 2+1", "value 2+2");
		assertThat(result.getFirst("name 3")).as("Invalid result").isNull();
	}

	@Test
	void readInvalidFormWithValueThatWontUrlDecode() {
		//java.net.URLDecoder doesn't like negative integer values after a % character
		String body = "name+1=value+1&name+2=value+2%" + ((char)-1);
		assertInvalidFormIsRejectedWithSpecificException(body);
	}

	@Test
	void readInvalidFormWithNameThatWontUrlDecode() {
		//java.net.URLDecoder doesn't like negative integer values after a % character
		String body = "name+1=value+1&name+2%" + ((char)-1) + "=value+2";
		assertInvalidFormIsRejectedWithSpecificException(body);
	}

	@Test
	void readInvalidFormWithNameWithNoValueThatWontUrlDecode() {
		//java.net.URLDecoder doesn't like negative integer values after a % character
		String body = "name+1=value+1&name+2%" + ((char)-1);
		assertInvalidFormIsRejectedWithSpecificException(body);
	}

	@Test
	void writeForm() throws IOException {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.set("name 1", "value 1");
		body.add("name 2", "value 2+1");
		body.add("name 2", "value 2+2");
		body.add("name 3", null);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		this.converter.write(body, APPLICATION_FORM_URLENCODED, outputMessage);

		assertThat(outputMessage.getBodyAsString(UTF_8))
				.as("Invalid result").isEqualTo("name+1=value+1&name+2=value+2%2B1&name+2=value+2%2B2&name+3");
		assertThat(outputMessage.getHeaders().getContentType())
				.as("Invalid content-type").isEqualTo(APPLICATION_FORM_URLENCODED);
		assertThat(outputMessage.getHeaders().getContentLength())
				.as("Invalid content-length").isEqualTo(outputMessage.getBodyAsBytes().length);
	}

	private void assertCanRead(MediaType mediaType) {
		assertCanRead(MULTI_VALUE_MAP, mediaType);
	}

	private void assertCanRead(ResolvableType type, MediaType mediaType) {
		assertThat(this.converter.canRead(type, mediaType)).as(type.toClass().getSimpleName() + " : " + mediaType).isTrue();
	}

	private void asssertCannotReadMultipart() {
		assertCannotRead(new MediaType("multipart", "*"));
		assertCannotRead(MULTIPART_FORM_DATA);
		assertCannotRead(MULTIPART_MIXED);
		assertCannotRead(MULTIPART_RELATED);
	}

	private void assertCannotRead(MediaType mediaType) {
		assertCannotRead(MULTI_VALUE_MAP, mediaType);
	}

	private void assertCannotRead(ResolvableType type, MediaType mediaType) {
		assertThat(this.converter.canRead(type, mediaType)).as(type + " : " + mediaType).isFalse();
	}

	private void assertCanWrite(ResolvableType type, MediaType mediaType) {
		assertThat(this.converter.canWrite(type, LinkedMultiValueMap.class, mediaType))
				.as(type + " : " + mediaType).isTrue();
	}

	private void assertCanWrite(MediaType mediaType) {
		assertCanWrite(MULTI_VALUE_MAP, mediaType);
	}

	private void assertCannotWrite(MediaType mediaType) {
		assertThat(this.converter.canWrite(MULTI_VALUE_MAP, MultiValueMap.class, mediaType))
				.as(MultiValueMap.class.getSimpleName() + " : " + mediaType).isFalse();
	}

	private void assertInvalidFormIsRejectedWithSpecificException(String body) {
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes(StandardCharsets.ISO_8859_1));
		inputMessage.getHeaders().setContentType(
				new MediaType("application", "x-www-form-urlencoded", StandardCharsets.ISO_8859_1));

		assertThatThrownBy(() -> this.converter.read(MULTI_VALUE_MAP, inputMessage, null))
				.isInstanceOf(HttpMessageNotReadableException.class)
				.hasCauseInstanceOf(IllegalArgumentException.class)
				.hasMessage("Could not decode HTTP form payload");
	}

}

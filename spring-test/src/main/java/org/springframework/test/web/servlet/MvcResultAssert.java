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

package org.springframework.test.web.servlet;

import java.io.UnsupportedEncodingException;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * AssertJ {@link Assert} for {@link MvcResult}.
 * @author Brian Clozel
 * @since 6.2.0
 */
public class MvcResultAssert extends AbstractObjectAssert<MvcResultAssert, MvcResult> {

	public MvcResultAssert(MvcResult actual) {
		super(actual, MvcResultAssert.class);
	}

	/**
	 * Verifies that the actual response status equal to the specified one.
	 * @param status the expected HTTP response status
	 * @return {@code this} assertion object
	 * @throws AssertionError if the actual response status is not equal to the given one
	 */
	public MvcResultAssert status(HttpStatus status) {
		return matches(MockMvcResultMatchers.status().is(status.value()));
	}

	/**
	 * Extract the HTTP response as a String for further object assertions.
	 * @return a new assertion object testing the HTTP response
	 */
	public AbstractStringAssert<?> extractingContentAsString() {
		return Assertions.assertThat(getContentAsString());
	}

	/**
	 * Apply the {@link ResultHandler} given as an argument to the current {@link MvcResult}.
	 * This method does not perform additional assertions but a function with side effects.
	 * @param resultHandler the result handler to apply
	 * @return {@code this} assertion object
	 */
	public MvcResultAssert andApply(ResultHandler resultHandler) {
		org.springframework.util.Assert.notNull(resultHandler, "resultHandler should not be null");
		try {
			resultHandler.handle(this.actual);
		}
		catch (Exception ex) {
			throw new AssertionError(ex);
		}
		return this;
	}

	public MvcResultAssert matches(ResultMatcher matcher) {
		try {
			matcher.match(this.actual);
			return this.myself;
		}
		catch (Throwable ex) {
			if (ex instanceof AssertionError) {
				throw (AssertionError) ex;
			}
			throw new AssertionError(ex);
		}
	}

	private String getContentAsString() {
		try {
			return this.actual.getResponse().getContentAsString();
		}
		catch (UnsupportedEncodingException ex) {
			throw new AssertionError(ex);
		}
	}

}

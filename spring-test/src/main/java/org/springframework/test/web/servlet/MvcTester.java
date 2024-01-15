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

import org.assertj.core.api.AssertProvider;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.Assert;

/**
 * {@link MockMvc} variant that tests Spring MVC exchanges and provide fluent assertions
 * using {@link org.assertj.core.api.Assertions AssertJ}.
 * @author Brian Clozel
 * @author Stéphane Nicoll
 * @author Phillip Webb
 * @since 6.2.0
 */
public final class MvcTester {

	private final MockMvc mockMvc;


	private MvcTester(MockMvc mockMvc) {
		Assert.notNull(mockMvc, "mockMVC should not be null");
		this.mockMvc = mockMvc;
	}

	/**
	 * Create a new {@link MvcTester} instance that delegates to the given {@link MockMvc}.
	 * @param mockMvc the MockMvc instance to delegate calls to.
	 */
	public static MvcTester create(MockMvc mockMvc) {
		return new MvcTester(mockMvc);
	}

	public MvcOutcome request(MockHttpServletRequestBuilder requestBuilder) {
		try {
			return new MvcOutcome(this.mockMvc.perform(requestBuilder).andReturn());
		}
		catch (Exception exc) {
			throw new IllegalStateException(exc);
		}
	}

	public static class MvcOutcome implements AssertProvider<MvcResultAssert> {

		private MvcResult result;

		MvcOutcome(MvcResult result) {
			this.result = result;
		}

		@Override
		public MvcResultAssert assertThat() {
			return new MvcResultAssert(this.result);
		}

	}

}

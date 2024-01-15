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

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Integration tests for {@link MvcTester}.
 *
 * @author Brian Clozel
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@WebAppConfiguration
public class MvcTesterIntegrationTests {

	private final MvcTester mvcTester;

	MvcTesterIntegrationTests(WebApplicationContext wac) {
		this.mvcTester = MvcTester.create(webAppContextSetup(wac).build());
	}

	@Test
	void shouldAssertRequestBuilder() {
		assertThat(this.mvcTester.request(get("/greet")))
				.status(HttpStatus.OK)
				.extractingContentAsString().isEqualTo("hello");
	}

	@Test
	void shouldApplyResultHandler() {
		AtomicBoolean applied = new AtomicBoolean();
		assertThat(this.mvcTester.request(get("/greet")))
				.andApply(result -> applied.set(true));
		assertThat(applied).isTrue();
	}

	@Configuration
	@EnableWebMvc
	static class WebConfiguration {

		@Bean
		MyController myController() {
			return new MyController();
		}
	}

	@RestController
	static class MyController {

		@GetMapping("/greet")
		String greet() {
			return "hello";
		}

	}
}

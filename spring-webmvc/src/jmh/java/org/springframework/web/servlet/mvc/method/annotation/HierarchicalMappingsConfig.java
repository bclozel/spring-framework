/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.web.servlet.mvc.method.annotation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Configuration
public class HierarchicalMappingsConfig {

	@Bean
	public BenchmarkRequestProvider requestProvider() {
		List<MockHttpServletRequest> requests = new ArrayList<>();
		requests.add(createGetRequest("/home"));

		requests.add(createGetRequest("/guides/gs/rest-service"));
		requests.add(createGetRequest("/guides/gs/scheduling-tasks"));
		requests.add(createGetRequest("/guides/gs/consuming-rest"));
		requests.add(createGetRequest("/guides/gs/relational-data-access"));

		requests.add(createGetRequest("/projects"));
		requests.add(createGetRequest("/projects/spring-framework"));
		requests.add(createGetRequest("/projects/spring-boot"));
		requests.add(createGetRequest("/projects/spring-data"));
		requests.add(createGetRequest("/projects/spring-security"));
		requests.add(createGetRequest("/projects/spring-cloud"));

		requests.add(createGetRequest("/blog/category/releases.atom"));
		requests.add(createGetRequest("/blog/category/engineering.atom"));
		requests.add(createGetRequest("/blog/category/news.atom"));

		requests.add(createGetRequest("/tools/eclipse"));
		requests.add(createGetRequest("/tools/vscode"));

		requests.add(createGetRequest("/team/snicoll"));
		requests.add(createGetRequest("/team/sdeleuze"));
		requests.add(createGetRequest("/team/jhoeller"));
		requests.add(createGetRequest("/team/rstoyanchev"));
		requests.add(createGetRequest("/team/bclozel"));

		requests.add(createGetRequest("/api/projects/spring-framework"));
		requests.add(createGetRequest("/api/projects/spring-boot"));
		requests.add(createGetRequest("/api/projects/spring-data"));
		requests.add(createGetRequest("/api/projects/spring-security"));
		requests.add(createGetRequest("/api/projects/spring-cloud"));

		requests.add(createGetRequest("/api/projects/spring-boot/releases/2.3.0"));
		requests.add(createGetRequest("/api/projects/spring-boot/releases/2.2.0"));
		requests.add(createGetRequest("/api/projects/spring-framework/releases/5.2.0"));
		requests.add(createGetRequest("/api/projects/spring-framework/releases/5.3.0"));

		return () -> requests;
	}

	private MockHttpServletRequest createGetRequest(String path) {
		return new MockHttpServletRequest("GET", path);
	}

	@Controller
	static class HierarchicalMappingsController {

		@GetMapping("/home")
		public String home() {
			return "";
		}

		@GetMapping("/guides")
		public String guides() {
			return "";
		}

		@GetMapping("/guides/gs/{repositoryName}")
		public String guide(@PathVariable String repositoryName) {
			return "";
		}

		@GetMapping("/projects")
		public String projects() {
			return "";
		}

		@GetMapping("/project/{name}")
		public String project(@PathVariable String name) {
			return "";
		}

		@GetMapping("/blog/category/{category}.atom")
		public String atom(@PathVariable String category) {
			return "";
		}

		@GetMapping("/tools/{name}")
		public String tools(@PathVariable String name) {
			return "";
		}

		@GetMapping("/team/{username}")
		public String team(@PathVariable String username) {
			return "";
		}

		@GetMapping("/api/projects/{projectId}")
		public String apiProject(@PathVariable String projectId) {
			return "";
		}

		@GetMapping("/api/projects/{projectId}/releases/{version}")
		public String apiProjectRelease(@PathVariable String projectId, @PathVariable String version) {
			return "";
		}

	}
}

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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Benchmarks for matching requests against annotated handlers.
 * We're considering here the {@link RequestMappingHandlerMapping} with different usage profiles.
 * @author Brian Clozel
 * @see HierarchicalMappingsConfig
 */
@BenchmarkMode(Mode.Throughput)
public class RequestMappingBenchmark {

	@Benchmark
	public void baseline(HierarchicalRequestMappingData data, Blackhole bh) throws Exception {
		for (MockHttpServletRequest request : data.requestProvider.getBenchmarkRequests()) {
			bh.consume(request);
		}
	}


	@State(Scope.Benchmark)
	public static class HierarchicalRequestMappingData extends RequestMappingData {

		@Setup(Level.Trial)
		public void setupContext() {
			setup(HierarchicalMappingsConfig.class);
		}

	}

	@Benchmark
	public void matchHierarchicalMappings(HierarchicalRequestMappingData data, Blackhole bh) throws Exception {

		for (MockHttpServletRequest request : data.requestProvider.getBenchmarkRequests()) {
			bh.consume(data.requestMapping.getHandler(request));
		}
	}

	static abstract class RequestMappingData {

		AnnotationConfigWebApplicationContext context;

		BenchmarkRequestProvider requestProvider;

		RequestMappingHandlerMapping requestMapping;

		void setup(Class<?>... configClasses) {
			this.context = new AnnotationConfigWebApplicationContext();
			this.context.register(DefaultRequestMappingConfig.class);
			this.context.register(configClasses);
			this.context.refresh();
			this.requestProvider = this.context.getBean(BenchmarkRequestProvider.class);
			this.requestMapping = this.context.getBean(RequestMappingHandlerMapping.class);
		}

	}

	@Configuration
	static class DefaultRequestMappingConfig {

		@Bean
		RequestMappingHandlerMapping requestMappingHandlerMapping() {
			return new RequestMappingHandlerMapping();
		}

	}

}

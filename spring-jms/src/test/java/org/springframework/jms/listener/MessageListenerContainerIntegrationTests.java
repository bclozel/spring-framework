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

package org.springframework.jms.listener;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import io.micrometer.observation.Observation;
import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;
import jakarta.jms.MessageListener;
import jakarta.jms.Queue;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.junit.EmbeddedActiveMQExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.MessageInterceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for {@link AbstractMessageListenerContainer} implementations.
 *
 * @author Brian Clozel
 */
class MessageListenerContainerIntegrationTests {

	@RegisterExtension
	EmbeddedActiveMQExtension server = new EmbeddedActiveMQExtension();

	TestObservationRegistry registry = TestObservationRegistry.create();

	ActiveMQConnectionFactory connectionFactory;

	@BeforeEach
	void setupServer() {
		server.start();
		connectionFactory = new ActiveMQConnectionFactory(server.getVmURL());
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("listenerContainers")
	void shouldRecordJmsProcessObservations(AbstractMessageListenerContainer listenerContainer) throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		listenerContainer.setConnectionFactory(connectionFactory);
		listenerContainer.setObservationRegistry(registry);
		listenerContainer.setDestinationName("spring.test.observation");
		listenerContainer.setMessageListener((MessageListener) message -> latch.countDown());
		listenerContainer.afterPropertiesSet();
		listenerContainer.start();
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.convertAndSend("spring.test.observation", "message content");
		latch.await(2, TimeUnit.SECONDS);
		TestObservationRegistryAssert.assertThat(registry).hasObservationWithNameEqualTo("jms.message.process")
				.that()
				.hasHighCardinalityKeyValue("messaging.destination.name", "spring.test.observation");
		TestObservationRegistryAssert.assertThat(registry).hasNumberOfObservationsEqualTo(1);
		listenerContainer.stop();
		listenerContainer.shutdown();
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("listenerContainers")
	void shouldHaveObservationScopeInErrorHandler(AbstractMessageListenerContainer listenerContainer) throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Observation> observationInErrorHandler = new AtomicReference<>();
		listenerContainer.setConnectionFactory(connectionFactory);
		listenerContainer.setObservationRegistry(registry);
		listenerContainer.setDestinationName("spring.test.observation");
		listenerContainer.setMessageListener((MessageListener) message -> {
			throw new IllegalStateException("error");
		});
		listenerContainer.setErrorHandler(error -> {
			observationInErrorHandler.set(registry.getCurrentObservation());
			latch.countDown();
		});
		listenerContainer.afterPropertiesSet();
		listenerContainer.start();
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.convertAndSend("spring.test.observation", "message content");
		latch.await(2, TimeUnit.SECONDS);
		assertThat(observationInErrorHandler.get()).isNotNull();
		TestObservationRegistryAssert.assertThat(registry).hasObservationWithNameEqualTo("jms.message.process")
				.that()
				.hasHighCardinalityKeyValue("messaging.destination.name", "spring.test.observation")
				.hasLowCardinalityKeyValue("exception", "none");
		TestObservationRegistryAssert.assertThat(registry).hasNumberOfObservationsEqualTo(1);
		listenerContainer.stop();
		listenerContainer.shutdown();
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("listenerContainers")
	void shouldApplyDiscardingInterceptorOnReceivedMessage(AbstractMessageListenerContainer listenerContainer) throws Exception {
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.convertAndSend("spring.test.interceptor", "message content");
		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean interceptorCalled = new AtomicBoolean();
		listenerContainer.setConnectionFactory(connectionFactory);

		MessageInterceptor interceptor = (destination, message) -> {
			assertThat(destination).isInstanceOf(Queue.class).extracting("queueName").isEqualTo("spring.test.interceptor");
			interceptorCalled.set(true);
			latch.countDown();
			return false;
		};
		listenerContainer.setReceiveInterceptors(List.of(interceptor));
		listenerContainer.setDestinationName("spring.test.interceptor");
		listenerContainer.setMessageListener((MessageListener) message -> {
			throw new IllegalStateException("should not invoke message listener");
		});
		listenerContainer.afterPropertiesSet();
		listenerContainer.start();
		latch.await(2, TimeUnit.SECONDS);

		assertThat(interceptorCalled).isTrue();
		listenerContainer.stop();
		listenerContainer.shutdown();
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("listenerContainers")
	void shouldApplyMutatingInterceptorOnReceivedMessage(AbstractMessageListenerContainer listenerContainer) throws Exception {
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.convertAndSend("spring.test.interceptor", "message content");
		CountDownLatch latch = new CountDownLatch(1);
		listenerContainer.setConnectionFactory(connectionFactory);

		MessageInterceptor interceptor = (destination, message) -> {
			assertThat(destination).isInstanceOf(Queue.class).extracting("queueName").isEqualTo("spring.test.interceptor");
			message.setStringProperty("spring", "framework");
			return false;
		};
		listenerContainer.setReceiveInterceptors(List.of(interceptor));
		listenerContainer.setDestinationName("spring.test.interceptor");
		listenerContainer.setMessageListener((MessageListener) message -> {
			try {
				assertThat(message.getStringProperty("spring")).isEqualTo("framework");
			}
			catch (Throwable ex) {
				throw new IllegalStateException(ex);
			}
			latch.countDown();
		});
		listenerContainer.afterPropertiesSet();
		listenerContainer.start();
		latch.await(2, TimeUnit.SECONDS);

		listenerContainer.stop();
		listenerContainer.shutdown();
	}

	static Stream<Arguments> listenerContainers() {
		return Stream.of(
				arguments(named(DefaultMessageListenerContainer.class.getSimpleName(), new DefaultMessageListenerContainer())),
				arguments(named(SimpleMessageListenerContainer.class.getSimpleName(), new SimpleMessageListenerContainer()))
		);
	}

	@AfterEach
	void shutdownServer() {
		connectionFactory.close();
		server.stop();
	}

}

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

package org.springframework.jms.support;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;


/**
 * Intercept a {@code Message} during a JMS operation:
 * <ul>
 *   <li>for send operations, interceptors can mutate the message before it is sent, or ignore it
 *   and prevent it from being sent altogether.
 *   <li>for receive operations, interceptors can mutate the message before it is consumed
 *   by the application, or ignore it and prevent its processing altogether.
 * </ul>
 *
 * @author Brian Clozel
 * @since 6.2.0
 */
@FunctionalInterface
public interface MessageInterceptor {

	/**
	 * Intercept the given message during a JMS operation.
	 * @param destination the JMS destination where the message is going to be sent, or where it was received from
	 * @param message the message being intercepted
	 * @return {@code true} if the message should be further processed, or {@code false} if it should be dropped
	 * @throws JMSException throws by {@link Message} methods
	 */
	boolean intercept(Destination destination, Message message) throws JMSException;

}

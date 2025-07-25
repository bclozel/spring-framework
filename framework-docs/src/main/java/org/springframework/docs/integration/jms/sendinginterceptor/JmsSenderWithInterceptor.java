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

package org.springframework.docs.integration.jms.sendinginterceptor;

import java.util.List;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.MessageInterceptor;

public class JmsSenderWithInterceptor {

	private JmsTemplate jmsTemplate;
	private Queue queue;

	public JmsSenderWithInterceptor(ConnectionFactory connectionFactory, String tenantId) {
		this.jmsTemplate = new JmsTemplate(connectionFactory);
		this.jmsTemplate.setSendInterceptors(List.of(new TenantIdMessageInterceptor(tenantId)));
	}

	static class TenantIdMessageInterceptor implements MessageInterceptor {

		private final String tenantId;

		public TenantIdMessageInterceptor(String tenantId) {
			this.tenantId = tenantId;
		}

		@Override
		public boolean intercept(Destination destination, Message message) throws JMSException {
			message.setStringProperty("tenantId", this.tenantId);
			return true;
		}
	}
}

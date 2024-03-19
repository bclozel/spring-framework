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

package org.springframework.jms.core.support;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.jms.JMSException;
import org.junit.jupiter.api.Test;

import org.springframework.jms.JmsException;
import org.springframework.jms.support.JmsUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link org.springframework.jms.support.JmsUtils}
 */
public class JmsUtilsTests {

	@Test
	void shouldConvertJmsExceptionStackTrace() {
		JMSException jmsEx = new JMSException("could not connect");
		Exception innerEx = new Exception("host not found");
		jmsEx.setLinkedException(innerEx);
		JmsException springJmsEx = JmsUtils.convertJmsAccessException(jmsEx);
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		springJmsEx.printStackTrace(out);
		String trace = sw.toString();
		assertThat(trace.indexOf("host not found")).as("inner jms exception not found").isGreaterThan(0);
	}
}

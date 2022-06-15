/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.aot.test.agent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

import org.assertj.core.api.AssertProvider;

import org.springframework.aot.agent.RecordedInvocation;
import org.springframework.aot.agent.RecordedInvocationsListener;
import org.springframework.aot.agent.RecordedInvocationsPublisher;
import org.springframework.aot.agent.RuntimeHintsAgent;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.util.Assert;

/**
 * Invocations relevant to {@link RuntimeHints} recorded during the execution of a block
 * of code instrumented by the {@link RuntimeHintsAgent}.
 *
 * @author Brian Clozel
 * @since 6.0
 */
public class RuntimeHintsInvocations implements RecordedInvocationsListener, AssertProvider<RuntimeHintsInvocationsAssert> {

	private final Deque<RecordedInvocation> recordedInvocations = new ArrayDeque<>();

	/**
	 * Record all method invocations relevant to {@link RuntimeHints} that happened
	 * during the execution of the given action.
	 * @param action the block of code we want to record invocations from
	 * @return the recorded invocations
	 */
	public static RuntimeHintsInvocations record(Runnable action) {
		Assert.notNull(action, "Runnable action should not be null");
		Assert.isTrue(RuntimeHintsAgent.isLoaded(), "RuntimeHintsAgent should be loaded in the current JVM");
		RuntimeHintsInvocations recording = new RuntimeHintsInvocations();
		RecordedInvocationsPublisher.addListener(recording);
		try {
			action.run();
		}
		finally {
			RecordedInvocationsPublisher.removeListener(recording);
		}
		return recording;
	}

	@Override
	public void onInvocation(RecordedInvocation invocation) {
		this.recordedInvocations.addLast(invocation);
	}

	@Override
	public RuntimeHintsInvocationsAssert assertThat() {
		return new RuntimeHintsInvocationsAssert(this);
	}

	Stream<RecordedInvocation> recordedInvocations() {
		return this.recordedInvocations.stream();
	}

}

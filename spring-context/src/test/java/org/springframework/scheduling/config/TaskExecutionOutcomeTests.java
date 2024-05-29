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

package org.springframework.scheduling.config;


import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class TaskExecutionOutcomeTests {

	@Test
	void shouldCreateWithNoneStatus() {
		TaskExecutionOutcome outcome = TaskExecutionOutcome.create();
		assertThat(outcome.getStatus()).isEqualTo(TaskExecutionOutcome.Status.NONE);
		assertThat(outcome.getExecutionTime()).isNull();
		assertThat(outcome.getThrowable()).isNull();
	}

	@Test
	void startedTaskShouldBeOngoing() {
		TaskExecutionOutcome outcome = TaskExecutionOutcome.create();
		Instant now = Instant.now();
		outcome = outcome.start(now);
		assertThat(outcome.getStatus()).isEqualTo(TaskExecutionOutcome.Status.STARTED);
		assertThat(outcome.getExecutionTime()).isEqualTo(now);
		assertThat(outcome.getThrowable()).isNull();
	}

	@Test
	void shouldRejectSuccessWhenNotStarted() {
		TaskExecutionOutcome outcome = TaskExecutionOutcome.create();
		assertThatIllegalStateException().isThrownBy(outcome::success);
	}

	@Test
	void shouldRejectErrorWhenNotStarted() {
		TaskExecutionOutcome outcome = TaskExecutionOutcome.create();
		assertThatIllegalStateException().isThrownBy(() -> outcome.failure(new IllegalArgumentException("test error")));
	}

	@Test
	void finishedTaskShouldBeSuccessful() {
		TaskExecutionOutcome outcome = TaskExecutionOutcome.create();
		Instant now = Instant.now();
		outcome = outcome.start(now);
		outcome = outcome.success();
		assertThat(outcome.getStatus()).isEqualTo(TaskExecutionOutcome.Status.SUCCESS);
		assertThat(outcome.getExecutionTime()).isEqualTo(now);
		assertThat(outcome.getThrowable()).isNull();
	}

	@Test
	void errorTaskShouldBeFailure() {
		TaskExecutionOutcome outcome = TaskExecutionOutcome.create();
		Instant now = Instant.now();
		outcome = outcome.start(now);
		outcome = outcome.failure(new IllegalArgumentException(("test error")));
		assertThat(outcome.getStatus()).isEqualTo(TaskExecutionOutcome.Status.ERROR);
		assertThat(outcome.getExecutionTime()).isEqualTo(now);
		assertThat(outcome.getThrowable()).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void newTaskExecutionShouldNotFail() {
		TaskExecutionOutcome outcome = TaskExecutionOutcome.create();
		Instant now = Instant.now();
		outcome = outcome.start(now);
		outcome = outcome.failure(new IllegalArgumentException(("test error")));

		outcome = outcome.start(now.plusSeconds(2));
		assertThat(outcome.getStatus()).isEqualTo(TaskExecutionOutcome.Status.STARTED);
		assertThat(outcome.getExecutionTime()).isAfter(now);
		assertThat(outcome.getThrowable()).isNull();
	}

}

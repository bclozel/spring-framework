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

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Outcome of a {@link Task} execution.
 * @author Brian Clozel
 * @since 6.2
 */
public final class TaskExecutionOutcome {

	@Nullable
	private final Instant executionTime;

	private final Status status;

	@Nullable
	private final Throwable throwable;

	private TaskExecutionOutcome(@Nullable Instant executionTime, Status status, @Nullable Throwable throwable) {
		this.executionTime = executionTime;
		this.status = status;
		this.throwable = throwable;
	}

	/**
	 * Return the instant when the task execution started,
	 * {@code null} if the task has not started.
	 */
	@Nullable
	public Instant getExecutionTime() {
		return this.executionTime;
	}

	/**
	 * Return the {@link Status} of the execution outcome.
	 */
	public Status getStatus() {
		return this.status;
	}

	/**
	 * Return the exception thrown from the task execution, if any.
	 */
	@Nullable
	public Throwable getThrowable() {
		return this.throwable;
	}

	TaskExecutionOutcome start(Instant executionTime) {
		return new TaskExecutionOutcome(executionTime, Status.STARTED, null);
	}

	TaskExecutionOutcome success() {
		Assert.state(this.executionTime != null, "Task has not been started yet");
		return new TaskExecutionOutcome(this.executionTime, Status.SUCCESS, null);
	}

	TaskExecutionOutcome failure(Throwable throwable) {
		Assert.state(this.executionTime != null, "Task has not been started yet");
		return new TaskExecutionOutcome(this.executionTime, Status.ERROR, throwable);
	}

	static TaskExecutionOutcome create() {
		return new TaskExecutionOutcome(null, Status.NONE, null);
	}


	/**
	 * Status of the task execution outcome.
	 */
	public enum Status {
		/**
		 * The task has not been executed so far.
		 */
		NONE,
		/**
		 * The task execution has been started and is ongoing.
		 */
		STARTED,
		/**
		 * The task execution finished successfully.
		 */
		SUCCESS,
		/**
		 * The task execution finished with an error.
		 */
		ERROR
	}
}

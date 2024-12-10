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

package org.springframework.aot.hint;

import java.util.Objects;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A {@link TypeReference} based on a {@link Class}.
 *
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @author Brian Clozel
 * @since 6.0
 */
final class ReflectionTypeReference implements TypeReference {

	private final Class<?> type;

	private ReflectionTypeReference(Class<?> type) {
		this.type = type;
	}

	static ReflectionTypeReference of(Class<?> type) {
		Assert.notNull(type, "'type' must not be null");
		Assert.notNull(type.getCanonicalName(), "'type.getCanonicalName()' must not be null");
		return new ReflectionTypeReference(type);
	}

	@Override
	public String getCanonicalName() {
		return this.type.getCanonicalName();
	}

	@Override
	public String getName() {
		return this.type.getName();
	}

	@Override
	public String getPackageName() {
		return this.type.getPackage().getName();
	}

	@Override
	public String getSimpleName() {
		return this.type.getSimpleName();
	}

	@Nullable
	@Override
	public TypeReference getEnclosingType() {
		Class<?> enclosingClass = this.type.getEnclosingClass();
		return (enclosingClass != null) ? TypeReference.of(enclosingClass) : null;
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof TypeReference that &&
				getCanonicalName().equals(that.getCanonicalName())));
	}

	@Override
	public int hashCode() {
		return Objects.hash(getCanonicalName());
	}

	@Override
	public String toString() {
		return getCanonicalName();
	}

}

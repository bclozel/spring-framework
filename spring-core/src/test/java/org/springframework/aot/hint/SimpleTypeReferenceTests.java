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

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link SimpleTypeReference}.
 *
 * @author Stephane Nicoll
 * @author Brian Clozel
 */
class SimpleTypeReferenceTests {


	@ParameterizedTest
	@MethodSource("primitivesAndPrimitivesArray")
	void primitivesAreHandledProperly(TypeReference typeReference, Class<?> clazz) {
		assertThat(typeReference.getName()).isEqualTo(clazz.getName());
		assertThat(typeReference.getCanonicalName()).isEqualTo(clazz.getCanonicalName());
		assertThat(typeReference.getPackageName()).isEqualTo("java.lang");
	}

	static Stream<Arguments> primitivesAndPrimitivesArray() {
		return Stream.of(
				Arguments.of(SimpleTypeReference.of("void"), void.class),
				Arguments.of(SimpleTypeReference.of("boolean"), boolean.class),
				Arguments.of(SimpleTypeReference.of("byte"), byte.class),
				Arguments.of(SimpleTypeReference.of("short"), short.class),
				Arguments.of(SimpleTypeReference.of("int"), int.class),
				Arguments.of(SimpleTypeReference.of("long"), long.class),
				Arguments.of(SimpleTypeReference.of("char"), char.class),
				Arguments.of(SimpleTypeReference.of("float"), float.class),
				Arguments.of(SimpleTypeReference.of("double"), double.class),
				Arguments.of(SimpleTypeReference.of("[Z"), boolean[].class),
				Arguments.of(SimpleTypeReference.of("[B"), byte[].class),
				Arguments.of(SimpleTypeReference.of("[S"), short[].class),
				Arguments.of(SimpleTypeReference.of("[I"), int[].class),
				Arguments.of(SimpleTypeReference.of("[J"), long[].class),
				Arguments.of(SimpleTypeReference.of("[C"), char[].class),
				Arguments.of(SimpleTypeReference.of("[F"), float[].class),
				Arguments.of(SimpleTypeReference.of("[D"), double[].class));
	}

	@ParameterizedTest
	@MethodSource("arrays")
	void arraysHaveSuitableReflectionTargetName(TypeReference typeReference, String expectedName) {
		assertThat(typeReference.getName()).isEqualTo(expectedName);
	}

	static Stream<Arguments> arrays() {
		return Stream.of(
				Arguments.of(SimpleTypeReference.of("[Ljava.lang.Object;"), java.lang.Object[].class.getName()),
				Arguments.of(SimpleTypeReference.of("[Ljava.lang.Integer;"), java.lang.Integer[].class.getName()),
				Arguments.of(SimpleTypeReference.of("[[Ljava.lang.Integer;"), java.lang.Integer[][].class.getName()),
				Arguments.of(SimpleTypeReference.of("[Lorg.springframework.aot.hint.SimpleTypeReferenceTests;"), SimpleTypeReferenceTests[].class.getName()));
	}

	@Test
	void nameOfCglibProxy() {
		TypeReference reference = TypeReference.of("com.example.Test$$SpringCGLIB$$0");
		assertThat(reference.getSimpleName()).isEqualTo("Test$$SpringCGLIB$$0");
		assertThat(reference.getEnclosingType()).isNull();
	}

	@Test
	void nameOfNestedCglibProxy() {
		TypeReference reference = TypeReference.of("com.example.Test$Another$$SpringCGLIB$$0");
		assertThat(reference.getSimpleName()).isEqualTo("Another$$SpringCGLIB$$0");
		assertThat(reference.getEnclosingType()).isNotNull();
		assertThat(reference.getEnclosingType().getSimpleName()).isEqualTo("Test");
	}

	@Test
	void typeReferenceInRootPackage() {
		TypeReference type = SimpleTypeReference.of("MyRootClass");
		assertThat(type.getCanonicalName()).isEqualTo("MyRootClass");
		assertThat(type.getPackageName()).isEmpty();
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "com.example.Tes(t", "com.example..Test" })
	void typeReferenceWithInvalidClassName(String invalidClassName) {
		assertThatIllegalStateException().isThrownBy(() -> SimpleTypeReference.of(invalidClassName))
				.withMessageContaining("Invalid class name");
	}

}

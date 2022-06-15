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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.aot.agent.RuntimeHintsAgent;

/**
 * {@code @EneabledIfRuntimeHintsAgent} is used to signal that the annotated test class or test method
 * is only enabled if the {@link RuntimeHintsAgent} is loaded on the current JVM.
 *
 * <pre class="code">
 * &#064;EneabledIfRuntimeHintsAgent
 * class MyTestCases {
 *     &#064;Test
 *     void hintsForMethodsReflectionShouldMatch() {
 *         RuntimeHints hints = new RuntimeHints();
 *         hints.reflection().registerType(String.class,
 *             hint -> hint.withMembers(MemberCategory.INTROSPECT_PUBLIC_METHODS));
 *
 *         RuntimeHintsInvocations invocations = RuntimeHintsInvocations.record(() -> {
 *             Method[] methods = String.class.getMethods();
 *         });
 *         assertThat(invocations).allMatch(hints);
 *     }
 *
 * }
 * </pre>
 *
 * @author Brian Clozel
 * @since 6.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(RuntimeHintsAgentCondition.class)
public @interface EnabledIfRuntimeHintsAgent {

}

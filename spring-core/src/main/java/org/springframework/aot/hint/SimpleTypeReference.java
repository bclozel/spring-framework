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

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A {@link TypeReference} based on fully qualified name.
 *
 * @author Stephane Nicoll
 * @author Brian Clozel
 * @since 6.0
 */
final class SimpleTypeReference implements TypeReference {

	private static final Pattern TYPE_NAME_PATTERN = Pattern.compile(
			"(\\[)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(\\.\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*;?");

	private final String packageName;

	private final String simpleName;

	private final int arrayDimension;

	@Nullable
	private final TypeReference enclosingType;

	@Nullable
	private String canonicalName;

	SimpleTypeReference(String packageName, String simpleName, @Nullable TypeReference enclosingType, int arrayDimension) {
		this.packageName = packageName;
		this.simpleName = simpleName;
		this.enclosingType = enclosingType;
		this.arrayDimension = arrayDimension;
	}

	static SimpleTypeReference of(String className) {
		Assert.notNull(className, "'className' must not be null");
		int arrayDimension = 0;
		if (!isValidClassName(className)) {
			throw new IllegalStateException("Invalid class name '" + className + "'");
		}
		while (className.startsWith("[")) {
			className = className.substring(1);
			arrayDimension++;
		}
		if (arrayDimension > 0) {
			if (className.charAt(0) == 'L') {
				className = className.substring(1, className.length() - 1);
			}
			else {
				Primitive primitive = Primitive.forInternalName(className.charAt(0));
				return new SimpleTypeReference("java.lang", primitive.getName(), null, arrayDimension);
			}
		}
		if (!className.contains("$")) {
			return createTypeReference(className, arrayDimension);
		}
		String[] elements = className.split("(?<!\\$)\\$(?!\\$)");
		SimpleTypeReference typeReference = createTypeReference(elements[0], arrayDimension);
		for (int i = 1; i < elements.length; i++) {
			typeReference = new SimpleTypeReference(typeReference.getPackageName(), elements[i], typeReference, 0);
		}
		return typeReference;
	}

	private static boolean isValidClassName(String className) {
		return TYPE_NAME_PATTERN.matcher(className).matches();
	}

	private static SimpleTypeReference createTypeReference(String className, int dimension) {
		int i = className.lastIndexOf('.');
		if (i != -1) {
			return new SimpleTypeReference(className.substring(0, i), className.substring(i + 1), null, dimension);
		}
		else {
			String packageName = (Primitive.forName(className) != null ? "java.lang" : "");
			return new SimpleTypeReference(packageName, className, null, dimension);
		}
	}

	@Override
	public String getPackageName() {
		return this.packageName;
	}

	@Override
	public String getSimpleName() {
		return this.simpleName;
	}

	@Nullable
	@Override
	public TypeReference getEnclosingType() {
		return this.enclosingType;
	}

	@Override
	public String getName() {
		StringBuilder builder = new StringBuilder();
		if (this.arrayDimension > 0) {
			builder.append("[".repeat(this.arrayDimension));
			Primitive primitive = Primitive.forName(this.simpleName);
			if (primitive != null) {
				builder.append(primitive.getInternalName());
			}
			else {
				builder.append('L');
				writeInternalName(builder);
				builder.append(';');
			}
		}
		else {
			writeInternalName(builder);
		}
		return builder.toString();
	}

	private void writeInternalName(StringBuilder builder) {
		if (this.enclosingType != null) {
			builder.append(this.enclosingType.getName());
			builder.append('$');
			builder.append(this.simpleName);
		}
		else {
			if (shouldWritePackage(this.simpleName)) {
				builder.append(this.packageName);
				builder.append('.');
			}
			builder.append(this.simpleName);
		}
	}

	private boolean shouldWritePackage(String simpleName) {
		return !(this.packageName.isEmpty() ||
				(this.packageName.equals("java.lang") && Primitive.forName(simpleName) != null));
	}

	@Override
	public String getCanonicalName() {
		if (this.canonicalName == null) {
			StringBuilder names = new StringBuilder();
			buildName(this, names);
			if (shouldWritePackage(names.toString())) {
				names.insert(0, '.');
				names.insert(0, this.packageName);
			}
			names.append("[]".repeat(this.arrayDimension));
			this.canonicalName = names.toString();
		}
		return this.canonicalName;
	}

	private static void buildName(@Nullable TypeReference type, StringBuilder sb) {
		if (type == null) {
			return;
		}
		String typeName = (type.getEnclosingType() != null ? "." + type.getSimpleName() : type.getSimpleName());
		sb.insert(0, typeName);
		buildName(type.getEnclosingType(), sb);
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

	enum Primitive {
		BOOLEAN('Z'), BYTE('B'), SHORT('S'), INT('I'),
		LONG('J'), CHAR('C'), FLOAT('F'), DOUBLE('D'),
		VOID('V');

		private final char internalName;

		Primitive(char internalName) {
			this.internalName = internalName;
		}

		String getName() {
			return this.name().toLowerCase(Locale.ROOT);
		}

		@Nullable
		static Primitive forName(String name) {
			for (Primitive candidate : Primitive.values()) {
				if (candidate.getName().toLowerCase(Locale.ROOT).equals(name)) {
					return candidate;
				}
			}
			return null;
		}

		static Primitive forInternalName(char internalName) {
			for (Primitive candidate : Primitive.values()) {
				if (candidate.internalName == internalName) {
					return candidate;
				}
			}
			throw new IllegalArgumentException("Unknown primitive '" + internalName + "'");
		}

		public char getInternalName() {
			return this.internalName;
		}

	}

}

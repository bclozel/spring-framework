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

package org.springframework.web.method.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ExceptionDepthComparator;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.util.MimeType;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Discovers {@linkplain ExceptionHandler @ExceptionHandler} methods in a given class,
 * including all of its superclasses, and helps to resolve a given {@link Exception}
 * and {@link MediaType} requested by clients to combinations supported by a given {@link Method}.
 * <p>This resolver will use the exception information declared as {@code @ExceptionHandler}
 * annotation attributes, or as a method argument as a fallback. This will throw
 * {@code IllegalStateException} instances if:
 * <ul>
 *     <li>No Exception information could be found for a method
 *     <li>An invalid {@link MediaType} has been declared as {@code @ExceptionHandler} attribute
 *     <li>Multiple handlers declare the same exception + media type mapping
 * </ul>
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Brian Clozel
 * @since 3.1
 */
public class ExceptionHandlerMethodResolver {

	/**
	 * A filter for selecting {@code @ExceptionHandler} methods.
	 */
	private static final MethodFilter EXCEPTION_HANDLER_METHODS = method ->
			AnnotatedElementUtils.hasAnnotation(method, ExceptionHandler.class);

	private static final Method NO_MATCHING_EXCEPTION_HANDLER_METHOD;

	static {
		try {
			NO_MATCHING_EXCEPTION_HANDLER_METHOD =
					ExceptionHandlerMethodResolver.class.getDeclaredMethod("noMatchingExceptionHandler");
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalStateException("Expected method not found: " + ex);
		}
	}

	private final Map<ExceptionMappingInfo, Method> mappedMethods = new HashMap<>(16);

	private final ConcurrentLruCache<ExceptionMappingInfo, Method> exceptionLookupCache = new ConcurrentLruCache<>(24,
			(cacheKey) -> getMappedMethod(cacheKey.exceptionType(), cacheKey.mediaType()));


	/**
	 * A constructor that finds {@link ExceptionHandler} methods in the given type.
	 * @param handlerType the type to introspect
	 * @throws IllegalStateException in case of invalid or ambiguous exception mapping declarations
	 */
	public ExceptionHandlerMethodResolver(Class<?> handlerType) {
		for (Method method : MethodIntrospector.selectMethods(handlerType, EXCEPTION_HANDLER_METHODS)) {
			for (ExceptionMappingInfo exceptionMappingInfo : detectExceptionMappings(method)) {
				addExceptionMapping(exceptionMappingInfo, method);
			}
		}
	}


	/**
	 * Extract exception mappings from the {@code @ExceptionHandler} annotation first,
	 * and then as a fallback from the method signature itself.
	 */
	@SuppressWarnings("unchecked")
	private List<ExceptionMappingInfo> detectExceptionMappings(Method method) {
		List<ExceptionMappingInfo> result = new ArrayList<>();
		ExceptionHandler exceptionHandler = readExceptionHandlerAnnotation(method);
		List<Class<? extends Throwable>> exceptions = new ArrayList<>(Arrays.asList(exceptionHandler.exception()));
		if (exceptions.isEmpty()) {
			for (Class<?> paramType : method.getParameterTypes()) {
				if (Throwable.class.isAssignableFrom(paramType)) {
					exceptions.add((Class<? extends Throwable>) paramType);
				}
			}
		}
		if (exceptions.isEmpty()) {
			throw new IllegalStateException("No exception types mapped to " + method);
		}
		List<MediaType> mediaTypes = new ArrayList<>();
		for (String mediaType : exceptionHandler.mediaType()) {
			try {
				mediaTypes.add(MediaType.parseMediaType(mediaType));
			}
			catch (InvalidMediaTypeException exc) {
				throw new IllegalStateException("Invalid media type [" + mediaType + "] declared on @ExceptionHandler  for " + method, exc);
			}
		}
		if (mediaTypes.isEmpty()) {
			mediaTypes.add(MediaType.ALL);
		}
		for (Class<? extends Throwable> throwable : exceptions) {
			for (MediaType mediaType: mediaTypes) {
				result.add(new ExceptionMappingInfo(throwable, mediaType));
			}
		}
		return result;
	}

	private ExceptionHandler readExceptionHandlerAnnotation(Method method) {
		ExceptionHandler ann = AnnotatedElementUtils.findMergedAnnotation(method, ExceptionHandler.class);
		Assert.state(ann != null, "No ExceptionHandler annotation");
		return ann;
	}

	private void addExceptionMapping(ExceptionMappingInfo mappingInfo, Method method) {
		Method oldMethod = this.mappedMethods.put(mappingInfo, method);
		if (oldMethod != null && !oldMethod.equals(method)) {
			throw new IllegalStateException("Ambiguous @ExceptionHandler method mapped for [" +
					mappingInfo + "]: {" + oldMethod + ", " + method + "}");
		}
	}

	/**
	 * Whether the contained type has any exception mappings.
	 */
	public boolean hasExceptionMappings() {
		return !this.mappedMethods.isEmpty();
	}

	/**
	 * Find a {@link Method} to handle the given exception.
	 * <p>Uses {@link ExceptionDepthComparator} if more than one match is found.
	 * @param exception the exception
	 * @return a Method to handle the exception, or {@code null} if none found
	 */
	@Nullable
	public Method resolveMethod(Exception exception) {
		return resolveMethodByThrowable(exception, MediaType.ALL);
	}

	/**
	 * Find a {@link Method} to handle the given exception.
	 * <p>Uses {@link ExceptionDepthComparator} and {@link MediaType#isMoreSpecific(MimeType)}
	 * if more than one match is found.
	 * @param exception the exception
	 * @param mediaType the media type requested by the HTTP client
	 * @return a Method to handle the exception, or {@code null} if none found
	 * @since 6.2.0
	 */
	@Nullable
	public Method resolveMethod(Exception exception, MediaType mediaType) {
		return resolveMethodByThrowable(exception, mediaType);
	}

	/**
	 * Find a {@link Method} to handle the given Throwable.
	 * <p>Uses {@link ExceptionDepthComparator} if more than one match is found.
	 * @param exception the exception
	 * @return a Method to handle the exception, or {@code null} if none found
	 * @since 5.0
	 */
	@Nullable
	public Method resolveMethodByThrowable(Throwable exception) {
		return resolveMethodByThrowable(exception, MediaType.ALL);
	}

	/**
	 * Find a {@link Method} to handle the given Throwable for the requested {@link MediaType}.
	 * <p>Uses {@link ExceptionDepthComparator} and {@link MediaType#isMoreSpecific(MimeType)}
	 * if more than one match is found.
	 * @param exception the exception
	 * @param mediaType the media type requested by the HTTP client
	 * @return a Method to handle the exception, or {@code null} if none found
	 * @since 6.2.0
	 */
	@Nullable
	public Method resolveMethodByThrowable(Throwable exception, MediaType mediaType) {
		Method method = resolveMethodByExceptionType(exception.getClass(), mediaType);
		if (method == null) {
			Throwable cause = exception.getCause();
			if (cause != null) {
				method = resolveMethodByThrowable(cause, mediaType);
			}
		}
		return method;
	}

	/**
	 * Find a {@link Method} to handle the given exception type. This can be
	 * useful if an {@link Exception} instance is not available (e.g. for tools).
	 * <p>Uses {@link ExceptionDepthComparator} if more than one match is found.
	 * @param exceptionType the exception type
	 * @return a Method to handle the exception, or {@code null} if none found
	 */
	@Nullable
	public Method resolveMethodByExceptionType(Class<? extends Throwable> exceptionType) {
		return resolveMethodByExceptionType(exceptionType, MediaType.ALL);
	}

	/**
	 * Find a {@link Method} to handle the given exception type and media type.
	 * This can be useful if an {@link Exception} instance is not available (e.g. for tools).
	 * @param exceptionType the exception type
	 * @param mediaType the media type requested by the HTTP client
	 * @return a Method to handle the exception, or {@code null} if none found
	 */
	@Nullable
	public Method resolveMethodByExceptionType(Class<? extends Throwable> exceptionType, MediaType mediaType) {
		Method method = this.exceptionLookupCache.get(new ExceptionMappingInfo(exceptionType, mediaType));
		return (method != NO_MATCHING_EXCEPTION_HANDLER_METHOD ? method : null);
	}

	/**
	 * Return the {@link Method} mapped to the given exception type, or
	 * {@link #NO_MATCHING_EXCEPTION_HANDLER_METHOD} if none.
	 */
	@Nullable
	private Method getMappedMethod(Class<? extends Throwable> exceptionType, MediaType mediaType) {
		List<ExceptionMappingInfo> matches = new ArrayList<>();
		for (ExceptionMappingInfo mappingInfo : this.mappedMethods.keySet()) {
			if (mappingInfo.exceptionType().isAssignableFrom(exceptionType) && mappingInfo.mediaType().isCompatibleWith(mediaType)) {
				matches.add(mappingInfo);
			}
		}
		if (!matches.isEmpty()) {
			if (matches.size() > 1) {
				matches.sort(new ExceptionMapingInfoComparator(exceptionType));
			}
			return this.mappedMethods.get(matches.get(0));
		}
		else {
			return NO_MATCHING_EXCEPTION_HANDLER_METHOD;
		}
	}

	/**
	 * For the {@link #NO_MATCHING_EXCEPTION_HANDLER_METHOD} constant.
 	 */
	@SuppressWarnings("unused")
	private void noMatchingExceptionHandler() {
	}

	private record ExceptionMappingInfo(Class<? extends Throwable> exceptionType, MediaType mediaType) {

		@Override
		public String toString() {
			return "ExceptionHandler{" +
					"exceptionType=" + exceptionType +
					", mediaType=" + mediaType +
					'}';
		}
	}

	private static class ExceptionMapingInfoComparator implements Comparator<ExceptionMappingInfo> {

		private final ExceptionDepthComparator exceptionDepthComparator;

		public ExceptionMapingInfoComparator(Class<? extends Throwable> exceptionType) {
			this.exceptionDepthComparator = new ExceptionDepthComparator(exceptionType);
		}

		@Override
		public int compare(ExceptionMappingInfo o1, ExceptionMappingInfo o2) {
			int result = this.exceptionDepthComparator.compare(o1.exceptionType(), o2.exceptionType());
			if (result != 0) {
				return result;
			}
			if (o1.mediaType.equals(o2.mediaType())) {
				return 0;
			}
			return (o1.mediaType().isMoreSpecific(o2.mediaType())) ? -1 : 1;
		}
	}

}

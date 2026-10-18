package org.apiphany.json.jackson2;

import java.util.Objects;
import java.util.function.Supplier;

import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.http.JsonHttpContentConverter;
import org.apiphany.json.JsonBuilder;
import org.morphix.reflection.GenericClass;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A {@link ContentConverter} implementation that converts JSON content to objects of type {@code T}. This converter
 * reads and writes with a {@link Jackson2JsonBuilder}. It supports content with the {@code application/json} content
 * type.
 * <p>
 * {@link #Jackson2JsonHttpContentConverter()} reads and writes with the runtime {@link Jackson2JsonBuilder}, resolved
 * by {@link Jackson2JsonBuilder#runtime()} on every call.
 * <p>
 * {@link #Jackson2JsonHttpContentConverter(ObjectMapper)} reads and writes with the given {@link ObjectMapper} instead,
 * without changing it. Use this constructor when a caller already configured a mapper, for example a Spring message
 * converter's mapper.
 *
 * @param <T> the type of the object to which the JSON content will be converted.
 *
 * @author Radu Sebastian LAZIN
 */
public class Jackson2JsonHttpContentConverter<T> extends JsonHttpContentConverter<T> {

	/**
	 * Supplies the {@link Jackson2JsonBuilder} that reads and writes JSON.
	 * <p>
	 * For {@link #Jackson2JsonHttpContentConverter()} this resolves the runtime builder again on every call.
	 * <p>
	 * For {@link #Jackson2JsonHttpContentConverter(ObjectMapper)} this always returns the same instance, built around the
	 * given mapper.
	 */
	private final Supplier<Jackson2JsonBuilder> jsonBuilderSupplier;

	/**
	 * Constructs a new {@link Jackson2JsonHttpContentConverter} that reads and writes with the runtime
	 * {@link Jackson2JsonBuilder}.
	 */
	public Jackson2JsonHttpContentConverter() {
		this.jsonBuilderSupplier = Jackson2JsonBuilder::runtime;
	}

	/**
	 * Constructs a new {@link Jackson2JsonHttpContentConverter} that reads and writes with the given {@link ObjectMapper}.
	 * The mapper is used as given; this constructor does not configure it.
	 *
	 * @param objectMapper the object mapper to use
	 */
	public Jackson2JsonHttpContentConverter(final ObjectMapper objectMapper) {
		Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
		Jackson2JsonBuilder jsonBuilder = new Jackson2JsonBuilder(objectMapper, JsonBuilder.Customization.NONE);
		this.jsonBuilderSupplier = () -> jsonBuilder;
	}

	/**
	 * Returns the current Jackson 2 JSON builder.
	 *
	 * @return the current Jackson 2 JSON builder
	 */
	protected Jackson2JsonBuilder jackson2JsonBuilder() {
		return jsonBuilderSupplier.get();
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, Class)
	 * @see Jackson2JsonBuilder#fromJsonObject(Object, Class)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final Class<T> targetClass) {
		return from(obj, o -> jackson2JsonBuilder().fromJsonObject(o, targetClass), targetClass::getName);
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
	 * @see Jackson2JsonBuilder#fromJsonObject(Object, GenericClass)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final GenericClass<T> targetGenericClass) {
		return from(obj, o -> jackson2JsonBuilder().fromJsonObject(o, targetGenericClass), () -> targetGenericClass.getType().getTypeName());
	}

	/**
	 * @see ContentConverter#toString(Object, ApiMimeType)
	 * @see Jackson2JsonBuilder#toJsonString(Object)
	 */
	@Override
	public String toString(final Object body, final ApiMimeType mimeType) {
		return jackson2JsonBuilder().toJsonString(body);
	}
}

package org.apiphany.json.jackson3;

import java.util.Objects;
import java.util.function.Supplier;

import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.http.JsonHttpContentConverter;
import org.apiphany.json.JsonBuilder;
import org.morphix.reflection.GenericClass;

import tools.jackson.databind.json.JsonMapper;

/**
 * A {@link ContentConverter} implementation that converts JSON content to objects of type {@code T}. This converter
 * reads and writes with a {@link Jackson3JsonBuilder}. It supports content with the {@code application/json} content
 * type.
 * <p>
 * {@link #Jackson3JsonHttpContentConverter()} reads and writes with the runtime {@link Jackson3JsonBuilder}, resolved
 * by {@link Jackson3JsonBuilder#runtime()} on every call.
 * <p>
 * {@link #Jackson3JsonHttpContentConverter(JsonMapper)} reads and writes with the given {@link JsonMapper} instead,
 * without changing it. Use this constructor when a caller already configured a mapper.
 *
 * @param <T> the type of the object to which the JSON content will be converted.
 *
 * @author Radu Sebastian LAZIN
 */
public class Jackson3JsonHttpContentConverter<T> extends JsonHttpContentConverter<T> {

	/**
	 * Supplies the {@link Jackson3JsonBuilder} that reads and writes JSON.
	 * <p>
	 * For {@link #Jackson3JsonHttpContentConverter()} this resolves the runtime builder again on every call.
	 * <p>
	 * For {@link #Jackson3JsonHttpContentConverter(JsonMapper)} this always returns the same instance, built around the
	 * given mapper.
	 */
	private final Supplier<Jackson3JsonBuilder> jsonBuilderSupplier;

	/**
	 * Constructs a new {@link Jackson3JsonHttpContentConverter} that reads and writes with the runtime
	 * {@link Jackson3JsonBuilder}.
	 */
	public Jackson3JsonHttpContentConverter() {
		this.jsonBuilderSupplier = Jackson3JsonBuilder::runtime;
	}

	/**
	 * Constructs a new {@link Jackson3JsonHttpContentConverter} that reads and writes with the given {@link JsonMapper}.
	 * The mapper is used as given; this constructor does not configure it.
	 *
	 * @param jsonMapper the JSON mapper to use
	 */
	public Jackson3JsonHttpContentConverter(final JsonMapper jsonMapper) {
		Objects.requireNonNull(jsonMapper, "jsonMapper cannot be null");
		Jackson3JsonBuilder jsonBuilder = new Jackson3JsonBuilder(jsonMapper, JsonBuilder.Customization.NONE);
		this.jsonBuilderSupplier = () -> jsonBuilder;
	}

	/**
	 * Returns the current Jackson 3 JSON builder.
	 *
	 * @return the current Jackson 3 JSON builder
	 */
	protected Jackson3JsonBuilder jackson3JsonBuilder() {
		return jsonBuilderSupplier.get();
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, Class)
	 * @see Jackson3JsonBuilder#fromJsonObject(Object, Class)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final Class<T> targetClass) {
		return from(obj, o -> jackson3JsonBuilder().fromJsonObject(o, targetClass), targetClass::getName);
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
	 * @see Jackson3JsonBuilder#fromJsonObject(Object, GenericClass)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final GenericClass<T> targetGenericClass) {
		return from(obj, o -> jackson3JsonBuilder().fromJsonObject(o, targetGenericClass),
				() -> targetGenericClass.getType().getTypeName());
	}

	/**
	 * @see ContentConverter#toString(Object, ApiMimeType)
	 * @see Jackson3JsonBuilder#toJsonString(Object)
	 */
	@Override
	public String toString(final Object body, final ApiMimeType mimeType) {
		return jackson3JsonBuilder().toJsonString(body);
	}
}

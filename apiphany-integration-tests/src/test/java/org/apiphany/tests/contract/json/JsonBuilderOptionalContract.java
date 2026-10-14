package org.apiphany.tests.contract.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for the {@code java.util.Optional} serialization and deserialization (including its primitive
 * variants) that must behave identically for every JSON library implementation.
 * <p>
 * An optional value is serialized as the contained value or a JSON {@code null} when empty.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JsonBuilderOptionalContract extends JsonBuilderContract {

	/**
	 * Transfer object holding optional values.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class OptionalDTO {

		private Optional<String> value;

		private OptionalInt count;

		private OptionalLong total;

		private OptionalDouble ratio;

		public Optional<String> getValue() {
			return value;
		}

		public void setValue(final Optional<String> value) {
			this.value = value;
		}

		public OptionalInt getCount() {
			return count;
		}

		public void setCount(final OptionalInt count) {
			this.count = count;
		}

		public OptionalLong getTotal() {
			return total;
		}

		public void setTotal(final OptionalLong total) {
			this.total = total;
		}

		public OptionalDouble getRatio() {
			return ratio;
		}

		public void setRatio(final OptionalDouble ratio) {
			this.ratio = ratio;
		}
	}

	@DisplayName("JSON: java.util.Optional values are serialized as the contained value or null and read back")
	@Test
	default void shouldTransformOptionalToJsonAndReadItBack() {
		OptionalDTO present = new OptionalDTO();
		present.setValue(Optional.of("x"));
		present.setCount(OptionalInt.of(7));
		present.setTotal(OptionalLong.of(8L));
		present.setRatio(OptionalDouble.of(9.5));

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(present));

		assertThat(json, equalTo("{\"value\":\"x\",\"count\":7,\"total\":8,\"ratio\":9.5}"));

		OptionalDTO read = JsonBuilder.fromJson(json, OptionalDTO.class);

		assertThat(read.getValue(), equalTo(Optional.of("x")));
		assertThat(read.getCount(), equalTo(OptionalInt.of(7)));
		assertThat(read.getTotal(), equalTo(OptionalLong.of(8L)));
		assertThat(read.getRatio(), equalTo(OptionalDouble.of(9.5)));
	}

	@DisplayName("JSON: empty java.util.Optional values are serialized as null and read back as empty")
	@Test
	default void shouldTransformEmptyOptionalToJsonAndReadItBack() {
		OptionalDTO empty = new OptionalDTO();
		empty.setValue(Optional.empty());
		empty.setCount(OptionalInt.empty());
		empty.setTotal(OptionalLong.empty());
		empty.setRatio(OptionalDouble.empty());

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(empty));

		assertThat(json, equalTo("{\"value\":null,\"count\":null,\"total\":null,\"ratio\":null}"));

		OptionalDTO read = JsonBuilder.fromJson(json, OptionalDTO.class);

		assertThat(read.getValue(), equalTo(Optional.empty()));
		assertThat(read.getCount(), equalTo(OptionalInt.empty()));
		assertThat(read.getTotal(), equalTo(OptionalLong.empty()));
		assertThat(read.getRatio(), equalTo(OptionalDouble.empty()));
	}
}

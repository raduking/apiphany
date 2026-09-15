package org.apiphany.spring.env;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.lang.function.Suppliers;
import org.morphix.reflection.Constructors;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * Tests for {@link EnvironmentProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class EnvironmentPropertiesTest {

	private static final String PROPERTY_SOURCE_PREFIX = "property.source.";

	private static final String KEY = "test.key";
	private static final String VALUE = "test-value";

	/**
	 * Environment without any pre-configured property sources for deterministic assertions.
	 */
	static class EmptyEnvironment extends StandardEnvironment {

		@Override
		protected void customizePropertySources(final MutablePropertySources propertySources) {
			// no default property sources
		}
	}

	@Nested
	class SetTests {

		@Test
		void shouldSetPropertyValue() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			EnvironmentProperties.set(env, KEY, VALUE);

			assertThat(env.getProperty(KEY), equalTo(VALUE));
		}

		@Test
		void shouldCreatePropertySourceNamedAfterPropertyKey() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			EnvironmentProperties.set(env, KEY, VALUE);

			assertThat(env.getPropertySources().contains(PROPERTY_SOURCE_PREFIX + KEY), is(true));
		}

		@Test
		void shouldAddPropertySourceFirst() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("other", Map.of("other.key", "other-value")));

			EnvironmentProperties.set(env, KEY, VALUE);

			assertThat(env.getPropertySources().iterator().next().getName(), equalTo(PROPERTY_SOURCE_PREFIX + KEY));
		}

		@Test
		void shouldOverridePropertyFromExistingPropertySource() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addFirst(new MapPropertySource("other", Map.of(KEY, "other-value")));

			EnvironmentProperties.set(env, KEY, VALUE);

			assertThat(env.getProperty(KEY), equalTo(VALUE));
		}

		@Test
		void shouldUpdateValueOnSecondSetForSameProperty() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			EnvironmentProperties.set(env, KEY, VALUE);
			EnvironmentProperties.set(env, KEY, "new-value");

			assertThat(env.getProperty(KEY), equalTo("new-value"));
			assertThat(env.getPropertySources().size(), equalTo(1));
		}

		@Test
		void shouldCreateSeparatePropertySourcesForDifferentProperties() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			EnvironmentProperties.set(env, KEY, VALUE);
			EnvironmentProperties.set(env, "other.key", "other-value");

			assertThat(env.getProperty(KEY), equalTo(VALUE));
			assertThat(env.getProperty("other.key"), equalTo("other-value"));
			assertThat(env.getPropertySources().size(), equalTo(2));
		}

		@Test
		void shouldRestorePriorityWhenUpdatingDemotedPropertySource() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			EnvironmentProperties.set(env, KEY, VALUE);
			env.getPropertySources().addFirst(new MapPropertySource("higher", Map.of(KEY, "higher-value")));

			EnvironmentProperties.set(env, KEY, "set-after-demotion");

			assertThat(env.getProperty(KEY), equalTo("set-after-demotion"));
			assertThat(env.getPropertySources().iterator().next().getName(), equalTo(PROPERTY_SOURCE_PREFIX + KEY));
			assertThat(env.getPropertySources().size(), equalTo(2));
		}

		@Test
		void shouldSetNonStringPropertyValue() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			EnvironmentProperties.set(env, KEY, 42);

			Integer result = EnvironmentProperties.get(env, KEY, Integer.class, Suppliers.supplyNull());

			assertThat(result, equalTo(42));
		}
	}

	@Nested
	class GetTests {

		@Test
		void shouldReturnPropertyValue() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("test", Map.of(KEY, VALUE)));

			String result = EnvironmentProperties.get(env, KEY, String.class, (String) null);

			assertThat(result, equalTo(VALUE));
		}

		@Test
		void shouldReturnDefaultValueWhenPropertyIsMissing() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			String result1 = EnvironmentProperties.get(env, "missing.key", String.class, "default");
			String result2 = EnvironmentProperties.get(env, "missing.key", String.class, (String) null);

			assertThat(result1, equalTo("default"));
			assertThat(result2, nullValue());
		}

		@Test
		void shouldConvertPropertyValueToRequestedType() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("test", Map.of(KEY, "42")));

			Integer result = EnvironmentProperties.get(env, KEY, Integer.class, 0);

			assertThat(result, equalTo(42));
		}

		@Test
		void shouldReturnDefaultValueFromSupplierWhenPropertyIsMissing() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			String result = EnvironmentProperties.get(env, "missing.key", String.class, (Supplier<String>) () -> "supplied-default");

			assertThat(result, equalTo("supplied-default"));
		}

		@Test
		void shouldReturnPropertyValueWhenDefaultValueIsSupplied() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("test", Map.of(KEY, VALUE)));

			String result = EnvironmentProperties.get(env, KEY, String.class, (Supplier<String>) () -> "supplied-default");

			assertThat(result, equalTo(VALUE));
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldNotEvaluateSupplierWhenPropertyIsPresent() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("test", Map.of(KEY, VALUE)));
			Supplier<String> supplier = mock(Supplier.class);

			EnvironmentProperties.get(env, KEY, String.class, supplier);

			verify(supplier, never()).get();
		}

		@Test
		@SuppressWarnings("unchecked")
		void shouldEvaluateSupplierWhenPropertyIsMissing() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			Supplier<String> supplier = mock(Supplier.class);
			when(supplier.get()).thenReturn("supplied-default");

			String result = EnvironmentProperties.get(env, "missing.key", String.class, supplier);

			assertThat(result, equalTo("supplied-default"));
			verify(supplier, times(1)).get();
		}
	}

	@Nested
	class GetMapTests {

		@Test
		void shouldReturnAllProperties() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("first", Map.of("first.key", "first-value")));
			env.getPropertySources().addLast(new MapPropertySource("second", Map.of("second.key", "second-value")));

			Map<String, String> result = EnvironmentProperties.getMap(env);

			assertThat(result, equalTo(Map.of("first.key", "first-value", "second.key", "second-value")));
		}

		@Test
		void shouldReturnEmptyMapWhenEnvironmentHasNoProperties() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			Map<String, String> result = EnvironmentProperties.getMap(env);

			assertTrue(result.isEmpty());
		}

		@Test
		void shouldReturnPropertiesSetWithEnvironmentProperties() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			EnvironmentProperties.set(env, KEY, VALUE);

			assertThat(EnvironmentProperties.getMap(env), equalTo(Map.of(KEY, VALUE)));
		}

		@Test
		void shouldSkipPropertiesWithNullValues() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			Map<String, Object> properties = new HashMap<>();
			properties.put(KEY, VALUE);
			properties.put("null.key", null);
			env.getPropertySources().addLast(new MapPropertySource("test", properties));

			Map<String, String> result = EnvironmentProperties.getMap(env);

			assertThat(result, equalTo(Map.of(KEY, VALUE)));
		}

		@Test
		void shouldFilterPropertiesWithPredicate() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("test",
					Map.of("first.key", "first-value", "second.key", "second-value")));

			Map<String, String> result = EnvironmentProperties.getMap(env, name -> name.startsWith("first."));

			assertThat(result, equalTo(Map.of("first.key", "first-value")));
		}

		@Test
		void shouldReturnPropertyOnceWhenDefinedInMultiplePropertySources() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("lower", Map.of(KEY, "lower-value")));
			env.getPropertySources().addFirst(new MapPropertySource("higher", Map.of(KEY, "higher-value")));

			Map<String, String> result = EnvironmentProperties.getMap(env);

			assertThat(result, equalTo(Map.of(KEY, "higher-value")));
		}

		@Test
		void shouldIgnoreNonEnumerablePropertySources() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new PropertySource<String>("non-enumerable") {

				@Override
				public Object getProperty(final String name) {
					return KEY.equals(name) ? VALUE : null;
				}
			});

			Map<String, String> result = EnvironmentProperties.getMap(env);

			assertTrue(result.isEmpty());
		}
	}

	@Nested
	class GetListTests {

		@Test
		void shouldReturnAllProperties() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("first", Map.of("first.key", "first-value")));
			env.getPropertySources().addLast(new MapPropertySource("second", Map.of("second.key", "second-value")));

			List<String> result = EnvironmentProperties.getList(env);

			assertThat(result.size(), equalTo(2));
			assertThat(result, hasItems("first.key=first-value", "second.key=second-value"));
		}

		@Test
		void shouldReturnEmptyListWhenEnvironmentHasNoProperties() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			List<String> result = EnvironmentProperties.getList(env);

			assertTrue(result.isEmpty());
		}

		@Test
		void shouldReturnPropertiesSetWithEnvironmentProperties() {
			ConfigurableEnvironment env = new EmptyEnvironment();

			EnvironmentProperties.set(env, KEY, VALUE);

			List<String> result = EnvironmentProperties.getList(env);

			assertThat(result, equalTo(List.of(KEY + "=" + VALUE)));
		}

		@Test
		void shouldSkipPropertiesWithNullValues() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			Map<String, Object> properties = new HashMap<>();
			properties.put(KEY, VALUE);
			properties.put("null.key", null);
			env.getPropertySources().addLast(new MapPropertySource("test", properties));

			List<String> result = EnvironmentProperties.getList(env);

			assertThat(result, equalTo(List.of(KEY + "=" + VALUE)));
		}

		@Test
		void shouldFilterPropertiesWithPredicate() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("test",
					Map.of("first.key", "first-value", "second.key", "second-value")));

			List<String> result = EnvironmentProperties.getList(env, name -> name.startsWith("first."));

			assertThat(result, equalTo(List.of("first.key=first-value")));
		}

		@Test
		void shouldReturnPropertyOnceWhenDefinedInMultiplePropertySources() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new MapPropertySource("lower", Map.of(KEY, "lower-value")));
			env.getPropertySources().addFirst(new MapPropertySource("higher", Map.of(KEY, "higher-value")));

			List<String> result = EnvironmentProperties.getList(env);

			assertThat(result, equalTo(List.of(KEY + "=higher-value")));
		}

		@Test
		void shouldIgnoreNonEnumerablePropertySources() {
			ConfigurableEnvironment env = new EmptyEnvironment();
			env.getPropertySources().addLast(new PropertySource<String>("non-enumerable") {

				@Override
				public Object getProperty(final String name) {
					return KEY.equals(name) ? VALUE : null;
				}
			});

			List<String> result = EnvironmentProperties.getList(env);

			assertTrue(result.isEmpty());
		}
	}

	@Test
	void shouldPreventInstantiation() {
		UnsupportedOperationException e = assertDefaultConstructorThrows(EnvironmentProperties.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}

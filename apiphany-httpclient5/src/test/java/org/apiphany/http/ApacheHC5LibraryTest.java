package org.apiphany.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.runtime.LibraryVersion;

/**
 * Test class for {@link ApacheHC5Library}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApacheHC5LibraryTest {

	@Nested
	class IsPresentTests {

		@Test
		void shouldReturnTrueWhenHttpClient5IsOnClasspath() {
			assertThat(ApacheHC5Library.isPresent(), equalTo(true));
		}
	}

	@Nested
	class VersionTests {

		@Test
		void shouldReturnRuntimeVersion() {
			String version = ApacheHC5Library.version().value();

			assertThat(version, notNullValue());
		}
	}

	@Nested
	class VerifyVersionTests {

		@Test
		void shouldNotThrowForRuntimeVersion() {
			assertDoesNotThrow(ApacheHC5Library::verifyVersion);
		}

		@Test
		void shouldThrowWhenRuntimeVersionIsOlderThanMinimum() {
			LibraryVersion oldVersion = LibraryVersion.of(ApacheHC5Library.CLIENT_NAME, "5.4.4");
			LibraryVersion minimumVersion = ApacheHC5Library.minimumVersion();

			assertThrows(IllegalStateException.class, () -> oldVersion.verifyAtLeast(minimumVersion));
		}

		@Test
		void shouldThrowWhenRuntimeVersionIsNewerThanMaximum() {
			LibraryVersion newVersion = LibraryVersion.of(ApacheHC5Library.CLIENT_NAME, "5.7.0");
			LibraryVersion maximumVersion = ApacheHC5Library.maximumVersion();

			assertThrows(IllegalStateException.class, () -> newVersion.verifyAtMost(maximumVersion));
		}

		@Test
		void shouldNotThrowForMaximumVersionItself() {
			LibraryVersion maximumVersion = ApacheHC5Library.maximumVersion();

			assertDoesNotThrow(() -> maximumVersion.verifyAtMost(ApacheHC5Library.maximumVersion()));
		}

		@Test
		void shouldNotThrowWhenRuntimeVersionCannotBeDetermined() {
			LibraryVersion undeterminedVersion = LibraryVersion.of(ApacheHC5Library.CLIENT_NAME, (String) null);

			assertDoesNotThrow(() -> undeterminedVersion.verifyAtLeast(ApacheHC5Library.minimumVersion()));
			assertDoesNotThrow(() -> undeterminedVersion.verifyAtMost(ApacheHC5Library.maximumVersion()));
		}
	}

	@Nested
	class MinimumVersionTests {

		@Test
		void shouldReturnMinimumRequiredVersion() {
			assertThat(ApacheHC5Library.minimumVersion().value(), equalTo("5.5.0"));
		}

		@Test
		void shouldNotBeNewerThanMaximumVersion() {
			assertThat(ApacheHC5Library.minimumVersion().isAtMost(ApacheHC5Library.maximumVersion()), equalTo(true));
		}
	}

	@Nested
	class MaximumVersionTests {

		@Test
		void shouldReturnMaximumSupportedVersion() {
			assertThat(ApacheHC5Library.maximumVersion().value(), equalTo("5.6.4"));
		}
	}

}

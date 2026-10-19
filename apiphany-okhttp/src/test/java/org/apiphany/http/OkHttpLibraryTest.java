package org.apiphany.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.runtime.LibraryVersion;

/**
 * Test class for {@link OkHttpLibrary}.
 *
 * @author Radu Sebastian LAZIN
 */
class OkHttpLibraryTest {

	@Nested
	class IsPresentTests {

		@Test
		void shouldReturnTrueWhenOkHttpIsOnClasspath() {
			assertThat(OkHttpLibrary.isPresent(), equalTo(true));
		}
	}

	@Nested
	class VersionTests {

		@Test
		void shouldReturnNullRuntimeVersionWhenManifestHasNoVersion() {
			// OkHttp jars do not declare an Implementation-Version manifest entry
			assertThat(OkHttpLibrary.version().value(), nullValue());
		}
	}

	@Nested
	class VerifyVersionTests {

		@Test
		void shouldNotThrowForRuntimeVersion() {
			assertDoesNotThrow(OkHttpLibrary::verifyVersion);
		}

		@Test
		void shouldThrowWhenRuntimeVersionIsOlderThanMinimum() {
			LibraryVersion oldVersion = LibraryVersion.of(OkHttpLibrary.CLIENT_NAME, "4.8.1");
			LibraryVersion minimumVersion = OkHttpLibrary.minimumVersion();

			assertThrows(IllegalStateException.class, () -> oldVersion.verifyAtLeast(minimumVersion));
		}

		@Test
		void shouldThrowWhenRuntimeVersionIsNewerThanMaximum() {
			LibraryVersion newVersion = LibraryVersion.of(OkHttpLibrary.CLIENT_NAME, "5.0.0");
			LibraryVersion maximumVersion = OkHttpLibrary.maximumVersion();

			assertThrows(IllegalStateException.class, () -> newVersion.verifyAtMost(maximumVersion));
		}

		@Test
		void shouldNotThrowForMaximumVersionItself() {
			LibraryVersion maximumVersion = OkHttpLibrary.maximumVersion();

			assertDoesNotThrow(() -> maximumVersion.verifyAtMost(OkHttpLibrary.maximumVersion()));
		}

		@Test
		void shouldNotThrowWhenRuntimeVersionCannotBeDetermined() {
			LibraryVersion undeterminedVersion = LibraryVersion.of(OkHttpLibrary.CLIENT_NAME, (String) null);

			assertDoesNotThrow(() -> undeterminedVersion.verifyAtLeast(OkHttpLibrary.minimumVersion()));
			assertDoesNotThrow(() -> undeterminedVersion.verifyAtMost(OkHttpLibrary.maximumVersion()));
		}
	}

	@Nested
	class MinimumVersionTests {

		@Test
		void shouldReturnMinimumRequiredVersion() {
			assertThat(OkHttpLibrary.minimumVersion().value(), equalTo("4.9.0"));
		}

		@Test
		void shouldNotBeNewerThanMaximumVersion() {
			assertThat(OkHttpLibrary.minimumVersion().isAtMost(OkHttpLibrary.maximumVersion()), equalTo(true));
		}
	}

	@Nested
	class MaximumVersionTests {

		@Test
		void shouldReturnMaximumSupportedVersion() {
			assertThat(OkHttpLibrary.maximumVersion().value(), equalTo("4.12.0"));
		}
	}

}

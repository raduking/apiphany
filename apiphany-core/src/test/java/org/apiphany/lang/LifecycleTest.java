package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Lifecycle}.
 *
 * @author Radu Sebastian LAZIN
 */
class LifecycleTest {

	@Test
	void shouldBuildFromBoolean() {
		Lifecycle managed = Lifecycle.from(true);
		Lifecycle unmanaged = Lifecycle.from(false);

		assertThat(managed, equalTo(Lifecycle.MANAGED));
		assertThat(unmanaged, equalTo(Lifecycle.UNMANAGED));
	}

	@Test
	void shouldReturnManaged() {
		assertThat(Lifecycle.MANAGED.isManaged(), equalTo(true));
		assertThat(Lifecycle.UNMANAGED.isManaged(), equalTo(false));
	}

	@Test
	void shouldReturnUnmanaged() {
		assertThat(Lifecycle.MANAGED.isUnmanaged(), equalTo(false));
		assertThat(Lifecycle.UNMANAGED.isUnmanaged(), equalTo(true));
	}
}

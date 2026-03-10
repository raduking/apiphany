package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link CurveInfo}.
 *
 * @author Radu Sebastian LAZIN
 */
class CurveInfoTest {

	@Test
	void shouldEqualSameValuesAndSameReference() {
		CurveInfo ci1 = new CurveInfo(CurveType.NAMED_CURVE, NamedCurve.X25519);
		CurveInfo ci2 = new CurveInfo(CurveType.NAMED_CURVE, NamedCurve.X25519);

		// same reference
		assertEquals(ci1, ci1);

		// different instance, same values
		assertEquals(ci1, ci2);
		assertEquals(ci2, ci1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(ci1.hashCode(), ci2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		CurveInfo ci1 = new CurveInfo(CurveType.NAMED_CURVE, NamedCurve.X25519);
		CurveInfo ci2 = new CurveInfo(CurveType.NAMED_CURVE, NamedCurve.SECP384R1);

		// different objects
		assertNotEquals(ci1, ci2);
		assertNotEquals(ci2, ci1);

		// different types
		assertThat(ci1, not(equalTo(null)));
		assertThat(ci1, not(equalTo("not-curve-info")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		CurveInfo ci = new CurveInfo(CurveType.NAMED_CURVE, NamedCurve.X25519);

		int expectedHash = Objects.hash(
				ci.getType(),
				ci.getName());

		assertEquals(expectedHash, ci.hashCode());
	}
}

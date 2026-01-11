package org.apiphany.utils;

import java.util.Objects;

import org.apiphany.json.JsonBuilder;

/**
 * A simple DTO for tests.
 *
 * @author Radu Sebastian LAZIN
 */
public class TestDto {

	public static final TestDto EMPTY = new TestDto();

	private String id;

	private int count;

	public static TestDto of(final String id, final int count) {
		TestDto result = new TestDto();
		result.setId(id);
		result.setCount(count);
		return result;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(final int count) {
		this.count = count;
	}

	@Override
	public int hashCode() {
		return Objects.hash(count, id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TestDto)) {
			return false;
		}
		TestDto other = (TestDto) obj;
		return count == other.count && Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

}

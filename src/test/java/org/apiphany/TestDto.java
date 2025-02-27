package org.apiphany;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apiphany.json.JsonBuilder;

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
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

}

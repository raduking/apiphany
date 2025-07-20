package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.Int16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;

public class SupportedGroups implements TLSExtension {

	private final ExtensionType type;

	private final Int16 length;

	private final Int16 groupsSize;

	private final List<NamedCurve> groups;

	public SupportedGroups(final ExtensionType type, final Int16 size, final Int16 groupsSize, final List<NamedCurve> groups) {
		this.type = type;
		this.length = size;
		this.groupsSize = groupsSize;
		this.groups = groups;
	}

	public SupportedGroups(final List<NamedCurve> namedCurves) {
		this(
				ExtensionType.SUPPORTED_GROUPS,
				Int16.of((short) (namedCurves.size() * NamedCurve.BYTES + Int16.BYTES)),
				Int16.of((short) (namedCurves.size() * NamedCurve.BYTES)),
				namedCurves
		);
	}

	public SupportedGroups() {
		this(List.of(NamedCurve.values()));
	}

	public static SupportedGroups from(final InputStream is) throws IOException {
		Int16 int16 = Int16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static SupportedGroups from(final InputStream is, final ExtensionType type) throws IOException {
		Int16 length = Int16.from(is);
		Int16 listSize = Int16.from(is);
		List<NamedCurve> groups = new ArrayList<>();
		for (int i = 0; i < listSize.getValue() / NamedCurve.BYTES; ++i) {
			Int16 value = Int16.from(is);
			NamedCurve curveName = NamedCurve.fromValue(value.getValue());
			groups.add(curveName);
		}

		return new SupportedGroups(type, length, listSize, groups);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(groupsSize.toByteArray());
		for (NamedCurve group : groups) {
			buffer.put(group.toByteArray());
		}
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return type.sizeOf() + length.sizeOf() + groupsSize.sizeOf() + ByteSizeable.sizeOf(groups);
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}

	public Int16 getGroupsSize() {
		return groupsSize;
	}

	public List<NamedCurve> getGroups() {
		return groups;
	}
}

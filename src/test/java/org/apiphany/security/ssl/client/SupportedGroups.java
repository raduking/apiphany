package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class SupportedGroups implements TLSExtension {

	private ExtensionType type;

	private Int16 length;

	private Int16 listSize;

	private List<CurveName> groups;

	public SupportedGroups(final ExtensionType type, final Int16 size, final Int16 listSize, final List<CurveName> groups) {
		this.type = type;
		this.length = size;
		this.listSize = listSize;
		this.groups = groups;
	}

	public SupportedGroups(final List<CurveName> curveNames) {
		this(
				ExtensionType.SUPPORTED_GROUPS,
				new Int16((short) (curveNames.size() * CurveName.BYTES + Int16.BYTES)),
				new Int16((short) (curveNames.size() * CurveName.BYTES)),
				curveNames
		);
	}

	public SupportedGroups() {
		this(List.of(CurveName.values()));
	}

	public static SupportedGroups from(final InputStream is) throws IOException {
		Int16 int16 = Int16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static SupportedGroups from(final InputStream is, final ExtensionType type) throws IOException {
		Int16 length = Int16.from(is);
		Int16 listSize = Int16.from(is);
		List<CurveName> groups = new ArrayList<>();
		for (int i = 0; i < listSize.getValue() / CurveName.BYTES; ++i) {
			Int16 value = Int16.from(is);
			CurveName curveName = CurveName.fromValue(value.getValue());
			groups.add(curveName);
		}

		return new SupportedGroups(type, length, listSize, groups);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(size());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(listSize.toByteArray());
		for (CurveName group : groups) {
			buffer.put(group.toByteArray());
		}
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		int result = type.size() + length.size() + listSize.size();
		for (CurveName group : groups) {
			result += group.size();
		}
		return result;
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}

	public Int16 getListSize() {
		return listSize;
	}

	public List<CurveName> getGroups() {
		return groups;
	}
}

package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class SupportedGroups implements Sizeable {

	private ExtensionType type = ExtensionType.SUPPORTED_GROUPS;

	private Int16 size;

	private Int16 listSize;

	private List<CurveName> groups;

	public SupportedGroups(final CurveName... curveNames) {
		this.listSize = new Int16((short) (curveNames.length * CurveName.BYTES));
		this.size = new Int16((short) (listSize.getValue() + listSize.size()));
		this.groups = List.of(curveNames);
	}

	public SupportedGroups() {
		this(CurveName.values());
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeShort(type.value());
		dos.write(size.toByteArray());
		dos.write(listSize.toByteArray());
		for (CurveName group : groups) {
			dos.writeShort(group.value());
		}

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		int result = type.size() + size.size() + listSize.size();
		for (CurveName group : groups) {
			result += group.size();
		}
		return result;
	}

	public ExtensionType getType() {
		return type;
	}

	public Int16 getSize() {
		return size;
	}

	public Int16 getListSize() {
		return listSize;
	}

	public List<CurveName> getGroups() {
		return groups;
	}
}

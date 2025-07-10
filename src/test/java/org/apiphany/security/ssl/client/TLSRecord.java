package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.ThrowingRunnable;

public class TLSRecord implements TLSObject {

	private RecordHeader header;

	private List<TLSObject> messages;

	public TLSRecord(RecordHeader header, List<TLSObject> messages, boolean setLength) {
		this.header = header;
		this.messages = messages;
		if (setLength) {
			short length = 0;
			for (Sizeable message : messages) {
				length += message.size();
			}
			header.getLength().setValue(length);
		}
	}

	public TLSRecord(RecordHeader header, List<TLSObject> messages) {
		this(header, messages, true);
	}

	public static TLSRecord from(InputStream is) throws IOException {
		RecordHeader header = RecordHeader.from(is);
		RecordType recordType = header.getType();

		List<TLSObject> messages = new ArrayList<>();
		int currentLength = header.getLength().getValue();
		while (currentLength > 0) {
			TLSObject message = recordType.message().from(is);
			messages.add(message);
			currentLength -= message.size();
		}
		return new TLSRecord(header, messages, false);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(header.toByteArray());
			for (TLSObject message : messages) {
				dos.write(message.toByteArray());
			}
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		int result = header.size();
		for (Sizeable message : messages) {
			result += message.size();
		}
		return result;
	}

	public RecordHeader getHeader() {
		return header;
	}

	public List<TLSObject> getMessages() {
		return messages;
	}

	public HandshakeMessage getHandshakeMessage(int index) {
		return JavaObjects.cast(messages.get(index));
	}
}

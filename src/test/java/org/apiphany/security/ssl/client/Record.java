package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Record implements Sizeable {

	private RecordHeader header;

	private List<Handshake> handshakes;

	public Record(RecordHeader header, List<Handshake> handshakes) {
		this.header = header;
		this.handshakes = handshakes;
	}

	public static Record from(InputStream is) throws IOException {
		RecordHeader header = RecordHeader.from(is);

		List<Handshake> handshakes = new ArrayList<>();
		int currentLength = header.getLength().getValue();
		while (currentLength > 0) {
			Handshake handshake = Handshake.from(is);
			currentLength -= handshake.size();
		}

		return new Record(header, handshakes);
	}

	@Override
	public int size() {
		int result = header.size();
		for (Handshake handshake : handshakes) {
			result += handshake.size();
		}
		return result;
	}

	public RecordHeader getHeader() {
		return header;
	}

	public List<Handshake> getHandshakes() {
		return handshakes;
	}
}

package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class Extensions implements TLSObject {

	private Int16 length;

	private List<Extension> extensions = new ArrayList<>();

	public Extensions(final Int16 length, final List<Extension> extensions, final boolean setSizes) {
		this.length = length;
		this.extensions.addAll(extensions);
		if (setSizes) {
			this.length.setValue((short) (size() - length.size()));
		}
	}

	public Extensions(
			final Int16 length,
			final List<Extension> extensions) {
		this(length, extensions, true);
	}

	public Extensions(final List<String> serverNames, final List<CurveName> curveNames) {
		this(
				new Int16(),
				List.of(
						new ServerNames(serverNames),
						new StatusRequest(),
						new SupportedGroups(curveNames),
						new ECPointFormats(),
						new SignatureAlgorithms(),
						new RenegotiationInfo(),
						new SignedCertificateTimestamp()
				)
		);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(length.toByteArray());
			for (Extension extension : extensions) {
				dos.write(extension.toByteArray());
			}
		}).run();
		return bos.toByteArray();
	}

	public static Extensions from(final InputStream is) throws IOException {
		Int16 length = Int16.from(is);

		List<Extension> extensions = new ArrayList<>();
		int currentLength = 0;
		while (currentLength < length.getValue()) {
			Int16 extensionType = Int16.from(is);
			ExtensionType type = ExtensionType.fromValue(extensionType.getValue());

			Extension extension = type.extensionFrom(is);
			extensions.add(extension);

			currentLength += extension.size();
		}

		return new Extensions(length, extensions, false);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		int result = length.size();
		for (Extension extension : extensions) {
			result += extension.size();
		}
		return result;
	}

	public Int16 getLength() {
		return length;
	}

	public List<Extension> getExtensions() {
		return extensions;
	}
}

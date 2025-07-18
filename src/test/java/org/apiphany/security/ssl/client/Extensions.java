package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;

public class Extensions implements TLSObject {

	private final Int16 length;

	private final List<TLSExtension> extensions = new ArrayList<>();

	public Extensions(final Int16 length, final List<TLSExtension> extensions, final boolean updateSize) {
		this.length = updateSize ? Int16.of((short) ByteSizeable.sizeOf(extensions)) : length;
		this.extensions.addAll(extensions);
	}

	public Extensions(
			final Int16 length,
			final List<TLSExtension> extensions) {
		this(length, extensions, true);
	}

	public Extensions(final List<String> serverNames, final List<CurveName> curveNames, final List<SignatureAlgorithm> signatureAlgorithms) {
		this(
				Int16.ZERO,
				List.of(
						new ServerNames(serverNames),
						new StatusRequest(),
						new SupportedGroups(curveNames),
						new ECPointFormats(),
						new SignatureAlgorithms(signatureAlgorithms),
						new RenegotiationInfo(),
						new SignedCertificateTimestamp()
				)
		);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		for (TLSExtension extension : extensions) {
			buffer.put(extension.toByteArray());
		}
		return buffer.array();
	}

	public static Extensions from(final InputStream is) throws IOException {
		Int16 length = Int16.from(is);

		List<TLSExtension> extensions = new ArrayList<>();
		int currentLength = 0;
		while (currentLength < length.getValue()) {
			Int16 int16 = Int16.from(is);
			ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

			TLSExtension extension = extensionType.extensionFrom(is);
			extensions.add(extension);

			currentLength += extension.sizeOf();
		}

		return new Extensions(length, extensions, false);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return length.sizeOf() + ByteSizeable.sizeOf(extensions);
	}

	public Int16 getLength() {
		return length;
	}

	public List<TLSExtension> getExtensions() {
		return extensions;
	}
}

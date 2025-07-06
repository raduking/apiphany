package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class Extensions implements Sizeable {

	private static final int EXTENSIONS_SIZE_INDEX = 0;

	private Int16 length = new Int16();

	private ServerNames serverNames;

	private StatusRequest statusRequest = new StatusRequest();

	private SupportedGroups supportedGroups = new SupportedGroups();

	private ECPointFormats ecPointFormats = new ECPointFormats();

	private SignatureAlgorithms signatureAlgorithms = new SignatureAlgorithms();

	private RenegotiationInfo renegotiationInfo = new RenegotiationInfo();

	private SignedCertificateTimestamp signedCertificateTimestamp = new SignedCertificateTimestamp();

	public Extensions(final List<String> serverNames) {
		this.serverNames = new ServerNames(serverNames);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(length.toByteArray());
		dos.write(serverNames.toByteArray());
		dos.write(statusRequest.toByteArray());
		dos.write(supportedGroups.toByteArray());
		dos.write(ecPointFormats.toByteArray());
		dos.write(signatureAlgorithms.toByteArray());
		dos.write(renegotiationInfo.toByteArray());
		dos.write(signedCertificateTimestamp.toByteArray());

		byte[] bytes = bos.toByteArray();

		// write actual size
		short extensionsSize = (short) (bytes.length - length.size());
		Bytes.set(extensionsSize, bytes, EXTENSIONS_SIZE_INDEX);

		return bytes;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return length.size()
				+ serverNames.size()
				+ statusRequest.size()
				+ supportedGroups.size()
				+ ecPointFormats.size()
				+ signatureAlgorithms.size()
				+ renegotiationInfo.size()
				+ signedCertificateTimestamp.size();
	}

	public Int16 getLength() {
		return length;
	}

	public ServerNames getServerNames() {
		return serverNames;
	}

	public StatusRequest getStatusRequest() {
		return statusRequest;
	}

	public SupportedGroups getSupportedGroups() {
		return supportedGroups;
	}

	public ECPointFormats getEcPointFormats() {
		return ecPointFormats;
	}

	public SignatureAlgorithms getSignatureAlgorithms() {
		return signatureAlgorithms;
	}

	public RenegotiationInfo getRenegotiationInfo() {
		return renegotiationInfo;
	}

	public SignedCertificateTimestamp getSignedCertificateTimestamp() {
		return signedCertificateTimestamp;
	}
}

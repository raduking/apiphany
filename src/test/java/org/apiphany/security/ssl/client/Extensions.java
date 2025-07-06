package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class Extensions implements Sizeable {

	private Int16 length = new Int16();

	private ServerNames serverNames;

	private StatusRequest statusRequest = new StatusRequest();

	private SupportedGroups supportedGroups = new SupportedGroups();

	private ECPointFormats ecPointFormats = new ECPointFormats();

	private SignatureAlgorithms signatureAlgorithms = new SignatureAlgorithms();

	private RenegotiationInfo renegotiationInfo = new RenegotiationInfo();

	private SignedCertificateTimestamp signedCertificateTimestamp = new SignedCertificateTimestamp();

	public Extensions(
			final Int16 length,
			final ServerNames serverNames,
			final StatusRequest statusRequest,
			final SupportedGroups supportedGroups,
			final ECPointFormats ecPointFormats,
			final SignatureAlgorithms signatureAlgorithms,
			final RenegotiationInfo renegotiationInfo,
			final SignedCertificateTimestamp signedCertificateTimestamp,
			final boolean setSizes) {
		this.length = length;
		this.serverNames = serverNames;
		this.statusRequest = statusRequest;
		this.supportedGroups = supportedGroups;
		this.ecPointFormats = ecPointFormats;
		this.signatureAlgorithms = signatureAlgorithms;
		this.renegotiationInfo = renegotiationInfo;
		this.signedCertificateTimestamp = signedCertificateTimestamp;
		if (setSizes) {
			this.length.setValue((short) (size() - length.size()));
		}
	}

	public Extensions(
			final Int16 length,
			final ServerNames serverNames,
			final StatusRequest statusRequest,
			final SupportedGroups supportedGroups,
			final ECPointFormats ecPointFormats,
			final SignatureAlgorithms signatureAlgorithms,
			final RenegotiationInfo renegotiationInfo,
			final SignedCertificateTimestamp signedCertificateTimestamp) {
		this(
				length,
				serverNames,
				statusRequest,
				supportedGroups,
				ecPointFormats,
				signatureAlgorithms,
				renegotiationInfo,
				signedCertificateTimestamp,
				true
		);
	}

	public Extensions(final List<String> serverNames) {
		this(
				new Int16(),
				new ServerNames(serverNames),
				new StatusRequest(),
				new SupportedGroups(),
				new ECPointFormats(),
				new SignatureAlgorithms(),
				new RenegotiationInfo(),
				new SignedCertificateTimestamp()
		);
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

		return bos.toByteArray();
	}

	public static Extensions from(final InputStream is) throws IOException {
		Int16 length = Int16.from(is);
		ServerNames serverNames = ServerNames.from(is);
		StatusRequest statusRequest = new StatusRequest();
		SupportedGroups supportedGroups = new SupportedGroups();
		ECPointFormats ecPointFormats = new ECPointFormats();
		SignatureAlgorithms signatureAlgorithms = new SignatureAlgorithms();
		RenegotiationInfo renegotiationInfo = new RenegotiationInfo();
		SignedCertificateTimestamp signedCertificateTimestamp = new SignedCertificateTimestamp();

		return new Extensions(
				length, serverNames, statusRequest, supportedGroups, ecPointFormats,
				signatureAlgorithms, renegotiationInfo, signedCertificateTimestamp, false);
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

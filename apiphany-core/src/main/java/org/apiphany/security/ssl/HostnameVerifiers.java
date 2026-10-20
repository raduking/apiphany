package org.apiphany.security.ssl;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.HostnameVerifier;

import org.morphix.lang.collections.Iterables;
import org.morphix.lang.function.ThrowingSupplier;
import org.morphix.reflection.Constructors;

/**
 * Factory methods for hostname verifiers backed by X.509 certificates.
 *
 * @author Radu Sebastian LAZIN
 */
public final class HostnameVerifiers {

	/**
	 * subjectAltName type value for dNSName entries.
	 */
	private static final int DNS_NAME_TYPE = 2;

	/**
	 * subjectAltName type value for iPAddress entries.
	 */
	private static final int IP_ADDRESS_TYPE = 7;

	/**
	 * Hide constructor.
	 */
	private HostnameVerifiers() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns a hostname verifier that matches subjectAltNames first and falls back to the certificate common name,
	 * mirroring the behavior of Apache HttpClient 5's default hostname verifier.
	 * <p>
	 * The common name is ignored when a DNS subjectAltName is present, as required by RFC 2818 / RFC 6125.
	 *
	 * @return a hostname verifier with CN fallback
	 */
	public static HostnameVerifier defaultVerifier() {
		return (hostname, session) -> ThrowingSupplier.unchecked(() -> {
			Certificate[] certificateChain = session.getPeerCertificates();
			if (0 == certificateChain.length || !(certificateChain[0] instanceof X509Certificate certificate)) {
				return false;
			}
			if (matchesSubjectAltName(hostname, certificate)) {
				return true;
			}
			if (hasDnsSubjectAltNames(certificate)) {
				return false;
			}
			return matchesCommonName(hostname, certificate);
		}).get();
	}

	/**
	 * Returns true if the given hostname matches one of the certificate's subjectAltNames, false otherwise.
	 *
	 * @param hostname the hostname to verify
	 * @param certificate the certificate to check
	 * @return true if the hostname matches a subjectAltName, false otherwise
	 * @throws CertificateParsingException when parsing the certificate fails
	 */
	private static boolean matchesSubjectAltName(final String hostname, final X509Certificate certificate) throws CertificateParsingException {
		Collection<List<?>> subjectAltNames = certificate.getSubjectAlternativeNames();
		for (List<?> subjectAltName : Iterables.safe(subjectAltNames)) {
			if (2 > subjectAltName.size()) {
				continue;
			}
			Object type = subjectAltName.get(0);
			Object value = subjectAltName.get(1);
			if (value instanceof String sanValue && type instanceof Integer typeValue
					&& (DNS_NAME_TYPE == typeValue || IP_ADDRESS_TYPE == typeValue)
					&& matchesHostName(sanValue, hostname)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the certificate contains at least one subjectAltName of type {@code dNSName}, false otherwise. Used
	 * to decide whether falling back to the certificate common name is allowed: per RFC 2818 / RFC 6125 the common name
	 * must be ignored whenever DNS subject alternative names are present.
	 *
	 * @param certificate the certificate to check
	 * @return true if the certificate has at least one DNS subjectAltName, false otherwise
	 * @throws CertificateParsingException when parsing the certificate fails
	 */
	private static boolean hasDnsSubjectAltNames(final X509Certificate certificate) throws CertificateParsingException {
		Collection<List<?>> subjectAltNames = certificate.getSubjectAlternativeNames();
		for (List<?> subjectAltName : Iterables.safe(subjectAltNames)) {
			if (2 > subjectAltName.size()) {
				continue;
			}
			Object type = subjectAltName.getFirst();
			if (type instanceof Integer typeValue && DNS_NAME_TYPE == typeValue) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the given hostname matches the certificate's common name, false otherwise.
	 *
	 * @param hostname the hostname to verify
	 * @param certificate the certificate to check
	 * @return true if the hostname matches the common name, false otherwise
	 */
	private static boolean matchesCommonName(final String hostname, final X509Certificate certificate) {
		String subjectDn = certificate.getSubjectX500Principal().getName();
		for (String dnPart : subjectDn.split(",")) {
			String trimmed = dnPart.trim();
			if (trimmed.regionMatches(true, 0, "CN=", 0, 3)) {
				String commonName = trimmed.substring(3);
				return matchesHostName(commonName, hostname);
			}
		}
		return false;
	}

	/**
	 * Returns true if the given certificate name matches the requested hostname, false otherwise. Supports the common
	 * wildcard form <code>*.&lt;domain&gt;</code>.
	 *
	 * @param certificateName name from the certificate, may contain a wildcard
	 * @param hostname the requested hostname
	 * @return true if the names match, false otherwise
	 */
	private static boolean matchesHostName(final String certificateName, final String hostname) {
		if (certificateName.startsWith("*.")) {
			String wildcardDomain = certificateName.substring(1);
			if (hostname.length() <= wildcardDomain.length()
					|| !hostname.regionMatches(true, hostname.length() - wildcardDomain.length(), wildcardDomain, 0, wildcardDomain.length())) {
				return false;
			}
			String prefix = hostname.substring(0, hostname.length() - wildcardDomain.length());
			return !prefix.contains(".");
		}
		return certificateName.equalsIgnoreCase(hostname);
	}
}

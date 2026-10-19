package org.apiphany.security.ssl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link HostnameVerifiers}.
 *
 * @author Radu Sebastian LAZIN
 */
class HostnameVerifiersTest {

	@Test
	void shouldRejectEmptyAndNonX509CertificateChains() {
		assertFalse(verify("example.com"));
		assertFalse(verify("example.com", mock(Certificate.class)));
	}

	@Test
	void shouldMatchDnsAndIpSubjectAlternativeNames() throws CertificateParsingException {
		X509Certificate dnsCertificate = certificate(null, List.of(List.of(2, "example.com")));
		X509Certificate ipCertificate = certificate(null, List.of(List.of(7, "127.0.0.1")));

		assertTrue(verify("example.com", dnsCertificate));
		assertTrue(verify("127.0.0.1", ipCertificate));
	}

	@Test
	void shouldIgnoreMalformedAndUnsupportedSubjectAlternativeNames() throws CertificateParsingException {
		Collection<List<?>> subjectAltNames = List.<List<?>>of(
				List.of(2),
				List.of(2, 123),
				List.of("invalid-type", "example.com"),
				List.of(999, "example.com"));
		X509Certificate certificate = certificate("CN=other.example.com", subjectAltNames);

		assertFalse(verify("example.com", certificate));
	}

	@Test
	void shouldNotFallBackToCommonNameWhenDnsSubjectAlternativeNameExists() throws CertificateParsingException {
		X509Certificate certificate = certificate("CN=example.com", List.of(List.of(2, "other.example.com")));

		assertFalse(verify("example.com", certificate));
	}

	@Test
	void shouldMatchCommonNameAndWildcards() throws CertificateParsingException {
		X509Certificate exactCertificate = certificate("CN=example.com", null);
		X509Certificate wildcardCertificate = certificate("CN=unused", List.of(List.of(2, "*.example.com")));

		assertTrue(verify("example.com", exactCertificate));
		assertFalse(verify("other.example.com", exactCertificate));
		assertTrue(verify("api.example.com", wildcardCertificate));
		assertFalse(verify("api.internal.example.com", wildcardCertificate));
		assertFalse(verify("example.com", wildcardCertificate));
		assertFalse(verify("api.other.com", wildcardCertificate));
	}

	@Test
	void shouldReturnFalseWhenCertificateHasNoCommonName() throws CertificateParsingException {
		X509Certificate certificate = certificate("O=Example", null);

		assertFalse(verify("example.com", certificate));
	}

	@Test
	void shouldPropagateCertificateParsingFailure() throws CertificateParsingException {
		X509Certificate certificate = mock(X509Certificate.class);
		when(certificate.getSubjectAlternativeNames()).thenThrow(new CertificateParsingException("invalid certificate"));

		assertThrows(CertificateParsingException.class, () -> verify("example.com", certificate));
	}

	private static X509Certificate certificate(final String subject, final Collection<List<?>> subjectAltNames)
			throws CertificateParsingException {
		X509Certificate certificate = mock(X509Certificate.class);
		doReturn(subjectAltNames).when(certificate).getSubjectAlternativeNames();
		when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal(null == subject ? "CN=unused" : subject));
		return certificate;
	}

	private static boolean verify(final String hostname, final Certificate... certificates) {
		SSLSession session = mock(SSLSession.class);
		try {
			doReturn(certificates).when(session).getPeerCertificates();
		} catch (SSLPeerUnverifiedException e) {
			throw new AssertionError(e);
		}
		return HostnameVerifiers.defaultVerifier().verify(hostname, session);
	}
}

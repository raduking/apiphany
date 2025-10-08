package org.apiphany.security;

import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Map;

import org.morphix.lang.Enums;

/**
 * Enumeration of standard JWS (JSON Web Signature) algorithms defined by
 * <a href="https://www.rfc-editor.org/rfc/rfc7518">RFC 7518</a> and related JOSE specifications.
 * <p>
 * Each enum constant provides its compact JWS {@code alg} name (e.g. {@code "RS256"}) and maps to a corresponding
 * {@link JcaSignatureAlgorithm} representing the underlying JCA algorithm.
 * </p>
 *
 * <h2>Mapping Notes</h2>
 * <ul>
 * <li>{@code RSxxx} → RSA PKCS#1 v1.5 using SHA-xxx</li>
 * <li>{@code PSxxx} → RSA-PSS using SHA-xxx</li>
 * <li>{@code ESxxx} → ECDSA with curve size matching the hash</li>
 * <li>{@code EdDSA} → Ed25519 or Ed448</li>
 * </ul>
 *
 * @see JcaSignatureAlgorithm
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7518#section-3.1">RFC 7518 §3.1 — Signature Algorithms</a>
 */
public enum JwsAlgorithm {

	/**
	 * RSA PKCS#1 v1.5 signature using SHA-256. Most common RSA JWS algorithm.
	 */
	RS256("RS256", JcaSignatureAlgorithm.SHA256_WITH_RSA),

	/**
	 * RSA PKCS#1 v1.5 signature using SHA-384.
	 */
	RS384("RS384", JcaSignatureAlgorithm.SHA384_WITH_RSA),

	/**
	 * RSA PKCS#1 v1.5 signature using SHA-512.
	 */
	RS512("RS512", JcaSignatureAlgorithm.SHA512_WITH_RSA),

	/**
	 * ECDSA signature using P-256 curve and SHA-256.
	 */
	ES256("ES256", JcaSignatureAlgorithm.SHA256_WITH_ECDSA),

	/**
	 * ECDSA signature using P-384 curve and SHA-384.
	 */
	ES384("ES384", JcaSignatureAlgorithm.SHA384_WITH_ECDSA),

	/**
	 * ECDSA signature using P-521 curve and SHA-512.
	 */
	ES512("ES512", JcaSignatureAlgorithm.SHA512_WITH_ECDSA),

	/**
	 * RSA-PSS signature using SHA-256 and MGF1(SHA-256).
	 */
	PS256("PS256", JcaSignatureAlgorithm.RSASSA_PSS,
			new PSSParameterSpec(MessageDigestAlgorithm.SHA256.value(), "MGF1", MGF1ParameterSpec.SHA256, 32, 1)),

	/**
	 * RSA-PSS signature using SHA-384 and MGF1(SHA-384).
	 */
	PS384("PS384", JcaSignatureAlgorithm.RSASSA_PSS,
			new PSSParameterSpec(MessageDigestAlgorithm.SHA384.value(), "MGF1", MGF1ParameterSpec.SHA384, 48, 1)),

	/**
	 * RSA-PSS signature using SHA-512 and MGF1(SHA-512).
	 */
	PS512("PS512", JcaSignatureAlgorithm.RSASSA_PSS,
			new PSSParameterSpec(MessageDigestAlgorithm.SHA512.value(), "MGF1", MGF1ParameterSpec.SHA512, 64, 1)),

	/**
	 * Edwards-curve digital signature algorithm (Ed25519).
	 */
	EDDSA("EdDSA", JcaSignatureAlgorithm.ED25519);

	/**
	 * The name map for easy {@link #fromValue(String)} implementation.
	 */
	private static final Map<String, JwsAlgorithm> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The JWS algorithm string.
	 */
	private final String value;

	/**
	 * The JCA signature algorithm associated.
	 */
	private final JcaSignatureAlgorithm jcaAlgorithm;

	/**
	 * The PSS parameter specification for RSASSA-PSS.
	 */
	private final PSSParameterSpec pssParameterSpec;

	/**
	 * Constructor.
	 *
	 * @param value the algorithm string value
	 * @param jcaAlgorithm the JCA algorithm
	 */
	JwsAlgorithm(final String value, final JcaSignatureAlgorithm jcaAlgorithm) {
		this(value, jcaAlgorithm, null);
	}

	/**
	 * Constructor.
	 *
	 * @param value the algorithm string value
	 * @param jcaAlgorithm the JCA algorithm
	 *
	 */
	JwsAlgorithm(final String value, final JcaSignatureAlgorithm jcaAlgorithm, final PSSParameterSpec pssSpec) {
		this.value = value;
		this.jcaAlgorithm = jcaAlgorithm;
		this.pssParameterSpec = pssSpec;
	}

	/**
	 * Returns the JWS algorithm name (e.g. {@code "RS256"}).
	 *
	 * @return the compact JWS algorithm identifier
	 */
	public String value() {
		return value;
	}

	/**
	 * Returns the corresponding {@link JcaSignatureAlgorithm}.
	 *
	 * @return the mapped JCA signature algorithm
	 */
	public JcaSignatureAlgorithm jcaAlgorithm() {
		return jcaAlgorithm;
	}

	/**
	 * Returns the PSS parameter specification.
	 *
	 * @return the PSS parameter specification
	 */
	public PSSParameterSpec pssParameterSpec() {
		return pssParameterSpec;
	}

	/**
	 * Resolves a {@link JwsAlgorithm} from its JWS {@code alg} name.
	 *
	 * @param algorithm the JWS algorithm name (e.g. {@code "RS256"})
	 * @return the corresponding {@link JwsAlgorithm}
	 */
	public static JwsAlgorithm fromString(final String algorithm) {
		return Enums.fromString(algorithm, NAME_MAP, values());
	}

	/**
	 * @see #toString()
	 */
	@Override
	public String toString() {
		return value();
	}
}

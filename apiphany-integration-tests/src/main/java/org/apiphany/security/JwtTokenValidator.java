package org.apiphany.security;

import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Utility class for validating JWT tokens.
 *
 * @author Radu Sebastian LAZIN
 */
public class JwtTokenValidator {

	/**
	 * Client ID expected in the token's subject claim.
	 */
	private final String clientId;

	/**
	 * Secret key used for MAC signature verification.
	 */
	private final byte[] secretKey;

	/**
	 * Expected issuer claim value.
	 */
	private final String expectedIssuer;

	/**
	 * Creates a JWT token validator with only the client secret.
	 *
	 * @param clientSecret the client secret used for signature verification
	 */
	public JwtTokenValidator(final String clientSecret) {
		this(null, clientSecret, null);
	}

	/**
	 * Creates a JWT token validator with the client ID, client secret, and expected issuer.
	 *
	 * @param clientId the client ID expected in the token's subject claim
	 * @param clientSecret the client secret used for signature verification
	 * @param expectedIssuer the expected issuer claim value
	 */
	public JwtTokenValidator(final String clientId, final String clientSecret, final String expectedIssuer) {
		if (clientSecret.length() < 32) {
			throw new IllegalArgumentException("Client secret must be at least 32 characters long");
		}
		this.secretKey = Arrays.copyOf(
				clientSecret.getBytes(StandardCharsets.UTF_8),
				clientSecret.length());
		this.clientId = clientId;
		this.expectedIssuer = expectedIssuer;
	}

	/**
	 * Validates the given JWT token and returns its claims.
	 *
	 * @param token the JWT token to validate
	 * @return the claims contained in the token
	 * @throws TokenValidationException if the token is invalid or cannot be validated
	 */
	public JWTClaimsSet validateToken(final String token) throws TokenValidationException {
		return validateToken(token, true);
	}

	/**
	 * Validates the given JWT token and returns its claims.
	 *
	 * @param token the JWT token to validate
	 * @param claimsValidationEnabled whether to perform standard claims validation
	 * @return the claims contained in the token
	 * @throws TokenValidationException if the token is invalid or cannot be validated
	 */
	public JWTClaimsSet validateToken(final String token, final boolean claimsValidationEnabled) throws TokenValidationException {
		try {
			// 1. Parse the token structure
			SignedJWT signedJWT = SignedJWT.parse(token);

			// 2. Verify the signature
			if (!signedJWT.verify(new MACVerifier(secretKey))) {
				throw new TokenValidationException("Invalid token signature");
			}

			// 3. Get claims
			JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

			// 4. Validate standard claims if enabled
			if (claimsValidationEnabled) {
				validateClaims(claims);
			}

			return claims;
		} catch (ParseException e) {
			throw new TokenValidationException("Malformed token", e);
		} catch (JOSEException e) {
			throw new TokenValidationException("Token verification failed", e);
		}
	}

	/**
	 * Validates standard JWT claims.
	 *
	 * @param claims the JWT claims to validate
	 * @throws TokenValidationException if any claim is invalid
	 */
	private void validateClaims(final JWTClaimsSet claims) throws TokenValidationException {
		// 1. Check expiration
		Date expirationTime = claims.getExpirationTime();
		if (null == expirationTime) {
			throw new TokenValidationException("Missing expiration claim");
		}
		if (expirationTime.before(new Date())) {
			throw new TokenValidationException("Token has expired");
		}

		// 2. Validate issuer
		String issuer = claims.getIssuer();
		if (null == issuer || !issuer.equals(expectedIssuer)) {
			throw new TokenValidationException("Invalid token issuer");
		}

		// 3. Check JWT ID (optional but recommended)
		if (null == claims.getJWTID()) {
			throw new TokenValidationException("Missing JWT ID claim");
		}

		// 4. Validate subject exists (since we're setting it in generation)
		String subject = claims.getSubject();
		if (null == subject || !subject.equals(clientId)) {
			throw new TokenValidationException("Missing subject claim");
		}
	}

	/**
	 * Exception thrown when token validation fails.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class TokenValidationException extends Exception {

		/**
		 * Serial version UID.
		 */
		@Serial
		private static final long serialVersionUID = -6786082918908973060L;

		/**
		 * Constructs a new {@link TokenValidationException} with the specified message.
		 *
		 * @param message the exception message
		 */
		public TokenValidationException(final String message) {
			super(message);
		}

		/**
		 * Constructs a new {@link TokenValidationException} with the specified message and cause.
		 *
		 * @param message the exception message
		 * @param cause the cause of the exception
		 */
		public TokenValidationException(final String message, final Throwable cause) {
			super(message, cause);
		}
	}
}

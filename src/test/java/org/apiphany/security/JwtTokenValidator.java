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

public class JwtTokenValidator {

	private final String clientId;
    private final byte[] secretKey;
    private final String expectedIssuer;

    public JwtTokenValidator(final String clientSecret) {
    	this(null, clientSecret, null);
    }

    public JwtTokenValidator(final String clientId, final String clientSecret, final String expectedIssuer) {
        if (clientSecret.length() < 32) {
            throw new IllegalArgumentException("Client secret must be at least 32 characters long");
        }
        this.secretKey = Arrays.copyOf(
            clientSecret.getBytes(StandardCharsets.UTF_8),
            clientSecret.length()
        );
        this.clientId = clientId;
        this.expectedIssuer = expectedIssuer;
    }

    public JWTClaimsSet validateToken(final String token) throws TokenValidationException {
    	return validateToken(token, true);
    }

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

    public static class TokenValidationException extends Exception {

        @Serial
        private static final long serialVersionUID = -6786082918908973060L;

		public TokenValidationException(final String message) {
            super(message);
        }

        public TokenValidationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

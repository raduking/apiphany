package org.apiphany.security.client;

import java.util.function.Consumer;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.security.oauth2.client.OAuth2HttpExchangeClientBuilder;
import org.apiphany.security.ssl.client.SSLHttpExchangeClientBuilder;
import org.morphix.lang.function.Consumers;
import org.morphix.lang.resource.ScopedResource;

/**
 * Secured exchange client builder.
 *
 * @author Radu Sebastian LAZIN
 */
public class SecuredExchangeClientBuilder extends ExchangeClientBuilder {

	/**
	 * Indicates whether a security mechanism was defined.
	 */
	private boolean securityDefined = false;

	/**
	 * Hide constructor.
	 */
	private SecuredExchangeClientBuilder() {
		// empty
	}

	/**
	 * Creates a secured exchange client builder.
	 *
	 * @return the secured exchange client builder
	 */
	public static SecuredExchangeClientBuilder create() {
		return new SecuredExchangeClientBuilder();
	}

	/**
	 * This builder always requires a security mechanism to be defined.
	 *
	 * @return never returns normally
	 * @throws IllegalStateException always thrown to indicate no security mechanism was defined
	 */
	@Override
	public ScopedResource<ExchangeClient> build() {
		if (securityDefined) {
			return super.build();
		}
		throw new IllegalStateException("Client not secured with any mechanism");
	}

	/**
	 * This builder always requires a security mechanism to be defined.
	 *
	 * @param clientClass exchange client class
	 * @return never returns normally
	 * @throws IllegalStateException always thrown to indicate no security mechanism was defined
	 */
	@Override
	public SecuredExchangeClientBuilder client(final Class<? extends ExchangeClient> clientClass) {
		throw new IllegalStateException("Cannot set exchange client class when securing an existing client");
	}

	/**
	 * This builder always requires a security mechanism to be defined.
	 *
	 * @param client exchange client
	 * @return never returns normally
	 * @throws IllegalStateException always thrown to indicate no security mechanism was defined
	 */
	@Override
	public SecuredExchangeClientBuilder client(final ExchangeClient client) {
		throw new IllegalStateException("Cannot set exchange client when securing an existing client");
	}

	/**
	 * Marks that a security mechanism was defined.
	 *
	 * @return this builder
	 */
	private SecuredExchangeClientBuilder securityDefined() {
		this.securityDefined = true;
		return this;
	}

	/**
	 * Adds OAuth2 functionality.
	 *
	 * @param oAuth2BuilderCustomizer OAuth2 customizer
	 * @return new OAuth2 exchange client builder
	 */
	public OAuth2HttpExchangeClientBuilder oAuth2(final Consumer<OAuth2HttpExchangeClientBuilder> oAuth2BuilderCustomizer) {
		return securityDefined().decoratedWithBuilder(OAuth2HttpExchangeClientBuilder.class, oAuth2BuilderCustomizer);
	}

	/**
	 * Adds OAuth2 functionality.
	 *
	 * @return new OAuth2 exchange client builder
	 */
	public OAuth2HttpExchangeClientBuilder oAuth2() {
		return oAuth2(Consumers.noConsumer());
	}

	/**
	 * Adds SSL/TLS functionality. Configures one-way TLS (server certificate validation).
	 *
	 * @param sslBuilderCustomizer SSL builder customizer
	 * @return new SSL exchange client builder
	 */
	public SSLHttpExchangeClientBuilder ssl(final Consumer<SSLHttpExchangeClientBuilder> sslBuilderCustomizer) {
		return securityDefined().decoratedWithBuilder(SSLHttpExchangeClientBuilder.class, sslBuilderCustomizer);
	}

	/**
	 * Adds SSL/TLS functionality. Configures one-way TLS (server certificate validation).
	 *
	 * @return new SSL exchange client builder
	 */
	public SSLHttpExchangeClientBuilder ssl() {
		return ssl(Consumers.noConsumer());
	}

	/**
	 * Adds mutual TLS functionality. Requires a key store (client certificate) to be configured.
	 *
	 * @param mtlsBuilderCustomizer mTLS builder customizer
	 * @return new SSL exchange client builder
	 */
	public SSLHttpExchangeClientBuilder mtls(final Consumer<SSLHttpExchangeClientBuilder> mtlsBuilderCustomizer) {
		return ssl(mtlsBuilderCustomizer.andThen(SSLHttpExchangeClientBuilder::requireKeystore));
	}
}

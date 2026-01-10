package org.apiphany.security.client;

import java.util.function.Consumer;

import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.security.oauth2.client.OAuth2HttpExchangeClientBuilder;
import org.morphix.lang.function.Consumers;

/**
 * Secured exchange client builder.
 *
 * @author Radu Sebastian LAZIN
 */
public class SecuredExchangeClientBuilder extends ExchangeClientBuilder {

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
	 * Adds OAuth2 functionality.
	 *
	 * @param oAuth2BuilderCustomizer OAuth2 customizer
	 * @return new OAuth2 exchange client builder
	 */
	public OAuth2HttpExchangeClientBuilder oAuth2(final Consumer<OAuth2HttpExchangeClientBuilder> oAuth2BuilderCustomizer) {
		return decorateWithBuilder(OAuth2HttpExchangeClientBuilder.class, oAuth2BuilderCustomizer);
	}

	/**
	 * Adds OAuth2 functionality.
	 *
	 * @return new OAuth2 exchange client builder
	 */
	public OAuth2HttpExchangeClientBuilder oAuth2() {
		return oAuth2(Consumers.noConsumer());
	}
}

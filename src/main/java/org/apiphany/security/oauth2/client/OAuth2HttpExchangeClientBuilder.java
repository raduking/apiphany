package org.apiphany.security.oauth2.client;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.lang.ScopedResource;

/**
 * OAuth2 exchange clients builder.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2HttpExchangeClientBuilder extends ExchangeClientBuilder {

	/**
	 * Token exchange client class.
	 */
	private Class<? extends ExchangeClient> tokenExchangeClientClass;

	/**
	 * Token exchange client.
	 */
	private ExchangeClient tokenExchangeClient;

	/**
	 * The client registration name.
	 */
	private String registrationName;

	/**
	 * Hide constructor.
	 */
	private OAuth2HttpExchangeClientBuilder() {
		// empty
	}

	/**
	 * Creates a new OAuth2 exchange client builder.
	 *
	 * @return a new OAuth2 exchange client builder
	 */
	public static OAuth2HttpExchangeClientBuilder create() {
		return new OAuth2HttpExchangeClientBuilder();
	}

	/**
	 * Builds the OAuth2 exchange client based on the builder members.
	 * <p>
	 * This builder always creates an {@link OAuth2HttpExchangeClient} managed resource since it is a decorator over an
	 * existing {@link ExchangeClient} even if the underlying client is not managed.
	 *
	 * @return a new exchange client pair with life cycle management information
	 */
	@SuppressWarnings("resource")
	@Override
	public ScopedResource<ExchangeClient> build() {
		ScopedResource<ExchangeClient> clientResource = super.build();
		ScopedResource<ExchangeClient> tokenClientResource = clientResource;
		if (null != tokenExchangeClientClass || null != tokenExchangeClient) {
			tokenClientResource = ExchangeClientBuilder.create()
					.client(tokenExchangeClientClass)
					.client(tokenExchangeClient)
					.properties(clientProperties)
					.build();
		}
		ExchangeClient exchangeClient = new OAuth2HttpExchangeClient(clientResource, tokenClientResource, registrationName);
		return ScopedResource.managed(exchangeClient);
	}

	/**
	 * Sets the token exchange client class.
	 *
	 * @param tokenClientClass the token client class
	 * @return this
	 */
	public OAuth2HttpExchangeClientBuilder tokenClient(final Class<? extends ExchangeClient> tokenClientClass) {
		this.tokenExchangeClientClass = tokenClientClass;
		return this;
	}

	/**
	 * Sets the token exchange client.
	 *
	 * @param tokenClient the token client
	 * @return this
	 */
	public OAuth2HttpExchangeClientBuilder tokenClient(final ExchangeClient tokenClient) {
		this.tokenExchangeClient = tokenClient;
		return this;
	}

	/**
	 * Sets the client registration name.
	 *
	 * @param clientRegistrationName the client registration name
	 * @return this
	 */
	public OAuth2HttpExchangeClientBuilder registrationName(final String clientRegistrationName) {
		this.registrationName = clientRegistrationName;
		return this;
	}
}

package org.apiphany.security.oauth2.client;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.lang.ScopedResource;
import org.morphix.lang.JavaObjects;

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
		ExchangeClient exchangeClient = new OAuth2HttpExchangeClient(clientResource.unwrap(), tokenClientResource.unwrap());
		return ScopedResource.of(exchangeClient, clientResource.isManaged());
	}

	/**
	 * Sets the client properties.
	 *
	 * @param clientProperties client properties to set
	 * @return this
	 */
	@Override
	public OAuth2HttpExchangeClientBuilder properties(final ClientProperties clientProperties) {
		return JavaObjects.cast(super.properties(clientProperties));
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
}

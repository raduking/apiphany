package org.apiphany.security.oauth2.client;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.lang.Pair;
import org.morphix.lang.Nullables;

/**
 * OAuth2 exchange clients builder.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2ExchangeClientBuilder extends ExchangeClientBuilder {

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
	private OAuth2ExchangeClientBuilder() {
		// empty
	}

	/**
	 * Creates a new OAuth2 exchange client builder.
	 *
	 * @return a new OAuth2 exchange client builder
	 */
	public static OAuth2ExchangeClientBuilder create() {
		return new OAuth2ExchangeClientBuilder();
	}

	/**
	 * Builds the OAuth2 exchange client based on the builder members.
	 *
	 * @return a new exchange client pair with life cycle management information
	 */
	@SuppressWarnings("resource")
	@Override
	public Pair<ExchangeClient, Boolean> build() {
		Pair<ExchangeClient, Boolean> clientInfo = super.build();
		ExchangeClient exchangeClient = clientInfo.left();
		ExchangeClient tokenClient = Nullables.notNull(tokenExchangeClientClass)
				.thenYield(() -> ExchangeClientBuilder.create()
						.client(tokenExchangeClientClass)
						.client(tokenExchangeClient)
						.properties(clientProperties)
						.build()
						.left())
				.orElse(exchangeClient);
		return Pair.of(new OAuth2HttpExchangeClient(exchangeClient, tokenClient), clientInfo.right());
	}

	/**
	 * Sets the client properties.
	 *
	 * @param clientProperties client properties to set
	 * @return this
	 */
	@Override
	public OAuth2ExchangeClientBuilder properties(final ClientProperties clientProperties) {
		this.clientProperties = clientProperties;
		return this;
	}

	/**
	 * Sets the token exchange client class.
	 *
	 * @param tokenClientClass the token client class
	 * @return this
	 */
	public OAuth2ExchangeClientBuilder tokenClient(final Class<? extends ExchangeClient> tokenClientClass) {
		this.tokenExchangeClientClass = tokenClientClass;
		return this;
	}

	/**
	 * Sets the token exchange client.
	 *
	 * @param tokenClient the token client
	 * @return this
	 */
	public OAuth2ExchangeClientBuilder tokenClient(final ExchangeClient tokenClient) {
		this.tokenExchangeClient = tokenClient;
		return this;
	}

}

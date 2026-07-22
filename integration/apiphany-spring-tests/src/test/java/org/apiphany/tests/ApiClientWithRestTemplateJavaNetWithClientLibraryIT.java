package org.apiphany.tests;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.SpringRestClientProperties;
import org.apiphany.http.JavaNetHttpLibrary;

/**
 * Test class for {@link org.apiphany.client.http.RestTemplateExchangeClient} with explicit JavaNet client library
 * configuration. Extends the auto-detected JavaNet base class and adds the {@code clientLibrary} property override.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithRestTemplateJavaNetWithClientLibraryIT extends ApiClientWithRestTemplateJavaNetIT {

	@Override
	public ClientProperties clientProperties() {
		ClientProperties properties = super.clientProperties();
		properties.setCustomProperties(SpringRestClientProperties.ROOT, new SpringRestClientProperties() {
			{
				setClientLibrary(JavaNetHttpLibrary.CLIENT_NAME);
			}
		});
		return properties;
	}
}

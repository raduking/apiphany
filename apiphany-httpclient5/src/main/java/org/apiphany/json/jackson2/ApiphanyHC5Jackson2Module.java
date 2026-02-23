package org.apiphany.json.jackson2;

import java.io.Serial;

import org.apache.hc.core5.util.Timeout;
import org.apiphany.json.jackson2.serializers.TimeoutDeserializer;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Custom Jackson module to register serializers and deserializers for Apache HttpClient 5 types, such as
 * {@link Timeout}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiphanyHC5Jackson2Module extends SimpleModule {

	/**
	 * Serial version UID for serialization compatibility. This ensures that deserialization will work correctly even if the
	 * class definition changes,
	 */
	@Serial
	private static final long serialVersionUID = 8414312440407295220L;

	/**
	 * Name of the module, used for identification when registering with ObjectMapper.
	 */
	public static final String NAME = "apiphany-hc5-jackson2";

	/**
	 * Default constructor that initializes the module with its name.
	 */
	public ApiphanyHC5Jackson2Module() {
		super(NAME);
	}

	/**
	 * Overrides the setupModule method to register custom annotation introspector for handling Apache HttpClient 5 specific
	 * annotations.
	 *
	 * @param context the setup context provided by Jackson during module registration
	 */
	@Override
	public void setupModule(final SetupContext context) {
		super.setupModule(context);
		context.insertAnnotationIntrospector(ApiphanyJackson2AnnotationIntrospector.getInstance());
	}

	/**
	 * Returns a configured instance of the module with all necessary serializers and deserializers registered. This method
	 * uses lazy initialization to ensure that the module is only created when needed, and that it is thread-safe without
	 * requiring synchronization.
	 *
	 * @return a configured instance of {@link ApiphanyHC5Jackson2Module}
	 */
	public static SimpleModule instance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Holder class for lazy initialization of the module instance. This ensures that the module is only created when
	 * needed, and that it is thread-safe without requiring synchronization.
	 */
	private static class InstanceHolder {

		/**
		 * The singleton instance of the ApiphanyModule, initialized with all necessary serializers and deserializers.
		 */
		private static final SimpleModule INSTANCE = new ApiphanyHC5Jackson2Module()
				.addDeserializer(Timeout.class, new TimeoutDeserializer());
	}
}

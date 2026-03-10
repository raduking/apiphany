package org.apiphany.json.jackson3;

import java.io.Serial;

import org.apache.hc.core5.util.Timeout;
import org.apiphany.json.jackson3.serializers.TimeoutDeserializer;

import tools.jackson.databind.module.SimpleModule;

/**
 * Custom Jackson 3 module to register serializers and deserializers for Apache HTTP Client 5 types, such as
 * {@link Timeout}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiphanyHC5Jackson3Module extends SimpleModule {

	/**
	 * Serial version UID for serialization compatibility. This ensures that deserialization will work correctly even if the
	 * class definition changes,
	 */
	@Serial
	private static final long serialVersionUID = 5326130072870211174L;

	/**
	 * Name of the module, used for identification when registering with ObjectMapper.
	 */
	public static final String NAME = "apiphany-hc5-jackson3";

	/**
	 * Default constructor that initializes the module with its name.
	 */
	public ApiphanyHC5Jackson3Module() {
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
		context.insertAnnotationIntrospector(ApiphanyJackson3AnnotationIntrospector.getInstance());
	}

	/**
	 * Returns a configured instance of the module with all necessary serializers and deserializers registered. This method
	 * uses lazy initialization to ensure that the module is only created when needed, and that it is thread-safe without
	 * requiring synchronization.
	 *
	 * @return a configured instance of {@link ApiphanyHC5Jackson3Module}
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
		private static final SimpleModule INSTANCE = new ApiphanyHC5Jackson3Module()
				.addDeserializer(Timeout.class, new TimeoutDeserializer());
	}
}

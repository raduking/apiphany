package org.apiphany.json.jackson2;

import java.io.Serial;

import org.apiphany.RequestMethod;
import org.apiphany.json.jackson2.serializers.RequestMethodDeserializer;
import org.apiphany.json.jackson2.serializers.RequestMethodSerializer;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Custom Jackson module to register serializers and deserializers for Apiphany-specific types, such as
 * {@link RequestMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiphanyJackson2Module extends SimpleModule {

	/**
	 * Serial version UID for serialization compatibility. This ensures that deserialization will work correctly even if the
	 * class definition changes,
	 */
	@Serial
	private static final long serialVersionUID = -1715482618452923941L;

	/**
	 * Name of the module, used for identification when registering with ObjectMapper.
	 */
	public static final String NAME = "apiphany-jackson2";

	/**
	 * Default constructor that initializes the module with its name.
	 */
	public ApiphanyJackson2Module() {
		super(NAME);
	}

	/**
	 * Overrides the setupModule method to register custom annotation introspector for handling Apiphany-specific
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
	 * @return a configured instance of {@link ApiphanyJackson2Module}
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
		private static final SimpleModule INSTANCE = new ApiphanyJackson2Module()
				.addSerializer(RequestMethod.class, new RequestMethodSerializer())
				.addDeserializer(RequestMethod.class, new RequestMethodDeserializer());
	}
}

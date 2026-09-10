package org.apiphany.logging;

import java.util.Map;

import org.apiphany.logging.slf4j.Slf4jLibrary;
import org.morphix.runtime.Libraries;
import org.morphix.runtime.OptionalLibrary;

/**
 * Diagnostic context abstraction with optional library-specific implementations.
 * <p>
 * This class decouples application code from a concrete diagnostic-context library. The default implementation is a
 * no-op. When SLF4J is present on the classpath, {@link #instance()} returns an SLF4J-backed implementation.
 *
 * @author Radu Sebastian LAZIN
 */
public class DiagnosticContext {

	/**
	 * The instance holder nested class.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class InstanceHolder {

		/**
		 * The diagnostic context instance.
		 */
		private static final DiagnosticContext INSTANCE = initializeInstance(Slf4jLibrary.DIAGNOSTIC_CONTEXT);
	}

	/**
	 * Returns an instance based on the available diagnostic context libraries.
	 *
	 * @param libraryDescriptors the library descriptors
	 * @return a diagnostic context instance
	 */
	@SafeVarargs
	protected static DiagnosticContext initializeInstance(final OptionalLibrary<? extends DiagnosticContext>... libraryDescriptors) {
		return Libraries.instance(DiagnosticContext::new, libraryDescriptors);
	}

	/**
	 * Default constructor.
	 */
	protected DiagnosticContext() {
		// empty
	}

	/**
	 * Returns a singleton instance.
	 *
	 * @return a singleton instance
	 */
	public static DiagnosticContext instance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Returns the context value for the given key.
	 *
	 * @param key the context key
	 * @return the context value, or {@code null} if none is set
	 */
	public String get(final String key) {
		return null;
	}

	/**
	 * Puts a value into the context map.
	 *
	 * @param key the context key
	 * @param val the context value
	 */
	public void put(final String key, final String val) {
		// empty
	}

	/**
	 * Clears the context map.
	 */
	public void clear() {
		// empty
	}

	/**
	 * Returns a copy of the current context map.
	 *
	 * @return a copy of the context map, or {@code null} if no context is set
	 */
	public Map<String, String> getCopyOfContextMap() {
		return null; // NOSONAR documented to return null when no context is set
	}

	/**
	 * Sets the context map.
	 *
	 * @param contextMap the context map
	 */
	public void setContextMap(final Map<String, String> contextMap) {
		// empty
	}

	/**
	 * Sets the context, clearing it when the given map is {@code null}.
	 *
	 * @param contextMap the context map
	 */
	public void setContext(final Map<String, String> contextMap) {
		if (null == contextMap) {
			clear();
		} else {
			setContextMap(contextMap);
		}
	}
}

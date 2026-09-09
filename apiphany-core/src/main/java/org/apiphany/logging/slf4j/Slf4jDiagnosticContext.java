package org.apiphany.logging.slf4j;

import java.util.Map;

import org.apiphany.logging.DiagnosticContext;
import org.slf4j.MDC;

/**
 * A {@link DiagnosticContext} implementation that delegates to SLF4J {@link MDC}.
 * <p>
 * This class adapts the {@code org.apiphany.logging} diagnostic context abstraction to SLF4J's mapped diagnostic
 * context.
 * <p>
 * <h2>Library presence detection</h2> The static field {@link Slf4jLibrary#DESCRIPTOR} can be used to detect at runtime
 * whether the SLF4J library is present on the classpath. This allows code to conditionally enable SLF4J-backed
 * diagnostic context without a hard dependency.
 *
 * @author Radu Sebastian LAZIN
 */
public class Slf4jDiagnosticContext extends DiagnosticContext {

	/**
	 * Creates a new {@code Slf4jDiagnosticContext}.
	 */
	public Slf4jDiagnosticContext() {
		// empty
	}

	/**
	 * @see #get(String)
	 */
	@Override
	public String get(final String key) {
		return MDC.get(key);
	}

	/**
	 * @see #put(String, String)
	 */
	@Override
	public void put(final String key, final String val) {
		MDC.put(key, val);
	}

	/**
	 * @see #clear()
	 */
	@Override
	public void clear() {
		MDC.clear();
	}

	/**
	 * @see #getCopyOfContextMap()
	 */
	@Override
	public Map<String, String> getCopyOfContextMap() {
		return MDC.getCopyOfContextMap();
	}

	/**
	 * @see #setContextMap(Map) 
	 */
	@Override
	public void setContextMap(final Map<String, String> contextMap) {
		MDC.setContextMap(contextMap);
	}
}

package org.apiphany.test.fork;

import java.lang.reflect.Method;

import org.morphix.reflection.Constructors;
import org.morphix.reflection.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runner for executing tests in a forked JVM.
 *
 * @author Radu Sebastian LAZIN
 */
public class ForkedJvmRunner {

	/**
	 * Logger instance for logging test execution details.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ForkedJvmRunner.class);

	/**
	 * Runner completed successfully.
	 */
	public static final int SUCCESS = 0;

	/**
	 * Error codes used by the {@link ForkedJvmRunner}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Error {

		/**
		 * Error code for usage errors.
		 */
		public static final int USAGE = 1;

		/**
		 * Error code for test execution failures.
		 */
		public static final int TEST = 666;

		/**
		 * Private constructor to prevent instantiation.
		 */
		private Error() {
			// empty
		}
	}

	/**
	 * Main method to run the test.
	 *
	 * @param args command line arguments: {@code <testClass> <testMethod>}
	 */
	public static void main(final String[] args) {
		if (args.length != 2) {
			LOGGER.error("Usage: ForkedJvmRunner <testClass> <testMethod>");
			System.exit(Error.USAGE);
		}
		String className = args[0];
		String methodName = args[1];
		LOGGER.info("Starting forked test: {}.{}", className, methodName);

		try {
			run(className, methodName);
			LOGGER.info("Finished forked test: {}.{}", className, methodName);
			System.exit(SUCCESS);
		} catch (Throwable t) { // NOSONAR we want to catch all exceptions/errors from the test
			LOGGER.error("Failed forked test: {}.{}", className, methodName, t);
			System.exit(Error.TEST);
		}
	}

	/**
	 * Runs the specified test method of the given class.
	 * <p>
	 * TODO: Support test methods with parameters. TODO: Support setup and tear down methods
	 * (e.g., @BeforeEach, @AfterEach).
	 *
	 * @param className the name of the test class
	 * @param methodName the name of the test method
	 * @throws ClassNotFoundException if the specified class cannot be found
	 */
	public static void run(final String className, final String methodName) throws ClassNotFoundException {
		Class<?> testClass = Class.forName(className);
		Object instance = Constructors.IgnoreAccess.newInstance(testClass);
		Method method = Methods.getOneDeclaredInHierarchy(methodName, testClass);
		Methods.IgnoreAccess.invoke(method, instance);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ForkedJvmRunner() {
		throw Constructors.unsupportedOperationException();
	}
}

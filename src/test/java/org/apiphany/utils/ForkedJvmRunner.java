package org.apiphany.utils;

import java.lang.reflect.Method;

import org.morphix.reflection.Constructors;
import org.morphix.reflection.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForkedJvmRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForkedJvmRunner.class);

	public static final int SUCCESS = 0;
	public static final int ERROR_USAGE = 1;
	public static final int ERROR_TEST = 666;

	public static void main(final String[] args) {
		if (args.length != 2) {
			LOGGER.error("Usage: ForkedJvmRunner <testClass> <testMethod>");
			System.exit(ERROR_USAGE);
		}
		String className = args[0];
		String methodName = args[1];
		LOGGER.info("Starting forked test: {}.{}", className, methodName);

		try {
			runTest(className, methodName);
			LOGGER.info("Finished forked test: {}.{}", className, methodName);
			System.exit(SUCCESS);
		} catch (Throwable t) {
			LOGGER.error("Failed forked test: {}.{}", className, methodName, t);
			System.exit(ERROR_TEST);
		}
	}

	private static void runTest(final String className, final String methodName) throws ClassNotFoundException, NoSuchMethodException {
		Class<?> testClass = Class.forName(className);
		Object instance = Constructors.IgnoreAccess.newInstance(testClass);
		Method method = Methods.getOneDeclaredInHierarchy(methodName, testClass);
		Methods.IgnoreAccess.invoke(method, instance);
	}
}

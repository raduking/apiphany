package org.apiphany.test.fork;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 5 extension to run tests in a forked JVM.
 *
 * @author Radu Sebastian LAZIN
 */
public class ForkedJvmExtension implements InvocationInterceptor {

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ForkedJvmExtension.class);

	/**
	 * Intercepts test method invocations to run them in a forked JVM if annotated with {@link ForkedJvmTest}.
	 *
	 * @param invocation the invocation to proceed with
	 * @param context the reflective invocation context
	 * @param extensionContext the extension context
	 * @throws Throwable if an error occurs during invocation
	 */
	@Override
	public void interceptTestMethod(
			final Invocation<Void> invocation,
			final ReflectiveInvocationContext<Method> context,
			final ExtensionContext extensionContext) throws Throwable {
		ForkedJvmTest annotation = context.getExecutable().getAnnotation(ForkedJvmTest.class);
		if (annotation == null) {
			invocation.proceed();
			return;
		}

		String className = context.getExecutable().getDeclaringClass().getName();
		String methodName = context.getExecutable().getName();
		String[] jvmArgs = annotation.jvmArgs();

		String[] cmd = buildCommand(className, methodName, jvmArgs);

		boolean showCommand = "true".equals(System.getProperty("process.show.command"));
		if (showCommand) {
			String commandLine = String.join(" \\\n", cmd);
			LOGGER.info("[forked] command $ {}\n", commandLine);
		}

		ProcessBuilder pb = new ProcessBuilder(cmd)
				.redirectErrorStream(true);
		Process process = pb.start();

		try (InputStream inputStream = process.getInputStream()) {
			inputStream.transferTo(System.out);

			if (ForkedJvmRunner.SUCCESS != process.waitFor()) {
				throw new AssertionError("Forked JVM test failed: " + className + "#" + methodName);
			}
			invocation.skip();
		}
	}

	/**
	 * Constructs the command to run the specified test method in a forked JVM.
	 *
	 * @param className the name of the test class
	 * @param methodName the name of the test method
	 * @param jvmArgs the JVM arguments
	 * @return the command as an array of strings
	 */
	private static String[] buildCommand(final String className, final String methodName, final String[] jvmArgs) {
		ArrayList<String> command = new ArrayList<>();
		command.add("java");
		command.addAll(List.of(jvmArgs));
		command.add("-cp");
		command.add(System.getProperty("java.class.path"));
		command.add(ForkedJvmRunner.class.getName());
		command.add(className);
		command.add(methodName);

		return command.toArray(String[]::new);
	}
}

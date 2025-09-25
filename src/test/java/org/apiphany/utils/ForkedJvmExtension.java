package org.apiphany.utils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForkedJvmExtension implements InvocationInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForkedJvmRunner.class);

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

		String[] cmd = getCommand(className, methodName, jvmArgs);

		boolean showCommand = "true".equals(System.getProperty("process.show.command"));
		if (showCommand) {
			LOGGER.info("[forked] command $ {}\n", String.join(" \\\n", cmd));
		}

		ProcessBuilder pb = new ProcessBuilder(cmd)
				.redirectErrorStream(true);
		Process process = pb.start();

		InputStream inputStream = null;
		try {
			inputStream = process.getInputStream();
			inputStream.transferTo(System.out);

			if (ForkedJvmRunner.SUCCESS != process.waitFor()) {
				throw new AssertionError("Forked JVM test failed: " + className + "#" + methodName);
			}
			invocation.skip();
		} finally {
			if (null != inputStream) {
				inputStream.close();
			}
		}
	}

	private static String[] getCommand(final String className, final String methodName, final String[] jvmArgs) {
		ArrayList<String> command = new ArrayList<>();
		command.add("java");
		command.addAll(List.of(jvmArgs));
		command.add("-cp");
		command.add(System.getProperty("java.class.path"));
		command.add(ForkedJvmRunner.class.getName());
		command.add(className);
		command.add(methodName);

		return command.stream().toArray(String[]::new);
	}
}

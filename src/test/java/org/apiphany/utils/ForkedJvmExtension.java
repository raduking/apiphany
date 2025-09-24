package org.apiphany.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

public class ForkedJvmExtension implements InvocationInterceptor {

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
		String classPath = System.getProperty("java.class.path");
		String[] jvmArgs = annotation.jvmArgs();

		ArrayList<String> command = new ArrayList<>();
		command.add("java");
		command.addAll(List.of(jvmArgs));
		command.add("-cp");
		command.add(classPath);
		command.add(ForkedJvmRunner.class.getName());
		command.add(className);
		command.add(methodName);

		String[] cmd = command.stream().toArray(String[]::new);

		boolean showCommand = "true".equals(System.getProperty("process.show.command"));
		if (showCommand) {
			System.out.println("[FORKED] command $ " + String.join(" \\\n", cmd));
			System.out.println("");
		}

		ProcessBuilder pb = new ProcessBuilder(cmd)
				.inheritIO()
				.redirectErrorStream(true);
		Process process = pb.start();

		Thread outputThread = Thread.ofPlatform().start(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println("[FORKED] " + line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		int exit = process.waitFor();
		outputThread.join(Duration.ofSeconds(3));
		if (exit != 0) {
			throw new AssertionError("Forked JVM test failed: " + className + "#" + methodName);
		}
		invocation.skip();
	}
}

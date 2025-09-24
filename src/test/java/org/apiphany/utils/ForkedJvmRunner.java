package org.apiphany.utils;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.morphix.reflection.Constructors;
import org.morphix.reflection.Methods;

public class ForkedJvmRunner {

	public static void main(final String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: ForkedJvmRunner <testClass> <testMethod>");
			System.exit(1);
		}
		System.out.println("Staring test: " + args);

		String className = args[0];
		String methodName = args[1];

		try {
			Class<?> testClass = Class.forName(className);
			Object instance = Constructors.IgnoreAccess.newInstance(testClass);

			Method method = Arrays.stream(testClass.getDeclaredMethods())
					.filter(m -> m.getName().equals(methodName))
					.findFirst()
					.orElseThrow(() -> new NoSuchMethodException(methodName));

			Methods.IgnoreAccess.invoke(method, instance);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}
}

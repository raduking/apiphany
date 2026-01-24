package org.apiphany.test.fork;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Annotation for tests that should be executed in a forked JVM.
 *
 * @author Radu Sebastian LAZIN
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ForkedJvmExtension.class)
public @interface ForkedJvmTest {

	/**
	 * JVM arguments to be used when forking the JVM.
	 *
	 * @return an array of JVM argument strings
	 */
	String[] jvmArgs() default { };

}

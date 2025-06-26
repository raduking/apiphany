package org.apiphany.net;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ReflectionException;

/**
 * Test class for {@link Sockets}.
 *
 * @author Radu Sebastian LAZIN
 */
class SocketsTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		ReflectionException reflectionException = assertThrows(ReflectionException.class, () -> Constructors.IgnoreAccess.newInstance(Sockets.class));
		InvocationTargetException invocationTargetException = JavaObjects.cast(reflectionException.getCause());
		UnsupportedOperationException unsupportedOperationException = JavaObjects.cast(invocationTargetException.getCause());
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

}

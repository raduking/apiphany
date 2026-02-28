package org.apiphany.io;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 * A {@link Supplier} that provides an {@link InputStream}. This is used to model request bodies that can be read from
 * when sending HTTP requests. The supplier allows for lazy initialization of the stream, which can be useful for
 * streaming large request bodies or for deferring the creation of the stream until it is actually needed.
 *
 * @author Radu Sebastian LAZIN
 */
public interface InputStreamSupplier extends Supplier<InputStream> {

	/**
	 * Returns an {@link InputStream} that can be read from.
	 *
	 * @return an InputStream to be read from
	 */
	@Override
	InputStream get();

	/**
	 * Creates an {@link InputStreamSupplier} from a given {@link Supplier} of {@link InputStream}. This allows for easy
	 * conversion from any supplier that provides an InputStream to the specific type expected by the HTTP client.
	 *
	 * @param supplier the supplier of InputStream to be wrapped, must not be null
	 * @return an InputStreamSupplier that wraps the given supplier
	 * @throws NullPointerException if the supplier is null
	 */
	static InputStreamSupplier from(final Supplier<? extends InputStream> supplier) {
		return supplier::get;
	}
}

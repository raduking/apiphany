package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.apiphany.io.ByteSizeable;
import org.apiphany.io.UInt16;
import org.apiphany.lang.collections.Lists;
import org.apiphany.security.ssl.SSLProtocol;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.ThrowingBiFunction;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a TLS Record Layer protocol message containing one or more fragments.
 * <p>
 * The Record Layer is the lowest layer in the TLS protocol stack, responsible for:
 * <ul>
 * <li>Framing messages with headers</li>
 * <li>Fragmenting or coalescing messages as needed</li>
 * <li>Providing basic message type and version information</li>
 * </ul>
 *
 * <p>
 * This class handles both the serialization and deserialization of TLS records, supporting all standard record content
 * types defined in the TLS specification.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-6">RFC 5246 - Record Layer</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class Record implements TLSObject {

	/**
	 * The TLS record header.
	 */
	private final RecordHeader header;

	/**
	 * The TLS fragments in this record.
	 */
	private final List<TLSObject> fragments;

	/**
	 * Constructs a new Record with optional header updating.
	 *
	 * @param header the record header containing type and version information
	 * @param fragments the list of message fragments contained in this record
	 * @param updateHeader if true, the header length will be updated to match the fragments' size
	 */
	public Record(final RecordHeader header, final List<TLSObject> fragments, final boolean updateHeader) {
		this.header = updateHeader
				? new RecordHeader(header.getType(), header.getVersion(), UInt16.of((short) ByteSizeable.sizeOf(fragments)))
				: header;
		this.fragments = fragments;
	}

	/**
	 * Constructs a new Record with automatic header length calculation.
	 *
	 * @param header the record header containing type and version information
	 * @param messages the list of message fragments contained in this record
	 */
	public Record(final RecordHeader header, final List<TLSObject> messages) {
		this(header, messages, true);
	}

	/**
	 * Constructs a new Record containing a single fragment.
	 *
	 * @param type the record content type
	 * @param protocol the SSL/TLS protocol version
	 * @param fragment the single fragment to include in this record
	 */
	public Record(final RecordContentType type, final SSLProtocol protocol, final TLSObject fragment) {
		this(new RecordHeader(type, protocol), List.of(fragment), true);
	}

	/**
	 * Constructs a new Record with automatically determined content type.
	 *
	 * @param protocol the SSL/TLS protocol version
	 * @param fragment the fragment to include in this record
	 */
	public Record(final SSLProtocol protocol, final TLSObject fragment) {
		this(RecordContentType.from(fragment), protocol, fragment);
	}

	/**
	 * Constructs a new Handshake-type Record.
	 *
	 * @param protocol the SSL/TLS protocol version
	 * @param handshakeObject the handshake message to wrap in a record
	 */
	public Record(final SSLProtocol protocol, final TLSHandshakeBody handshakeObject) {
		this(new RecordHeader(RecordContentType.HANDSHAKE, protocol), List.of(new Handshake(handshakeObject)));
	}

	/**
	 * Parses a Record from an input stream. This method will return a single record for handshake messages that span
	 * through multiple records.
	 *
	 * @param is the input stream containing the record data
	 * @return the parsed Record object
	 * @throws IOException If an I/O error occurs while reading
	 * @throws IllegalArgumentException If the record data is malformed
	 */
	public static Record from(final InputStream is) throws IOException {
		RecordHeader header = RecordHeader.from(is);
		RecordContentType recordType = header.getType();

		List<TLSObject> fragments = new ArrayList<>();
		int currentLength = header.getLength().getValue();
		while (currentLength > 0) {
			TLSObject fragment = recordType.fragment().from(is, currentLength);
			fragments.add(fragment);
			currentLength -= fragment.sizeOf();
		}
		return new Record(header, fragments, false);
	}

	/**
	 * Parses a Record from an input stream using a custom fragment reader.
	 *
	 * @param is the input stream containing the record data
	 * @param fragmentReader the function to parse the record's fragments
	 * @return the parsed Record object
	 * @throws IOException If an I/O error occurs while reading
	 * @throws IllegalArgumentException If the record data is malformed
	 */
	private static Record from(final InputStream is, final BiFunction<InputStream, Short, TLSObject> fragmentReader) throws IOException {
		RecordHeader header = RecordHeader.from(is);
		TLSObject fragment = fragmentReader.apply(is, header.getLength().getValue());

		return new Record(header, List.of(fragment), false);
	}

	/**
	 * Parses a Record from an input stream using a custom fragment reader.
	 *
	 * @param is the input stream containing the record data
	 * @param fragmentReader the function to parse the record's fragments
	 * @return the parsed Record object
	 * @throws IOException If an I/O error occurs while reading
	 * @throws IllegalArgumentException If the record data is malformed
	 */
	public static Record from(final InputStream is, final ThrowingBiFunction<InputStream, Short, TLSObject> fragmentReader) throws IOException {
		return from(is, ThrowingBiFunction.unchecked(fragmentReader));
	}

	/**
	 * Returns the binary representation of this record.
	 *
	 * @return a byte array containing the complete serialized record
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(header.toByteArray());
		for (TLSObject fragment : fragments) {
			buffer.put(fragment.toByteArray());
		}
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this record.
	 *
	 * @return a JSON string representing this record
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size of this record when serialized.
	 *
	 * @return the size in bytes of this record including header and all fragments
	 */
	@Override
	public int sizeOf() {
		return header.sizeOf() + ByteSizeable.sizeOf(fragments);
	}

	/**
	 * Returns the record header containing type and version information.
	 *
	 * @return the RecordHeader for this record
	 */
	public RecordHeader getHeader() {
		return header;
	}

	/**
	 * Returns all fragments contained in this record.
	 *
	 * @return an unmodifiable list of fragments in this record
	 */
	public List<TLSObject> getFragments() {
		return fragments;
	}

	/**
	 * Returns a specific handshake fragment by index.
	 *
	 * @param index the index of the handshake fragment to retrieve
	 * @return the Handshake fragment at the specified index
	 * @throws IndexOutOfBoundsException If the index is invalid
	 * @throws ClassCastException If the fragment is not a Handshake
	 */
	public Handshake getHandshake(final int index) {
		return JavaObjects.cast(fragments.get(index));
	}

	/**
	 * Returns all fragments of a specific type.
	 *
	 * @param <T> the type of fragments to filter for
	 *
	 * @param tlsObjectClass the class object of the fragment type to filter for
	 * @return a list containing all matching fragments (may be empty)
	 */
	public <T extends TLSObject> List<T> getFragments(final Class<T> tlsObjectClass) {
		if (Lists.isEmpty(fragments)) {
			return Collections.emptyList();
		}
		List<T> result = new ArrayList<>(fragments.size());
		for (TLSObject fragment : fragments) {
			if (tlsObjectClass.isAssignableFrom(fragment.getClass())) {
				result.add(JavaObjects.cast(fragment));
			}
		}
		return result;
	}

	/**
	 * Returns a fragment of a specific type. If there are none or more than one fragments of the same type a
	 * {@link IllegalStateException} is thrown.
	 *
	 * @param <T> the type of fragments to filter for
	 *
	 * @param tlsObjectClass the class object of the fragment type to filter for
	 * @return a matching fragment
	 * @throws IllegalStateException if there are none or more than one fragments of the required type present
	 */
	public <T extends TLSObject> T getFragment(final Class<T> tlsObjectClass) {
		if (Lists.isEmpty(fragments)) {
			throw new IllegalStateException("No fragments of type " + tlsObjectClass + " are present in the record.");
		}
		T result = null;
		for (TLSObject fragment : fragments) {
			if (tlsObjectClass.isAssignableFrom(fragment.getClass())) {
				if (null != result) {
					throw new IllegalStateException("More than one fragments of type " + tlsObjectClass + " are present in the record.");
				}
				result = JavaObjects.cast(fragment);
			}
		}
		return result;
	}

	/**
	 * Returns the first handshake fragment in this record.
	 *
	 * @return the first Handshake fragment
	 * @throws IndexOutOfBoundsException If there are no fragments
	 * @throws ClassCastException If the first fragment is not a Handshake
	 */
	@JsonIgnore
	public Handshake getFirstHandshake() {
		return getHandshake(0);
	}

	/**
	 * Checks if this record contains a specific type of handshake message.
	 *
	 * @param <T> the type of handshake message to check for
	 *
	 * @param tlsHandshakeClass the class object of the handshake type to check for
	 * @return true if the record contains a matching handshake message
	 */
	public <T extends TLSHandshakeBody> boolean hasHandshake(final Class<T> tlsHandshakeClass) {
		for (TLSObject fragment : fragments) {
			if (Handshake.class.isAssignableFrom(fragment.getClass())) {
				Handshake handshake = JavaObjects.cast(fragment);
				if (handshake.is(tlsHandshakeClass)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if this record does not contain a specific type of handshake message.
	 *
	 * @param <T> the type of handshake message to check for
	 *
	 * @param tlsHandshakeClass the class object of the handshake type to check for
	 * @return true if the record does not contain a matching handshake message
	 */
	public <T extends TLSHandshakeBody> boolean hasNoHandshake(final Class<T> tlsHandshakeClass) {
		return !hasHandshake(tlsHandshakeClass);
	}

	/**
	 * Returns a specific type of handshake message from this record.
	 *
	 * @param <T> the type of handshake message to retrieve
	 *
	 * @param tlsHandshakeClass the class object of the handshake type to retrieve
	 * @return the matching handshake message
	 * @throws IllegalArgumentException If no matching handshake is found
	 */
	public <T extends TLSHandshakeBody> T getHandshake(final Class<T> tlsHandshakeClass) {
		for (TLSObject fragment : fragments) {
			if (Handshake.class.isAssignableFrom(fragment.getClass())) {
				Handshake handshake = JavaObjects.cast(fragment);
				if (handshake.is(tlsHandshakeClass)) {
					return handshake.get(tlsHandshakeClass);
				}
			}
		}
		throw new IllegalArgumentException("No handhsake of type " + tlsHandshakeClass + " found in record");
	}
}

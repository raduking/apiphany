package org.apiphany.client;

/**
 * Enum for client life-cycle management.
 * <p>
 * This enum defines the expected life-cycle of a client instance. It indicates whether the client is ephemeral
 * (short-lived) or long-lived (intended for reuse across multiple operations or sessions).
 *
 * @author Radu Sebastian LAZIN
 */
public enum ClientLifecycle {

	/**
	 * Ephemeral life-cycle, meaning that the client instance is expected to be short-lived and may be created and disposed
	 * frequently. This is suitable for scenarios where clients are used for single operations or sessions and then
	 * discarded.
	 */
	EPHEMERAL,

	/**
	 * Long-lived life-cycle, meaning that the client instance is intended for reuse across multiple operations or sessions.
	 * This is suitable for scenarios where clients maintain state or connections that should persist over time, and where
	 * creating new instances frequently would be inefficient.
	 */
	LONG_LIVED;
}

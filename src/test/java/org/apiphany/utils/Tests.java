package org.apiphany.utils;

import org.apiphany.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;

/**
 * Utility methods for tests.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Tests {

	static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

	/**
	 * Returns true if Docker is running, false otherwise.
	 *
	 * @return true if Docker is running, false otherwise
	 */
	@SuppressWarnings("resource")
	public static boolean isDockerRunning() {
		try {
			DockerClientFactory.instance().client();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Verifies that docker is running.
	 *
	 * @throws IllegalStateException if docker is not running
	 */
	@SuppressWarnings("resource")
	public static void verifyDockerRunning() {
		try {
			DockerClientFactory.instance().client();
			LOGGER.info("Docker is running and accessible");
		} catch (Exception e) {
			throw new IllegalStateException("""
					Docker is not running or not accessible. Please:
					1. Start Docker Desktop/Engine
					2. Ensure your user is in the 'docker' group
					3. For Linux: run 'sudo usermod -aG docker $USER' and restart session
					""", e);
		}
	}
}

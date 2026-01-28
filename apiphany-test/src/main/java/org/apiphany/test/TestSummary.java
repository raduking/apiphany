package org.apiphany.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.morphix.reflection.Constructors;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test summary utility.
 * <p>
 * TODO: This is a rough implementation. Improve it later.
 *
 * @author Radu Sebastian LAZIN
 */
public class TestSummary {

	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = Logger.getLogger(TestSummary.class.getName());
	static {
		Logger root = Logger.getLogger("");
		for (Handler existingHandler : root.getHandlers()) {
			root.removeHandler(existingHandler);
		}

		// disable default console handler
		LOGGER.setUseParentHandlers(false);

		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);

		handler.setFormatter(new SimpleFormatter() {
			@Override
			public synchronized String format(final LogRecord lr) {
				return lr.getMessage() + "\n";
			}
		});

		LOGGER.addHandler(handler);
		LOGGER.setLevel(Level.ALL);
	}

	/**
	 * Title constants.
	 */
	private static final String TITLE = "TEST SUMMARY";

	/**
	 * Line separator.
	 */
	private static final String LINE = "-".repeat(128);

	/**
	 * Private constructor to prevent instantiation.
	 */
	private TestSummary() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Main method.
	 *
	 * @param args command line arguments
	 * @throws Exception if an error occurs
	 */
	public static void main(final String[] args) throws Exception {
		File surefireTargetDir;
		File failsafeTargetDir;
		if (args.length == 1) {
			surefireTargetDir = new File(args[0]);
			failsafeTargetDir = surefireTargetDir;
		} else if (args.length == 2) {
			surefireTargetDir = new File(args[0]);
			failsafeTargetDir = new File(args[1]);
		} else {
			surefireTargetDir = detectTargetDirectory();
			failsafeTargetDir = surefireTargetDir;
		}

		File surefire = new File(surefireTargetDir, "surefire-reports");
		File failsafe = new File(failsafeTargetDir, "failsafe-reports");

		Summary unit = parseDir(surefire);
		Summary integration = parseDir(failsafe);

		double totalTime = unit.time + integration.time;

		log(LINE);
		log(TITLE);
		log(LINE);

		log("Unit tests:        %.3f s (%d tests, %d passed, %d failed, %d skipped)",
				unit.time, unit.tests, unit.passed(), unit.failed(), unit.skipped);
		log("Integration tests: %.3f s (%d tests, %d passed, %d failed, %d skipped)",
				integration.time, integration.tests, integration.passed(), integration.failed(), integration.skipped);

		log(LINE);
		log("Total:             %.3f s (%d tests)",
				totalTime, unit.tests + integration.tests);
	}

	/**
	 * Detects the target directory based on the class location.
	 *
	 * @return the target directory
	 */
	private static File detectTargetDirectory() {
		// get the directory where the class is located
		URL classLocation = TestSummary.class.getProtectionDomain().getCodeSource().getLocation();
		String classPath = classLocation.getPath();
		log("[%sINFO%s] Class path: %s%n", ANSIColor.BLUE, ANSIColor.RESET, classPath);

		// navigate from target/classes or target/test-classes to target directory
		return new File(classPath).getParentFile();
	}

	/**
	 * Parses the test report XML files in the given directory.
	 *
	 * @param dir the directory containing the test report XML files
	 * @return the summary of the test results
	 * @throws ParserConfigurationException if a parser configuration error occurs
	 * @throws SAXException if a SAX error occurs
	 * @throws IOException if an I/O error occurs
	 */
	private static Summary parseDir(final File dir) throws ParserConfigurationException, SAXException, IOException {
		Summary s = new Summary();
		if (!dir.exists()) {
			log("[%sERROR%s] Directory %s does not exist", ANSIColor.RED, ANSIColor.RESET, dir.getAbsolutePath());
			return s;
		}

		File[] files = dir.listFiles((d, name) -> name.startsWith("TEST-") && name.endsWith(".xml"));
		if (files == null) {
			return s;
		}

		for (File f : files) {
			Document doc = createDocumentBuilderFactory().newDocumentBuilder().parse(f);
			NodeList suites = doc.getElementsByTagName("testsuite");

			for (int i = 0; i < suites.getLength(); i++) {
				var e = suites.item(i);

				s.tests += getIntAttr(e, "tests");
				s.failures += getIntAttr(e, "failures");
				s.errors += getIntAttr(e, "errors");
				s.skipped += getIntAttr(e, "skipped");
				s.time += getDoubleAttr(e, "time");
			}
		}
		return s;
	}

	/**
	 * Retrieves an integer attribute from a DOM node.
	 *
	 * @param node the DOM node
	 * @param attr the attribute name
	 * @return the integer value of the attribute, or 0 if not found or invalid
	 */
	private static int getIntAttr(final Node node, final String attr) {
		try {
			return Integer.parseInt(node.getAttributes().getNamedItem(attr).getTextContent());
		} catch (Exception e) {
			log("[%sERROR%s] %s", ANSIColor.RED, ANSIColor.RESET, e.getMessage());
			return 0;
		}
	}

	/**
	 * Retrieves a double attribute from a DOM node.
	 *
	 * @param node the DOM node
	 * @param attr the attribute name
	 * @return the double value of the attribute, or 0.0 if not found or invalid
	 */
	private static double getDoubleAttr(final Node node, final String attr) {
		try {
			return Double.parseDouble(node.getAttributes().getNamedItem(attr).getTextContent());
		} catch (Exception e) {
			log("[%sERROR%s] %s", ANSIColor.RED, ANSIColor.RESET, e.getMessage());
			return 0;
		}
	}

	/**
	 * Creates a secure DocumentBuilderFactory to prevent XXE attacks.
	 *
	 * @return a secure DocumentBuilderFactory
	 * @throws ParserConfigurationException if a parser configuration error occurs
	 */
	private static DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// Disable DTDs entirely
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

		// Disable external entities
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

		// Disable external DTDs in validation
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);

		return factory;
	}

	/**
	 * Logs a formatted message.
	 *
	 * @param message the message format
	 * @param args the message arguments
	 */
	private static void log(final String message, final Object... args) {
		String formattedMessage = String.format(message, args);
		LOGGER.info(formattedMessage);
	}

	/**
	 * Summary data class.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Summary {

		/**
		 * Number of tests.
		 */
		int tests = 0;

		/**
		 * Number of failures.
		 */
		int failures = 0;

		/**
		 * Number of errors.
		 */
		int errors = 0;

		/**
		 * Number of skipped tests.
		 */
		int skipped = 0;

		/**
		 * Total time taken.
		 */
		double time = 0.0;

		/**
		 * Default constructor.
		 */
		public Summary() {
			// empty
		}

		/**
		 * Calculates the number of failed tests.
		 *
		 * @return the number of failed tests
		 */
		public int failed() {
			return failures + errors;
		}

		/**
		 * Calculates the number of passed tests.
		 *
		 * @return the number of passed tests
		 */
		public int passed() {
			return tests - failed() - skipped;
		}
	}
}

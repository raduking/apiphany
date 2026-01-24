package org.apiphany.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.ConsoleHandler;
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

	private static final Logger LOGGER = Logger.getLogger(TestSummary.class.getName());

	static {
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

	private static final String TITLE = "TEST SUMMARY";
	private static final String LINE = "-".repeat(128);

	/**
	 * ANSI color codes.
	 */
	public static final String RESET = "\u001B[0m";
	public static final String BLACK = "\u001B[30m";
	public static final String RED = "\u001B[31m";
	public static final String GREEN = "\u001B[32m";
	public static final String YELLOW = "\u001B[33m";
	public static final String BLUE = "\u001B[34m";
	public static final String PURPLE = "\u001B[35m";
	public static final String CYAN = "\u001B[36m";
	public static final String WHITE = "\u001B[37m";

	/**
	 * Background colors.
	 */
	public static final String BG_RED = "\u001B[41m";
	public static final String BG_GREEN = "\u001B[42m";
	public static final String BG_BLUE = "\u001B[44m";

	/**
	 * Private constructor to prevent instantiation.
	 */
	private TestSummary() {
		throw Constructors.unsupportedOperationException();
	}

	public static void main(final String[] args) throws Exception {
		File targetDir;
		if (args.length == 1) {
			targetDir = new File(args[0]);
		} else {
			targetDir = detectTargetDirectory();
		}

		File surefire = new File(targetDir, "surefire-reports");
		File failsafe = new File(targetDir, "failsafe-reports");

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

	private static File detectTargetDirectory() {
		// get the directory where the class is located
		URL classLocation = TestSummary.class.getProtectionDomain().getCodeSource().getLocation();
		String classPath = classLocation.getPath();
		log("[%sINFO%s] Class path: %s%n", BLUE, RESET, classPath);

		// navigate from target/classes or target/test-classes to target directory
		return new File(classPath).getParentFile();
	}

	private static Summary parseDir(final File dir) throws ParserConfigurationException, SAXException, IOException {
		Summary s = new Summary();
		if (!dir.exists()) {
			log("[%sERROR%s] Directory %s does not exist", RED, RESET, dir.getAbsolutePath());
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

	private static int getIntAttr(final Node node, final String attr) {
		try {
			return Integer.parseInt(node.getAttributes().getNamedItem(attr).getTextContent());
		} catch (Exception e) {
			log("[%sERROR%s] %s", RED, RESET, e.getMessage());
			return 0;
		}
	}

	private static double getDoubleAttr(final Node node, final String attr) {
		try {
			return Double.parseDouble(node.getAttributes().getNamedItem(attr).getTextContent());
		} catch (Exception e) {
			log("[%sERROR%s] %s", RED, RESET, e.getMessage());
			return 0;
		}
	}

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

	private static void log(final String message, final Object... args) {
		String formattedMessage = String.format(message, args);
		LOGGER.info(formattedMessage);
	}

	/**
	 * Summary data class.
	 */
	public static class Summary {

		int tests = 0;
		int failures = 0;
		int errors = 0;
		int skipped = 0;
		double time = 0.0;

		public int failed() {
			return failures + errors;
		}

		public int passed() {
			return tests - failed() - skipped;
		}
	}
}

package org.apiphany.test;

import java.io.File;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.morphix.reflection.Constructors;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Build test summary utility.
 *
 * @author Radu Sebastian LAZIN
 */
public class TestSummary {

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
		// get the directory where the class is located
		URL classLocation = TestSummary.class.getProtectionDomain().getCodeSource().getLocation();
		String classPath = classLocation.getPath();
		System.out.printf("[%sINFO%s] Class path: %s%n", BLUE, RESET, classPath);

		// navigate from target/classes or target/test-classes to target directory
		File targetDir = new File(classPath).getParentFile();

		File surefire = new File(targetDir, "surefire-reports");
		File failsafe = new File(targetDir, "failsafe-reports");

		Summary unit = parseDir(surefire);
		Summary integration = parseDir(failsafe);

		double totalTime = unit.time + integration.time;

		System.out.println(LINE);
		System.out.println(TITLE);
		System.out.println(LINE);

		System.out.printf("Unit tests:        %.3f s (%d tests, %d passed, %d failed, %d skipped)%n",
				unit.time, unit.tests, unit.passed(), unit.failed(), unit.skipped);
		System.out.printf("Integration tests: %.3f s (%d tests, %d passed, %d failed, %d skipped)%n",
				integration.time, integration.tests, integration.passed(), integration.failed(), integration.skipped);

		System.out.println(LINE);
		System.out.printf("Total:             %.3f s (%d tests)%n",
				totalTime, unit.tests + integration.tests);
	}

	static Summary parseDir(final File dir) throws Exception {
		Summary s = new Summary();
		if (!dir.exists()) {
			System.err.printf("[%sERROR%s] Directory %s does not exist%n", RED, RESET, dir.getAbsolutePath());
			return s;
		}

		File[] files = dir.listFiles((d, name) -> name.startsWith("TEST-") && name.endsWith(".xml"));
		if (files == null) {
			return s;
		}

		for (File f : files) {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
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

	static int getIntAttr(final Node node, final String attr) {
		try {
			return Integer.parseInt(node.getAttributes().getNamedItem(attr).getTextContent());
		} catch (Exception e) {
			System.err.printf("[%sERROR%s] %s", RED, RESET, e.getMessage());
			return 0;
		}
	}

	static double getDoubleAttr(final Node node, final String attr) {
		try {
			return Double.parseDouble(node.getAttributes().getNamedItem(attr).getTextContent());
		} catch (Exception e) {
			System.err.printf("[%sERROR%s] %s", RED, RESET, e.getMessage());
			return 0;
		}
	}

	static class Summary {

		int tests = 0;
		int failures = 0;
		int errors = 0;
		int skipped = 0;
		double time = 0.0;

		int failed() {
			return failures + errors;
		}

		int passed() {
			return tests - failed() - skipped;
		}
	}
}

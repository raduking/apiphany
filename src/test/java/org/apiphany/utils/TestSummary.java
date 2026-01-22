package org.apiphany.utils;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.testcontainers.shaded.com.google.common.base.Strings;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Build test summary utility.
 *
 * @author Radu Sebastian LAZIN
 */
public class TestSummary {

	private static final String TITLE = "TEST SUMMARY";
	private static final String LINE = Strings.repeat("-", 128);

	public static void main(final String[] args) throws Exception {
		File surefire = new File("target/surefire-reports");
		File failsafe = new File("target/failsafe-reports");

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

	static int getIntAttr(final org.w3c.dom.Node node, final String attr) {
		try {
			return Integer.parseInt(node.getAttributes().getNamedItem(attr).getTextContent());
		} catch (Exception ignored) {
			return 0;
		}
	}

	static double getDoubleAttr(final org.w3c.dom.Node node, final String attr) {
		try {
			return Double.parseDouble(node.getAttributes().getNamedItem(attr).getTextContent());
		} catch (Exception ignored) {
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

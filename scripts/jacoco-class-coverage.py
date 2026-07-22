#!/usr/bin/env python3
# Requires Python 3.9+ (uses built-in generic types in annotations)
"""
Print JaCoCo coverage counters for one or more classes from jacoco.xml.

Examples:
  python3 scripts/jacoco-class-coverage.py \
    --xml apiphany-core/target/site/jacoco.xml \
    org.apiphany.security.oauth2.OAuth2TokenProvider

  python3 scripts/jacoco-class-coverage.py \
    org/apiphany/lang/BackoffDelayStrategy \
    --show-misses
"""

import argparse
import sys
import pathlib
import xml.etree.ElementTree as ET

COUNTERS = ("INSTRUCTION", "BRANCH", "LINE", "COMPLEXITY", "METHOD", "CLASS")


def normalize_class_name(name: str) -> str:
    return name.strip().replace(".", "/")


def pct(covered: int, missed: int) -> float:
    total = covered + missed
    return 100.0 if total == 0 else (covered * 100.0 / total)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Check JaCoCo class coverage")
    parser.add_argument(
        "classes",
        nargs="+",
        help="Class name(s), either dotted (a.b.C) or internal slash format (a/b/C)",
    )
    parser.add_argument(
        "--xml",
        default="apiphany-core/target/site/jacoco.xml",
        help="Path to jacoco.xml (default: apiphany-core/target/site/jacoco.xml)",
    )
    parser.add_argument(
        "--show-misses",
        action="store_true",
        help="Show methods with missed lines or branches",
    )
    return parser.parse_args()

def get_counters_dictionary(object: ET.Element) -> dict[str, tuple[int, int]]:
	return {
        c.get("type"): (int(c.get("missed", "0")), int(c.get("covered", "0")))
        for c in object.findall("counter")
    }


def show_method_misses(cls: ET.Element) -> None:
    print("  methods with misses:")
    for method in cls.findall("method"):
        method_counters = get_counters_dictionary(method)
        line_missed, line_covered = method_counters.get("LINE", (0, 0))
        branch_missed, branch_covered = method_counters.get("BRANCH", (0, 0))
        if line_missed > 0 or branch_missed > 0:
            print(
                f"    {method.get('name')}{method.get('desc')} "
                f"LINE miss {line_missed} cov {line_covered}; "
                f"BRANCH miss {branch_missed} cov {branch_covered}"
            )


def main() -> int:
    args = parse_args()
    xml_path = pathlib.Path(args.xml)

    if not xml_path.exists():
        print(f"ERROR: JaCoCo XML not found: {xml_path}", file=sys.stderr)
        return 2

    try:
        root = ET.parse(xml_path).getroot()
    except ET.ParseError as exc:
        print(f"ERROR: Invalid XML file {xml_path}: {exc}", file=sys.stderr)
        return 2

    wanted = {normalize_class_name(c) for c in args.classes}
    found = set()

    for package in root.findall("package"):
        for cls in package.findall("class"):
            class_name = cls.get("name", "")
            if class_name not in wanted:
                continue

            found.add(class_name)
            class_counters = get_counters_dictionary(cls)

            print(class_name)
            for counter_type in COUNTERS:
                missed, covered = class_counters.get(counter_type, (0, 0))
                total = missed + covered
                print(
                    f"  {counter_type:12} covered={covered:4} missed={missed:4} "
                    f"total={total:4} pct={pct(covered, missed):6.2f}%"
                )

            if args.show_misses:
                show_method_misses(cls)

            print()

    missing = wanted - found
    if missing:
        for cls in sorted(missing):
            print(f"NOT FOUND: {cls}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())

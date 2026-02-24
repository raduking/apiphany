#!/bin/bash

# Usage: ./create-module.sh <module-name> <parent-artifact-id>
# Example: ./create-module.sh apiphany-json-jackson2-tests apiphany-parent

MODULE_NAME=$1
PARENT_ARTIFACT=$2
VERSION=$3

if [[ -z "$MODULE_NAME" || -z "$PARENT_ARTIFACT" || -z "$VERSION" ]]; then
  echo "Usage: $0 <module-name> <parent-artifact-id> <version>"
  exit 1
fi

echo "Creating module: $MODULE_NAME with parent: $PARENT_ARTIFACT and version: $VERSION"

# Create module directory
mkdir -p "$MODULE_NAME"/src/{main,test}/{java,resources}

# Create a minimal pom.xml
cat > "$MODULE_NAME/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

	<modelVersion>4.0.0</modelVersion>

	<parent>
		<groupId>io.github.raduking</groupId>
		<artifactId>$PARENT_ARTIFACT</artifactId>
		<version>$VERSION</version>
		<relativePath>../../pom.xml</relativePath>
	</parent>

	<artifactId>$MODULE_NAME</artifactId>
	<packaging>jar</packaging>

	<name>\${project.groupId}:\${project.artifactId}</name>
	<description>Apiphany tests</description>
	<url>https://github.com/raduking/apiphany</url>

	<licenses>
		<license>
			<name>Apache License, Version 2.0</name>
			<url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
		</license>
	</licenses>

	<developers>
		<developer>
			<id>raduking</id>
			<name>Radu Sebastian LAZIN</name>
			<email>raduking@gmail.com</email>
		</developer>
	</developers>

	<scm>
		<connection>scm:git:git://github.com/raduking/apiphany.git</connection>
		<developerConnection>scm:git:ssh://github.com:raduking/apiphany.git</developerConnection>
		<url>https://github.com/raduking/apiphany/tree/master</url>
	</scm>

	<properties>
		<!-- empty -->
	</properties>

	<dependencies>
	
		<!-- Apiphany -->
		<dependency>
			<groupId>io.github.raduking</groupId>
			<artifactId>apiphany</artifactId>
			<version>\${project.version}</version>
		</dependency>

		<!-- Testing -->
		<dependency>
			<groupId>io.github.raduking</groupId>
			<artifactId>apiphany-test</artifactId>
			<version>\${project.version}</version>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>ch.qos.logback</groupId>
			<artifactId>logback-classic</artifactId>
			<version>\${logback.version}</version>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>org.hamcrest</groupId>
			<artifactId>hamcrest</artifactId>
			<version>\${hamcrest.version}</version>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter-api</artifactId>
			<version>\${junit.jupiter.version}</version>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter-params</artifactId>
			<version>\${junit.jupiter.version}</version>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter-engine</artifactId>
			<version>\${junit.jupiter.version}</version>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>org.mockito</groupId>
			<artifactId>mockito-core</artifactId>
			<version>\${mockito.version}</version>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>org.mockito</groupId>
			<artifactId>mockito-junit-jupiter</artifactId>
			<version>\${mockito.version}</version>
			<scope>test</scope>
		</dependency>

	</dependencies>

	<build>
		<plugins>
			<!-- Copy resources to output directories -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-resources-plugin</artifactId>
				<executions>
					<execution>
						<id>copy-main-resources</id>
						<phase>process-resources</phase>
						<goals>
							<goal>copy-resources</goal>
						</goals>
						<configuration>
							<outputDirectory>\${project.build.outputDirectory}</outputDirectory>
							<resources>
								<resource>
									<directory>src/main/resources</directory>
								</resource>
							</resources>
						</configuration>
					</execution>
					<execution>
						<id>copy-test-resources</id>
						<phase>process-test-resources</phase>
						<goals>
							<goal>testResources</goal>
						</goals>
						<configuration>
							<outputDirectory>\${project.build.testOutputDirectory}</outputDirectory>
							<resources>
								<resource>
									<directory>src/test/resources</directory>
								</resource>
							</resources>
						</configuration>
					</execution>
					<execution>
						<id>copy-resources</id>
						<phase>prepare-package</phase>
						<goals>
							<goal>copy-resources</goal>
						</goals>
						<configuration>
							<outputDirectory>\${project.build.outputDirectory}/static/docs</outputDirectory>
							<resources>
								<resource>
									<directory>\${project.build.directory}/generated-docs</directory>
								</resource>
							</resources>
						</configuration>
					</execution>
				</executions>
			</plugin>

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
			</plugin>

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
			</plugin>

			<!-- Code formatting -->
			<plugin>
				<groupId>com.diffplug.spotless</groupId>
				<artifactId>spotless-maven-plugin</artifactId>
				<executions>
					<execution>
						<phase>compile</phase>
						<goals>
							<goal>apply</goal>
						</goals>
					</execution>
				</executions>
			</plugin>

		</plugins>
	</build>

</project>
EOF

cat > "$MODULE_NAME/.gitignore" <<EOF
\# Compiled class files
/target/

\# Eclipse IDE
/.settings
/.factorypath
/.classpath
/.project

\# IntelliJ IDEA
/.idea

\# Visual Studio Code
/.vscode
EOF

echo "Add $MODULE_NAME to parent."

PARENT_POM_XML="../pom.xml"
NEW_MODULE="integration/$MODULE_NAME"

# Add new module just before </modules>
sed -i '' '/<\/modules>/i \
		<module>'"$NEW_MODULE"'</module>' "$PARENT_POM_XML"

echo "Module $MODULE_NAME created successfully."

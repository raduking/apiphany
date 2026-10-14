package org.apiphany.tests.json;

import org.apiphany.json.JsonBuilder;
import org.apiphany.tests.contract.json.JsonBuilderAnnotationContract;
import org.apiphany.tests.contract.json.JsonBuilderContract;
import org.apiphany.tests.contract.json.JsonBuilderLocaleContract;
import org.apiphany.tests.contract.json.JsonBuilderMonthContract;
import org.apiphany.tests.contract.json.JsonBuilderOptionalContract;
import org.apiphany.tests.contract.json.JsonBuilderSqlDateContract;
import org.apiphany.tests.contract.json.JsonBuilderTimeContract;
import org.apiphany.tests.contract.json.JsonBuilderYearContract;

/**
 * Integration tests for {@link JsonBuilder}.
 * <p>
 * These tests must pass for every JSON library implementation, as they verify the basic contract of transforming
 * objects to JSON and back. The test methods are defined once in the shared JSON contract interfaces
 * ({@link JsonBuilderContract}, {@link JsonBuilderAnnotationContract}, {@link JsonBuilderTimeContract},
 * {@link JsonBuilderYearContract}, {@link JsonBuilderMonthContract}, {@link JsonBuilderSqlDateContract},
 * {@link JsonBuilderLocaleContract}, {@link JsonBuilderOptionalContract}) and inherited here, so every JSON library
 * integration test module runs the exact same contract and cross-library parity is trivial to verify.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonBuilderIT implements JsonBuilderAnnotationContract, JsonBuilderTimeContract, JsonBuilderYearContract,
		JsonBuilderMonthContract, JsonBuilderSqlDateContract, JsonBuilderLocaleContract, JsonBuilderOptionalContract {

	// tests are inherited from the JSON contract interfaces

}

package tech.provokedynamic.gymcrmworkload.cucumberintegration;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cross-service integration suite: exercises gym-crm's real producer
 * contract (topic, key, transactionId header, JSON payload) against
 * gym-crm-workload's real Kafka listener, Mongo persistence, and REST API,
 * all wired to one Testcontainers Kafka + Mongo pair.
 *
 * Kept separate from {@code RunCucumberTest} (component tests) both by
 * feature directory and glue package, so `mvn test -Dcucumber.filter.tags=...`
 * or simply running one class vs. the other lets you run component and
 * integration suites independently in CI.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features-integration")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "tech.provokedynamic.gymcrmworkload.cucumberintegration")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty, summary")
public class RunIntegrationCucumberIT {
}

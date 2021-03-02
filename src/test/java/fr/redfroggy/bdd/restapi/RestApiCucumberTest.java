package fr.redfroggy.bdd.restapi;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Gherkin tests entry points
 * - features: Path where the .feature files are located
 * - glue: List of packages where step implementation are written to
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        features = "src/test/resources/features",
        glue = {"fr.redfroggy.bdd.restapi.glue"})
public  final class RestApiCucumberTest {
}

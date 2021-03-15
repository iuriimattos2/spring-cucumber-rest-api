# Test your local and remote REST API with Spring boot, Cucumber and Gherkin !

<div align="center">
  <a name="logo" href="https://www.redfroggy.fr"><img src="assets/logo.png" alt="RedFroggy"></a>
  <h4 align="center">A RedFroggy project</h4>
</div>
<br/>
<div align="center">
  <a href="https://forthebadge.com"><img src="https://forthebadge.com/images/badges/fuck-it-ship-it.svg"/></a>
  <a href="https://forthebadge.com"><img src="https://forthebadge.com/images/badges/built-with-love.svg"/></a>
  <a href="https://forthebadge.com"><img src="https://forthebadge.com/images/badges/made-with-java.svg"/></a>
</div>
<div align="center">
   <a href="https://maven-badges.herokuapp.com/maven-central/fr.redfroggy.test.bdd/cucumber-restapi"><img src="https://maven-badges.herokuapp.com/maven-central/fr.redfroggy.test.bdd/cucumber-restapi/badge.svg?style=plastic" /></a>
   <a href="https://travis-ci.com/RedFroggy/spring-cucumber-rest-api"><img src="https://travis-ci.com/RedFroggy/spring-cucumber-rest-api.svg?branch=master"/></a>
   <a href="https://codecov.io/gh/RedFroggy/spring-cucumber-rest-api"><img src="https://codecov.io/gh/RedFroggy/spring-cucumber-rest-api/branch/master/graph/badge.svg?token=XM9R6ZV9SJ"/></a>
   <a href="https://github.com/semantic-release/semantic-release"><img src="https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg"/></a>
   <a href="https://opensource.org/licenses/mit-license.php"><img src="https://badges.frapsoft.com/os/mit/mit.svg?v=103"/></a>
</div>
<br/>
<br/>

Made with , [Cucumber](https://cucumber.io/) and [Gherkin](https://cucumber.io/docs/gherkin/) !
Inspired from the awesome [apickli project](https://github.com/apickli/apickli) project.

## Stack
- Spring Boot
- Cucumber / Gherkin
- Jayway JsonPath

## Installation
```xml
<dependency>
  <groupId>fr.redfroggy.test.bdd</groupId>
  <artifactId>cucumber-restapi</artifactId>
</dependency>
```
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.redfroggy.test.bdd/cucumber-restapi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.redfroggy.test.bdd/cucumber-restapi)

Run `npm install` to add commitlint + husky

## Demo & Example

![Spring Cucumber Gherkin Demo](assets/demo.gif)

You can look at the [users.feature](src/test/resources/features/users.feature) file for a more detailed example.

## Share data between steps
- You can use the following step to store data from a json response body to a shared context:
```gherkin
And I store the value of body path $.id as idUser in scenario scope
```
- You can use the following step to store data from a response header to a shared context:
```gherkin
And I store the value of response header Authorization as authHeader in scenario scope
```
- The result of the JsonPath `$.id` will be stored in the `idUser` variable.
- To reuse this variable in another step, you can do:
```gherkin
When I DELETE /users/`$idUser`
And I set Authorization header to `$authHeader`
```


## How to use it in my existing project ?

You can see a usage example in the [test folder](src/test/java/fr/redfroggy/bdd/restapi).

### Add a CucumberTest  file

```java
@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty"},
        features = "src/test/resources/features",
        glue = {"fr.redfroggy.bdd.restapi.glue"})
public class CucumberTest {

}
````
- Set the glue property to  `fr.redfroggy.bdd.glue` and add your package glue.
- Set your `features` folder property
- Add your `.feature` files under your `features` folder
- In your `.feature` files you should have access to all the steps defined in the [DefaultRestApiBddStepDefinition](src/main/java/fr/redfroggy/bdd/restapi/glue/DefaultRestApiBddStepDefinition.java) file.


### Add default step definition file
It is mandatory to have a class annotated with `@CucumberContextConfiguration` to be able to run the tests.
This class must be in the same `glue` package that you've specified in the `CucumberTest` class.

```java
@CucumberContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultStepDefinition {

}
````

### Specify an authentication mode
- You can authenticate using the step: `I authenticate with login/password (.*)/(.*)` but the authentication 
  mode must be implemented by you.
- You need to implement the `BddRestTemplateAuthentication` interface.
- You can inject a `TestRestTemplate` instance in your code, so you can do pretty much anything you want.
- For example, for a JWT authentication you can do :
```java
@CucumberContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultStepDefinition implements BddRestTemplateAuthentication {
    
    final TestRestTemplate template;

    public DefaultRestApiStepDefinitionTest(TestRestTemplate template) {
        this.template = template;
    }

    @Override
    public TestRestTemplate authenticate(String login, String password) {
        String token = generateJwt();
        restTemplate.getRestTemplate().getInterceptors().add(
                (outReq, bytes, clientHttpReqExec) -> {
                    outReq.getHeaders().set(
                            HttpHeaders.AUTHORIZATION, token
                    );
                    return clientHttpReqExec.execute(outReq, bytes);
                });

        return restTemplate;
    }
}
```
- For a basic authentication, you can do :
```java
@CucumberContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultStepDefinition implements BddRestTemplateAuthentication {

    final TestRestTemplate template;

    public DefaultRestApiStepDefinitionTest(TestRestTemplate template) {
        this.template = template;
    }

    @Override
    public TestRestTemplate authenticate(String login, String password) {
        return this.template.withBasicAuth(login, password);
    }
}
```
- If you use a specific class, it must be annotated with `@Component` to be detected by spring context scan.
```java
@Component
public class BasicAuthAuthentication implements BddRestTemplateAuthentication {

    final TestRestTemplate template;

    public BasicAuthAuthentication(TestRestTemplate template) {
        this.template = template;
    }

    @Override
    public TestRestTemplate authenticate(String login, String password) {
        return this.template.withBasicAuth(login, password);
    }
}
```

## Mock third party call
If you need to mock a third party API, you can use the following steps:

```gherkin
I mock third party api call (.*) (.*) with return code (.*) and body: (.*)
  # Example: I mock third party api call GET /public/characters/1?format=json with return code 200 and body: {"comicName": "IronMan", "city": "New York", "mainColor": ["red", "yellow"]}
I mock third party api call (.*) (.*) with return code (.*), content type: (.*) and file: (.*)
  # Example: I mock third party api call GET /public/characters/2 with return code 200, content type: application/json and file: fixtures/bruce_wayne_marvel_api.fixture.json
```

It relies on [WireMock](http://wiremock.org) for stubbing api calls.
By default, the wiremock port is `8888`, if you need to override it you need to change the 
`redfroggy.cucumber.restapi.wiremock.port` property in your project.

## Run local unit tests

````bash
$ mvn test
````

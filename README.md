# Spring Cloudflare Turnstile Service

## Overview

Cloudflare's Turnstile Service is a captcha-like bot detection and mitigation service that helps protect your website from malicious traffic. This library provides a Spring Boot service that integrates with Cloudflare's Turnstile Service to protect your website from bots. It provides a simple and easy-to-use interface for configuring and using the Turnstile Service in your Spring Boot application.

Read more about Cloudflare's Turnstile Service [here](https://www.cloudflare.com/products/turnstile/).

You will need a Cloudflare Account, and to create a Turnstile Widget for your site, which will give you a Site Key and Secret to use with this library.


## Features

- Easy configuration using Spring Boot's `application.yml`.
- Handles API requests and responses seamlessly.
- Provides a simple interface validating Cloudflare Turnstile tokens.


## Getting Started

### Prerequisites

- Java 17 or later
- Gradle 8.10.1 or Maven 3.8.1+
- Cloudflare Turnstile Site Key and Secret

### Quick Start

The library is available through the Maven Central Repository. You can include it in your Spring Boot project using either Maven or Gradle.

#### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.digitalsanctuary</groupId>
    <artifactId>ds-spring-cf-turnstile</artifactId>
    <version>1.1.4</version>
</dependency>
```

#### Gradle

Add the following dependency to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.digitalsanctuary:ds-spring-cf-turnstile:1.1.4'
}
```

### Configuration

Configure the library using the `application.yml` file located in

`src/main/resources`

You need to setup your Turnstile Site Key and Secret in the `application.yml` file.

```yaml
ds:
  cf:
    turnstile:
      sitekey: # Turnstile Site Key
      secret: # Turnstile Secret
```



### Simple Example Usage

#### Front End

To add Cloudflare Turnstile to your website, include the following script in your HTML:

```html
<script src="https://challenges.cloudflare.com/turnstile/v0/api.js" defer></script>
```

Add the Turnstile widget to your form with the `cf-turnstile` class and the `data-sitekey` attribute set to your Turnstile Site Key:

```html
<form id="login-form" action="/login" method="post">
    <input type="email" name="email" placeholder="Email" required>
    <div class="cf-turnstile" data-sitekey="$YOUR_SITE_KEY"></div>
    <button type="submit">Login</button>
</form>
```

If you are using Thymeleaf, you can use the following code:

```html
<form id="login-form" action="#" th:action="@{/login}" method="post">
    <input type="email" name="email" placeholder="Email" required>
    <div class="cf-turnstile" th:data-sitekey="${@turnstileValidationService.getTurnsiteSitekey()}"></div>
    <button type="submit">Login</button>
</form>
```

And the site key will be automatically populated from the `application.yml` file.



#### Back End

Use the `TurnstileService` to validate Turnstile tokens from your Controller:

The Turnstile token is passed as a request parameter named `cf-turnstile-response`. You can access the token by adding a `@RequestParam` annotation to your controller method.

```java
...(Model model, @RequestParam("cf-turnstile-response") String turnstileResponse, .....) {
```


```java
...
import com.digitalsanctuary.cf.turnstile.TurnstileValidationService; // Import the TurnstileValidationService
...
@Autowired
private TurnstileValidationService turnstileValidationService; // Inject the TurnstileValidationService
...
@PostMapping("/login")
	public String login(Model model, @RequestParam String email,
      @RequestParam("cf-turnstile-response") String turnstileResponse,
			HttpServletRequest request) {
		// Get the client IP address (optional but recommended)
		String clientIpAddress = turnstileValidationService.getClientIpAddress(request);

		// Validate the Turnstile response token
		boolean turnstileValid = turnstileValidationService.validateTurnstileResponse(turnstileResponse, clientIpAddress);

		if (!turnstileValid) {
			log.error("Turnstile validation failed for login request with email: " + email);
			return "error";
		}

		// Otherwise handle the login process normally...
		if (service.handleLogin(email)) {
      ...
	}
}
```


## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Contact

For any questions or support, please open an issue on the GitHub repository.

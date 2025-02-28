# Spring Cloudflare Turnstile Service

## Overview

Cloudflare's Turnstile Service is a captcha-like bot detection and mitigation service that helps protect your website from malicious traffic. This library provides a Spring Boot service that integrates with Cloudflare's Turnstile Service to protect your website from bots. It provides a simple and easy-to-use interface for configuring and using the Turnstile Service in your Spring Boot application.

Read more about Cloudflare's Turnstile Service [here](https://www.cloudflare.com/products/turnstile/).

You will need a Cloudflare Account, and to create a Turnstile Widget for your site, which will give you a Site Key and Secret to use with this library.


## Features

- Easy configuration using Spring Boot's `application.yml` or `application.properties`.
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
    <version>1.1.7</version>
</dependency>
```

#### Gradle

Add the following dependency to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.digitalsanctuary:ds-spring-cf-turnstile:1.1.7'
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
    <div class="cf-turnstile" th:data-sitekey="${@turnstileValidationService.getTurnstileSitekey()}"></div>
    <button type="submit">Login</button>
</form>
```

And the site key will be automatically populated from the `application.yml` file.



#### Back End

Use the `TurnstileValidationService` to validate Turnstile tokens from your Controller:

The Turnstile token is passed as a request parameter named `cf-turnstile-response`. You can access the token by adding a `@RequestParam` annotation to your controller method.

```java
@RequestParam(name = "cf-turnstile-response", required = true) String turnstileResponse
```

Here's a complete example:

```java
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class LoginController {

    @Autowired
    private TurnstileValidationService turnstileValidationService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public String login(Model model, 
                      @RequestParam String email,
                      @RequestParam(name = "cf-turnstile-response", required = true) String turnstileResponse,
                      HttpServletRequest request) {
                      
        // Get the client IP address (recommended for security)
        String clientIpAddress = turnstileValidationService.getClientIpAddress(request);

        // Validate the Turnstile response token
        boolean turnstileValid = turnstileValidationService.validateTurnstileResponse(turnstileResponse, clientIpAddress);

        if (!turnstileValid) {
            log.warn("Turnstile validation failed for login request with email: {}", email);
            model.addAttribute("error", "Security verification failed. Please try again.");
            return "login";
        }

        // Handle the login process normally
        if (userService.authenticateUser(email)) {
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Invalid login credentials");
            return "login";
        }
    }
}
```

#### Alternative Method (Without IP)

If you don't need to validate the client IP address, you can use the simplified method:

```java
boolean turnstileValid = turnstileValidationService.validateTurnstileResponse(turnstileResponse);
```


## Security Best Practices

When integrating Cloudflare Turnstile into your application, keep these security considerations in mind:

1. **Protect your secret key**: Never expose your Turnstile Secret key in client-side code or public repositories.

2. **Always validate on the server**: Never rely solely on client-side validation. Always validate Turnstile tokens on your server.

3. **Use IP validation**: When possible, include the client's IP address in the validation request for an additional layer of security.

4. **Implement proper error handling**: Don't provide detailed error messages to users that could reveal implementation details.

5. **Add rate limiting**: Consider implementing rate limiting on endpoints that use Turnstile validation to prevent abuse.

6. **Monitor validation failures**: Track and alert on unusual patterns of validation failures, which could indicate an attack.

## Architecture

Spring Cloudflare Turnstile uses Spring Boot's auto-configuration to seamlessly integrate with your application:

1. **Auto-configuration**: The library is automatically configured when included in your Spring Boot application.

2. **Configuration Properties**: Properties are loaded from your application configuration files.

3. **Service Layer**: The `TurnstileValidationService` provides methods for validating tokens and retrieving configuration.

4. **HTTP Client**: Uses Spring's `RestClient` to communicate with Cloudflare's API.

5. **DTO Layer**: Response objects map directly to Cloudflare's API responses.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to this project.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](http://www.apache.org/licenses/LICENSE-2.0) file for details.

## Contact

For questions or support, please open an issue on the [GitHub repository](https://github.com/devondragon/SpringCFTurnstile/issues).

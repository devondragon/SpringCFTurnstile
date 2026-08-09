# 2.1.0

## Behavior change

- `TurnstileCaptchaFilter` is now opt-in. It registers only when
  `ds.cf.turnstile.login.enabled=true`. Previously it registered automatically whenever the
  library was on the classpath, intercepting POSTs to `ds.cf.turnstile.login.submission-path`
  (default `/login`). **If you use the login filter, add `ds.cf.turnstile.login.enabled=true`
  when upgrading.** If you only use `TurnstileValidationService` directly, no change is needed.
  Apps that inject `TurnstileCaptchaFilter` directly (e.g. into a `SecurityFilterChain`, per
  the old README pattern) will fail startup with `NoSuchBeanDefinitionException` until
  `ds.cf.turnstile.login.enabled=true` is set.

  Apps that relied on the filter being auto-registered with the servlet container — that is,
  they never injected it anywhere — get **no** startup error. The filter is simply absent and
  POSTs to the login submission path are no longer captcha-checked. Two things limit the blast
  radius: the startup log now states the filter's registration state explicitly
  (`Turnstile login captcha filter (ds.cf.turnstile.login.enabled): ENABLED|DISABLED`), and
  Spring Security form-login apps were not effectively protected by the auto-registered filter
  anyway — as a plain servlet filter it ran behind the security filter chain, after
  authentication had already been processed.

## New

- `TurnstileValidationService.isUsingTestCredentials()` — returns true when the configured
  sitekey or secret is one of Cloudflare's published test credentials. A WARN banner is logged
  at startup in that case, so test keys cannot reach production unnoticed. Depending on the key
  those credentials make validation always pass (no bot protection) or always fail (all users
  blocked).
- The turnstile actuator health contributor reports a `usingTestCredentials` detail, so the same
  condition is visible on a running deployment and not only in the startup log.
- Startup reporting (missing secret/URL errors, the test-credential banner, and the login filter
  registration state) now runs from a `TurnstileStartupReporter` bean that is registered
  unconditionally, so these checks survive a consumer overriding the service bean.

## Fixed

- `turnstileValidationService` and `turnstileRestClient` beans are now `@ConditionalOnMissingBean`,
  so consumers can supply their own implementations without a bean-definition conflict.
  `TurnstileValidationService` is a concrete class with no interface, so overriding it means
  supplying your own instance of that class or a subclass. Note what this does *not* buy you: the
  built-in counters only move when the library's own validation methods run, so a subclass that
  overrides validation without delegating to `super` leaves health permanently UP with zero
  counts and flat metrics. Either delegate to `super` or override the counter getters
  (`getValidationCount()`, `getErrorRate()`, and friends). (The RestClient condition is
  name-based: only a bean named `turnstileRestClient` overrides it.)
- The login filter's settings are bound through `TurnstileConfigProperties` instead of `@Value`
  placeholders. The kebab-case names published in the configuration metadata
  (`ds.cf.turnstile.login.submission-path`, `login.redirect-url`, `token.parameter-name`) were
  silently ignored before; they now work, and the camelCase forms still bind via relaxed binding.

(#106)

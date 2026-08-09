# 2.1.0

## Behavior change

- `TurnstileCaptchaFilter` is now opt-in. It registers only when
  `ds.cf.turnstile.login.enabled=true`. Previously it registered automatically whenever the
  library was on the classpath, intercepting POSTs to `ds.cf.turnstile.login.submissionPath`
  (default `/login`). **If you use the login filter, add `ds.cf.turnstile.login.enabled=true`
  when upgrading.** If you only use `TurnstileValidationService` directly, no change is needed.

## New

- `TurnstileValidationService.isUsingTestCredentials()` — returns true when the configured
  sitekey or secret is one of Cloudflare's published test credentials. The service also logs a
  WARN banner at startup in that case, so always-pass test keys cannot reach production
  unnoticed.

## Fixed

- `turnstileValidationService` and `turnstileRestClient` beans are now `@ConditionalOnMissingBean`,
  so consumers can supply their own implementations without a bean-definition conflict. (The
  RestClient condition is name-based: only a bean named `turnstileRestClient` overrides it.)

(#106)

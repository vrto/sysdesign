### Summary

A simple rate limiter implementation based on the [token bucket](https://en.wikipedia.org/wiki/Token_bucket) algorithm.

### How does it work?

- requests pass through the rate limiter, each request consumes a token
- rate limiter's internal state is represented by a 'token bucket'
- if there is one or more tokens available, the request is forwarded
- if there aren't any more tokens available, the request is dropped
- tokens are periodically refilled (at fixed) rate to the token bucket by 'refiller'

### Notes
- for now this isn't using any real server under the hood, but the limiter is agnostic to any tech
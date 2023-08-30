### Summary

A simple snowflake algorithm implementation.

### How does it work?

- The leftmost 41 bits represent the timestamp.
- The middle 10 bits represent the machine ID.
- The rightmost 12 bits represent the sequence number.

```
  0         41            51            63
+-----------+-------------+-------------+
| Timestamp | Machine ID  |  Sequence   |
+-----------+-------------+-------------+
```
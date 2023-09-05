### Summary

A simple URL shortener code (without the actual web layer).

### How does it work?

- Shortening
    - Input string is Base62 converted and stored in the db
- Expanding
    - Finds the Base62 input string in the existing db

Base62 returns very short strings (not fixed size, though) immune to hash collision.
Alternate solutions are:
- hash functions (CRC32, MD5, SHA-1 ...) - prone to hash collisions 
- bloom filter (more complex)
### Summary

A small demonstration of a “rehashing problem” and one of its solutions using consistent hashing.

### Rehashing

- given a server list
- keys are mapped to the server list via hash(key) % len(serverList)
- when a server goes down, len(serverList) and keys need to be remapped
- this typically results in a high percentage of moved keys

### Consistent hashing

- servers are mapped to the ‘hash ring’
- lookup: hash(key) and move clockwise until a server with a higher hash found
- add/remove server: only a fraction of keys need to be moved
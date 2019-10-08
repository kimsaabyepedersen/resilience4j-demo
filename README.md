# Resilience4j demo


## Server
Has a few endpoints:
* `time`
* `slowTime`
* `slowFaultyTime`

All return the following payload and status code 200:
````json
{"time":  "current time in ISO8601 format"}
````
with the following caveats:
* `time` will return immediately.
* `slowTime` will  sleep for 30 seconds then return, except for every 10th call, where it will return immediately.
* `slowFaultyTime` will return status code 500, except for every 10th call, where it will return as described above immediately.

## Client
See the `main` method.
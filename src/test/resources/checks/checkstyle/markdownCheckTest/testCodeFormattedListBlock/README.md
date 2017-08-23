FORMAT: 1A

# Eclipse SmartHome UI Logging Bundle

## Log [/rest/log/{limit}]    

### Get last log messages [GET]

Return the last log entries received by `/rest/log/`.


+ Parameters
    + limit (number, optional) - Limit the amount of messages.

        On invalid input, limit is set to it's default.

        + Default: 500

+ Response 200 (application/json)

    + Attributes
        + timestamp (number) - UTC milliseconds from the epoch.

            In JavaScript, you can use this value for constructing a `Date`.

        + severity (enum[string])
            + Members
                + `error`
                + `warn`
                + `info`
                + `debug`

        + url (string)
        + message (string)

    + Body

            [
              {
                "timestamp": 1450531459479,
                "severity": "error",
                "url": "http://example.com/page1",
                "message": "test 5"
              },
              {
                "timestamp": 1450531459655,
                "severity": "error",
                "url": "http://example.com/page1",
                "message": "test 6"
              },
              {
                "timestamp": 1450531460038,
                "severity": "error",
                "url": "http://example.com/page2",
                "message": "test 7"
              }
            ]        


To extract the Keycloak public key you can run any test in the `OAuth2ApiClientIT` class and copy it from the logs.
The logs will contain the following:

```
22:29:52.005 [main] INFO  org.apiphany.ApiClient - Keycloak certificate (PEM public key):
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtp3VvKBt7SpKY4FAJUr1
9vyAsKHZvzvlMa2WdmFd3qlTXwZykPZ4Cerd91i4vlP+C5lLbsADvdfZq77vMrA3
QPwFWqg4TZu0ibHPxEoTLZJbSQXY8FXtsN0EJ/zW+p0oeFjurcUA7PH/FuH7gptI
2UNPWdaJT9q5Y7oEu8u+x1Xhy9zVeb5BWw4uhJ6JtVABYLknETInXyhLeMpUYmoj
WZSjC+L8dmOHJ1ypLlZ8bWLaboQc2Xsc8/6k4XAfG3s8MsaIL/pjsq/CtcRn3J1F
FYoLnyMDBNa5UWEBOiPLk/Vj46TC1c2wVpYAfwX2nUWsSQ0mgwtigVrHcHBoTsXY
uwIDAQAB
-----END PUBLIC KEY-----
```

Since Keycloak is most likely rotating the keys this is only usable for the currently running test.


To see the logs for Java SSL debugging information add the following property:

```
-Djavax.net.debug=ssl:handshake:verbose
```

For the most verbose version use:

```
-Djavax.net.debug=ssl:handshake:verbose:plaintext:sslctx:packet
-Djava.security.debug=all
```

Also, you can see all options with:

```
-Djavax.net.debug=help
```

See the `sun.security.util.Debug.Help` class documentation for more information.

---

When running the `TLSLoggingProvider` add `-Djava.security.skipVerifyProviders=true` because the provider is not signed.

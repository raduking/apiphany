
## Eclipse

When running unit tests / coverage you need to set the following JVM (VM) arguments.

```
-Djson-builder.to-json.indent-output=true
-XX:+EnableDynamicAgentLoading
-Xshare:off
--add-opens java.base/javax.net.ssl=ALL-UNNAMED
--add-opens java.base/javax.crypto=ALL-UNNAMED
--add-opens java.base/com.sun.crypto.provider=ALL-UNNAMED
--add-opens java.base/sun.security.ssl=ALL-UNNAMED
--add-opens java.base/sun.security.internal.spec=ALL-UNNAMED
--add-opens jdk.httpserver/sun.net.httpserver=ALL-UNNAMED
```

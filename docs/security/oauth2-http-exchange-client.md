`OAuth2HttpExchangeClient` class hierarchy:

```mermaid
classDiagram
    direction TB

    ExchangeClient <|.. HttpExchangeClient
    ExchangeClient <|.. AuthenticatedExchangeClient
    ExchangeClient <|.. DelegatingExchangeClient

	DelegatingExchangeClient <|-- DecoratingExchangeClient
	
	HttpExchangeClient <|--  AbstractHttpExchangeClient

    DelegatingExchangeClient <|.. AuthenticatedExchangeClient
    
    HttpExchangeClient <|.. AuthorizationHttpExchangeClient
    AuthorizationHeaderProvider <|.. AuthorizationHttpExchangeClient
    AuthenticatedExchangeClient <|.. AuthorizationHttpExchangeClient

	AuthenticationTokenProvider <|.. TokenHttpExchangeClient
	AuthorizationHttpExchangeClient <|.. TokenHttpExchangeClient
	DecoratingExchangeClient <|-- TokenHttpExchangeClient

    TokenHttpExchangeClient <|-- OAuth2HttpExchangeClient

    class ExchangeClient {
        +[T,U] ApiResponse[U] exchange(ApiRequest[T])
        +[T,U] CompletableFuture[ApiResponse[U]] asyncExchange(ApiRequest[T])
        +[T extends ClientProperties] T getClientProperties()
        +AuthenticationType getAuthenticationType()
        +String getName()
        +void close()
    }

	class DelegatingExchangeClient {
	    +[T,U] ApiResponse[U] exchange(ApiRequest[T])
		+ExchangeClient getExchangeClient()
	}

    class DecoratingExchangeClient {
        -ScopedResource[ExchangeClient] exchangeClient
        +[T,U] ApiResponse[U] exchange(ApiRequest[T])
        +ExchangeClient getExchangeClient()
        +void close()
    }

    class HttpExchangeClient {
        +RequestMethod get()
        +RequestMethod post()
        +RequestMethod put()
        +RequestMethod delete()
        +RequestMethod patch()
        +RequestMethod options()
        +RequestMethod head()
        +RequestMethod trace()
    }

    class AbstractHttpExchangeClient {
        -ClientProperties clientProperties
        -List[ContentConverter[?]] contentConverters
        -HeaderValues headerValuesChain
        -SSLContext sslContext
        +void addDefaultContentConverters()
        +void addDefaultHeaderValues()
        +SSLContext getSslContext()
        +convertBody(...)
    }

    class AuthenticatedExchangeClient {
        +[T,U] ApiResponse[U] exchange(ApiRequest[T])
        +[T] void authenticate(ApiRequest[T])
    }

    class AuthorizationHeaderProvider {
        +String getAuthorizationHeader()
    }

    class AuthorizationHttpExchangeClient {
        +[T] void authenticate(ApiRequest[T])
        +String getAuthorizationHeader()*
    }

    class AuthenticationTokenProvider {
        +AuthenticationToken getAuthenticationToken()
    }

    class TokenHttpExchangeClient {
        -AuthenticationToken authenticationToken
        -HttpAuthScheme authenticationScheme
        +AuthenticationToken getAuthenticationToken()
        +String getAuthorizationHeader()
        +AuthenticationType getAuthenticationType()
        +HttpAuthScheme getAuthenticationScheme()
    }

    class OAuth2HttpExchangeClient {
        -ScopedResource[ExchangeClient] tokenExchangeClient
        -OAuth2Properties oAuth2Properties
        -OAuth2TokenProvider tokenProvider
        +AuthenticationType getAuthenticationType()
    }
```

`OAuth2HttpExchangeClient` exchange flow:

```mermaid
sequenceDiagram
    participant User
    participant OAuth2 as OAuth2HttpExchangeClient
    participant Token as TokenHttpExchangeClient
    participant Authz as AuthorizationHttpExchangeClient
    participant Auth as AuthenticatedExchangeClient
    participant Decorator as DelegatingExchangeClient
    participant Delegate as ExchangeClient

    User->>OAuth2: exchange(request)

%% Template method
    OAuth2->>Auth: exchange(request)
    Auth->>Authz: authenticate(request)

%% Authorization header construction
    Authz->>Token: getAuthorizationHeader()
    Token->>OAuth2: getAuthenticationToken()
    OAuth2-->>Token: AuthenticationToken
    Token-->>Authz: "Bearer <access_token>"

%% Header mutation
    Authz->>Authz: add Authorization header

%% Delegation
    Auth->>Decorator: exchange(request)
    Decorator->>Delegate: exchange(request)

    Delegate-->>User: ApiResponse
```
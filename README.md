# Spring Extension for Kotlin

Spring Boot extension library providing common utilities and configurations for Kotlin applications.

## Features

### Configuration

#### Global Error Handler

Global exception handler that provides centralized error handling with configurable error attributes.

**Configuration:**

```properties
server.error.global.enabled=true  # Default: true
server.error.filter=               # Optional: Stack trace filter pattern
```

**Features:**

- Configurable error attributes (exception, stack trace, message, binding errors)
- Stack trace filtering support
- Respects Spring Boot error properties

#### Request Logging Filter

Configurable request logging filter for HTTP requests.

**Configuration:**

```properties
logging.request.enabled=true               # Default: true
logging.request.include-client-info=true   # Default: true
logging.request.include-headers=false      # Default: false
logging.request.include-payload=true       # Default: true
logging.request.include-query-string=true  # Default: true
logging.request.max-payload-length=5000    # Default: 5000
```

**Features:**

- Configurable request detail logging
- Client info, headers, payload, and query string support
- Configurable payload length limit

#### Custom Message Source

Custom message source implementation that loads messages from external directories.

**Configuration:**

```properties
# resource path to message files directory
# - ex) src/main/resources/language/i18n -> language/i18n
spring.messages.path=language/i18n
```

**Features:**

- Loads messages from external directory
- Integrates with `basica-kt` Messages utility
- Supports locale-based message resolution
- Message binding with arguments

#### Object Mapper Builder

Utility for building Jackson ObjectMapper with common configurations.

**Features:**

- Field-based serialization (ignores getters)
- XSS protection via HTML character escaping
- Kotlin module configuration
- JavaTime module support
- Custom date/time serializers
- Configurable null inclusion

**Usage:**

```kotlin
val mapper = ObjectMapperBuilder().build(removeXss = true, includeNotNull = true)
```

#### HTML Character Escapes

XSS protection utility for escaping HTML characters in JSON serialization.

**Features:**

- Escapes potentially dangerous characters: `<`, `>`, `"`, `(`, `)`, `#`, `'`
- Integrates with Jackson ObjectMapper

### Servlet Utilities

#### HttpContext

Comprehensive HTTP context utility providing easy access to request/response information.

**Configuration:**

```properties
server.httpctx.enabled=true  # Default: true
```

**Features:**

- Request/response access
- Session management
- Header and parameter access
- Cookie management
- File download header configuration
- Spring bean access
- Environment property access
- Profile management
- Transaction ID (UUID-based)
- IP address utilities (remote, localhost)
- URL encoding utilities

**Usage:**

```kotlin
val request = HttpContext.request
val session = HttpContext.session
val remoteIp = HttpContext.remoteAddress
val bean = HttpContext.bean(MyService::class)
```

#### Cookies

Cookie management utility.

**Configuration:**

```properties
server.cookies.enabled=true  # Default: true
```

**Features:**

- Cookie retrieval and existence checking
- Cookie creation with path and max-age support
- Map-based cookie access

**Usage:**

```kotlin
val cookie = Cookies["sessionId"]
val exists = Cookies.exists("sessionId")
val newCookie = Cookies.create("name", "value", path = "/", maxAge = 3600)
```

### JPA Utilities

#### List/Set Converters

JPA attribute converters for `List<T>` and `Set<T>` types.

**Features:**

- Automatic JSON serialization/deserialization
- Auto-applied to all entities
- Null-safe conversion

**Usage:**

```kotlin
@Entity
class MyEntity {
    @Convert(converter = ListConverter::class)
    var tags: List<String>? = null
    
    @Convert(converter = SetConverter::class)
    var categories: Set<String>? = null
}
```

#### Base Page Param

Base class for pagination parameters.

**Features:**

- Page, size, and sort parameter support
- Conversion to Spring Data `Pageable`
- Sort expression parsing with `SortBuilder`
- Support for entity-based column mapping

**Usage:**

```kotlin
class MyPageParam(
    page: Int = 0,
    size: Int = 10,
    sort: String? = null
) : BasePageParam(page, size, sort)

// Convert to Pageable
val pageable = myPageParam.toPageable("id,desc", MyEntity::class)
```

#### Sort Builder

Utility for building Spring Data `Sort` objects from string expressions.

**Features:**

- Parse sort expressions: `"name,asc ^ id,desc"`
- Entity-based column validation
- Custom column mapping support
- Cached field name lookup

**Usage:**

```kotlin
val sort  = SortBuilder().toSort("name,asc ^ id,desc", MyEntity::class)
val sort2 = SortBuilder().toSort("name,asc") { field -> 
    COLUMN_MAP[field] 
}
```

### Cache Utilities

#### Simple Key Generator

Cache key generator for Spring Cache abstraction.

**Features:**

- Generates keys from method parameters
- Handles empty parameters
- Single parameter optimization
- Array parameter support

#### Simple Key

Simple cache key implementation with deep equality checking.

**Features:**

- Deep array comparison
- Efficient hashCode calculation
- Thread-safe empty key singleton

#### Ignorable Cache Error Handler

Cache error handler that logs errors instead of throwing exceptions.

**Features:**

- Graceful error handling
- Warning-level logging
- Prevents cache errors from breaking application flow

#### Redis Cache Writer for Clear All

Enhanced Redis cache writer with improved `clear()` operation.

**Features:**

- Efficient pattern-based key deletion using SCAN
- Prevents concurrent clearing operations
- Batch deletion for performance
- Locking support for thread safety
- Statistics collection support

## Auto-Configuration

All components are automatically configured when the library is included. Components can be disabled via configuration properties:

| Property                            | Description                    |
|-------------------------------------|--------------------------------|
| `server.error.global.enabled=false` | Disable global error handler   |
| `logging.request.enabled=false`     | Disable request logging filter |
| `spring.messages.path` (empty)      | Disable custom message source  |
| `server.httpctx.enabled=false`      | Disable HttpContext            |
| `server.cookies.enabled=false`      | Disable Cookies utility        |

## Dependencies

- Spring Boot 3.5.6+
- Kotlin 2.2.0+
- basica-kt 0.3.9+
- Jackson (for JSON processing)
- Spring Data JPA (optional, for JPA utilities)
- Spring Data Redis (optional, for a Redis cache writer)

## License

Apache License, Version 2.0

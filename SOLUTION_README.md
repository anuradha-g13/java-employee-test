# Employee Service Integration

####  This project provides an integration layer between a client and an external employee API service. It handles caching, retry mechanisms, and graceful degradation (fallbacks) in case of failures like rate limiting or mismatched API expectations.

# 🔧 Configuration

This solution requires a properties file with the following configuration:



    external.api.base-url=http://localhost:8112/api/v1/employee
    employee.cache.refresh-rate-ms=300000
    cache.size=500
    cache.expire.time.min=10


🔍 Property Descriptions

external.api.base-url  `The base URL of the external employee API.`

employee.cache.refresh-rate-ms `Time interval (in milliseconds) to refresh the employee cache periodically. This helps maintain up-to-date fallback data.`

cache.size  `The maximum number of employees that can be stored in the in-memory cache.`

cache.expire.time.min `Cache entry expiration time (in minutes). Controls how long an employee stays in cache before being considered stale.`

💡 Problem Solved: Rate Limiting

External APIs may randomly apply rate limiting, leading to failures even for valid requests. This project introduces two fallback levels to mitigate this:

1. Retry MechanismEach request is retried up to 3 times before failing.

2. Cache-Based FallbackIf retries fail, the system falls back to an in-memory cache of employees.

3. The cache is periodically refreshed (based on employee.cache.refresh-rate-ms).

Ensures resilience even when the external API is down.

🔥 Delete API Limitation and Workaround

The external delete API supports deletion by name, but the application needs to delete by ID. The solution works around this by:

* Looking up the employee name in the local cache using the provided ID.

* Deleting by name using the external API.

* Caveat: The external API deletes all employees matching that name (could affect multiple entries).

➡️ Response Message

The API response clearly states the deletion result:

`All Employees deleted with : {name}`

This is to ensure users are aware that multiple records may have been deleted.

🛠️ Alternative Solutions Considered

These additional strategies were considered and can be added if needed:

* Delete from Cache OnlyOnly remove the employee from the local cache without making a call to the external server.

* Reject Delete RequestsReturn a message indicating that delete functionality is currently unavailable due to API limitations.
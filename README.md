After a successful build (with Java 21) stack can be started with **docker-compose up -d** 

Swagger UI is available under: http://localhost:8080/swagger-ui/index.html#/Payments/

**Notes:** 

To address NFRs, I implemented the following strategies:

**Optimistic Locking**: Ensures data integrity by detecting conflicts during transaction commits, allowing safe retries without immediate locking.

**Idempotency Checks**: Prevents the processing of duplicate requests, ensuring that repeated operations do not produce unintended side effects.

**Outbox Pattern**: Guarantees reliable message delivery by storing messages in a dedicated outbox table within the same transactional context as business data, ensuring consistency between the database state and dispatched messages.

**API usage:** 

POST **/api/payments**

`{
"fromAccount": "22222222-2222-2222-2222-222222222222",
"toAccount": "11111111-1111-1111-1111-111111111111",
"amount": 200
}`

Note: A unique _Idempotency-Key_ in the header is **mandatory**. 


# payment-service

Beaconstone Payments. Resolves the card-network authorisation profile for a payment and
authorises it through the adapter for that network, then publishes `PaymentSucceeded` so
Invoicing can mark the invoice paid.

Kotlin / JDK 21. Upstream: `billing-service` calls this service to authorise an invoice
payment. Downstream: `invoicing-service` consumes `PaymentSucceeded`.

## Authorisation profiles

Each card network is certified against a specific authorisation profile. Adapters validate
the profile they are handed, because an uncertified profile is rejected by the acquirer in a
way that is hard to attribute after the fact.

| Network | Profile | Acquirer BIN | Capture |
|---|---|---|---|
| American Express | `amex-network` | 378282 | delayed |
| Visa | `card-default` | 445566 | immediate |
| Mastercard | `card-default` | 445566 | immediate |

American Express is the only network that requires its own profile. `authProfiles.forNetwork()`
resolves this; `authProfiles.default` returns the shared profile and is **not** valid for AMEX.

## API

| Method | Route | Purpose |
|---|---|---|
| `POST` | `/payments/authorize` | Authorise a payment for an invoice |
| `GET` | `/payments/authorizations/:authorizationId` | Read a stored authorisation |
| `GET` | `/payments/networks` | List supported card networks |
| `GET` | `/payments/invoices/:invoiceId/authorizations` | Authorisations for an invoice |
| `GET` | `/health` | Liveness |
| `GET` | `/ready` | Readiness |
| `GET` | `/metrics` | Authorisation counts by network |

### `POST /payments/authorize`

```json
{
  "paymentId": "pay_01H",
  "invoiceId": "inv_01H",
  "network": "amex",
  "amountMinor": 4200,
  "currency": "USD"
}
```

`201` returns the authorisation record. A network handed a profile it is not certified for
returns `500` with `PaymentAuthorizationConfigurationError`.

## Quick start

```bash
mvn test
mvn -q -DskipTests package
java -jar target/payment-service-4.18.0.jar
```

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | HTTP listen port |
| `SERVICE_NAME` | `payment-service` | Emitted on every log line |
| `RELEASE` | `payments-<package version>` | Release identifier in logs and telemetry. Set by the deployment; falls back to the package version locally. |
| `API_KEY` | unset | When set, requires `x-api-key` |
| `RATE_LIMIT_WINDOW_MS` | `60000` | Rate limit window |
| `RATE_LIMIT_MAX` | `100` | Requests per window |

## Architecture

```
http/HttpApp  ->  controllers/PaymentController
                    -> services/AuthorizationService
                         -> config/AuthProfiles          (profile selection)
                         -> adapters/{Amex,Visa,Mastercard}  (profile validation)
                         -> repositories/AuthorizationRepository
                         -> events/PaymentEvents         (PaymentSucceeded)
```

Authorisations are held in memory; there is no external database.

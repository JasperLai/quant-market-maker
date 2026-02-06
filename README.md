# quant-market-maker

A baseline implementation for a FX + Gold market making system.

## Implemented in this first slice

- Domain model for quotes, orders, execution reports
- In-memory order book with TTL-based quote expiration
- Separated books for domestic market-making vs offshore hedging
- Market data normalization for platform MQ and Dimple events
- Order state machine with monotonic transitions
- Pre-trade risk checks (price deviation, single-order limit, daily limit)
- Post-trade position update model
- Reconciliation job for local-vs-venue correction
- Auto-hedge strategy engine (trigger by position, VWAP + volume slicing, PnL gate wait)
- Lightweight HTTP query API for health/order/position/book data
- Unit tests for core behaviors

## Build

```bash
mvn test
```

## Run

```bash
mvn -q -DskipTests package
java -cp target/quant-market-maker-0.1.0-SNAPSHOT.jar com.mubin.quant.App
```

## HTTP API

- `GET /health`
- `GET /orders?id=<orderId>`
- `GET /positions?symbol=<symbol>`
- `GET /books?type=DOMESTIC_MM|OFFSHORE_HEDGE&symbol=<symbol>&side=BUY|SELL&depth=5`

## Defaults

- FX price deviation threshold: 15 bps
- Gold price deviation threshold: 0.30%
- Single order notional limit: 5,000,000
- FX daily limit: 100,000,000
- Gold daily limit: 1,000,000 grams (1 ton)

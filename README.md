## Distributed Notification Processing & Traffic Simulation Platform

A backend engineering project that incrementally builds a **high-throughput, fault-aware notification processing system**, evolving from in-memory queues to cloud-native distributed infrastructure.

The project is developed **phase by phase** to mirror how real production systems are designed, stressed, and hardened under load.

---

## Problem Statement

Notification systems (OTP, alerts, marketing messages) must handle:

* Sudden traffic spikes
* Slow or failing downstream providers
* Backpressure without crashing
* Eventual retries and dead-letter handling

Synchronous processing breaks under load.
This project focuses on **decoupling ingestion from processing** and progressively introducing reliability and scale.

---

## Current Status

**Phase 1 — Minimal Asynchronous Decoupling (Completed)**

The system accepts notification requests and places them into a bounded in-memory queue, returning immediately to the client without blocking on processing.

---

## Phase 1 Architecture

```
Client
  |
  v
POST /notification
  |
  v
NotificationController
  |
  v
InMemoryNotificationQueue (BlockingQueue, bounded)
```

---

## Core Design (Phase 1)

### Asynchronous Ingestion

* Requests are accepted via REST
* Notifications are **enqueued**, not processed synchronously
* This prevents slow consumers from impacting API latency

### Bounded Queue (Backpressure)

* `LinkedBlockingQueue` with fixed capacity
* When full, requests are **explicitly rejected**
* Prevents unbounded memory growth

This mimics how real systems behave under overload.

---

## API

### POST `/notification`

**Request**

```json
{
  "type": "EMAIL",
  "recipient": "user@test.com",
  "message": "Hello"
}
```

**Responses**

* `202 Accepted` → notification queued
* `429 Too Many Requests` → system overloaded (queue full)

---

## Load Testing (Phase 1)

A custom Java-based load simulator was used to stress the ingestion layer.

### Test Configuration

* Total requests: **10,000**
* Max concurrent in-flight requests: **50**
* Queue capacity: **1,000**
* No consumer draining the queue

### Result

```
========== LOAD TEST RESULT ==========
Total Requests: 10000
Accepted (2xx): 1455
Rejected (429): 8545
Errors: 0
Time Taken(ms): 3398
Throughput (req/sec): ~2942
```

---

## Observations

* Ingestion remained stable under burst traffic
* System rejected excess load **gracefully**
* No thread exhaustion or application crashes
* Backpressure behavior is visible and intentional

This validates the **producer–consumer decoupling** model.

---

## What Phase 1 Demonstrates

* Producer–Consumer pattern
* Asynchronous request handling
* Explicit overload protection
* Stable ingestion under high traffic

---

## Known Limitations (Intentional)

Phase 1 does **not** include:

* Retry or backoff logic
* Dead Letter Queue (DLQ)
* Persistence
* Metrics or observability

These are introduced incrementally in later phases.

---

## Tech Stack

* Java
* Spring Boot
* BlockingQueue (LinkedBlockingQueue)
* Java HttpClient (load testing)

---

## Engineering Philosophy

* Design for failure, not happy paths
* Make overload visible
* Prefer explicit backpressure over silent degradation
* Evolve systems incrementally

---

### Status: Actively evolving

Each phase is added deliberately to expose real backend challenges and trade-offs.

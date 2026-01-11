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

## Phase 1 — Minimal Asynchronous Decoupling (Completed)

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


## Phase-2 Architecture 

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
InMemoryNotificationQueue (bounded)
  |
  v
NotificationDispatcher (single thread)
  |
  v
WorkerPool (ThreadPoolExecutor, bounded)
  |
  v
Notification Processing
```

This architecture directly maps to:

* Kafka consumer group model
* SQS poller + ECS worker tasks
* RabbitMQ worker queues

---

## Core Design Evolution

### Phase-1: Asynchronous Ingestion

* Requests are accepted via REST
* Notifications are **enqueued**, not processed synchronously
* API latency remains stable even if processing is slow

### Phase-1 Backpressure

* Bounded queue prevents memory blow-up
* Queue full → HTTP `429 Too Many Requests`
* Overload is **explicit**, not silent

---

### Phase-2: Dispatcher + Worker Pool (Critical Upgrade)

Naive approach (intentionally avoided):

```java
new Thread(() -> {
    while (true) {
        process();
    }
}).start();
```

This is:

* Unbounded
* Impossible to tune
* Not observable
* Unsafe under load

---

### Phase-2 Correct Model

```java
ExecutorService workerPool = new ThreadPoolExecutor(
    10,                      // core threads
    20,                      // max threads
    60, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(500),
    new ThreadPoolExecutor.CallerRunsPolicy()
);
```

A **single dispatcher thread** pulls from the queue and submits tasks to the worker pool.

---

## Load Testing (Phase-2)

A custom Java-based load simulator was used to stress the system end-to-end at the ingestion layer.

### Test Configuration

* Total requests: **10,000**
* Client concurrency: **50**
* Bounded ingestion queue
* Dispatcher + bounded worker pool
* Processing delay simulated (~100 ms)

---

### Phase-2 Results

```
========== LOAD TEST RESULT ==========
Total Requests: 10000
Accepted (2xx): 1940
Rejected (429): 8060
Errors: 0
Time Taken(ms): 3620
Throughput (req/sec): 2762
```

## Phase 3 – Slow Downstream Simulation & Backpressure Validation

In this phase, I simulated **real-world downstream latency and failures** to observe how the system behaves under stress.

### What was added

* Introduced **artificial latency (50–350ms)** in the Email notification provider to mimic slow external services.
* Simulated **random failures (~20%)** during notification delivery.
* Kept the queue **bounded** and executor **thread-limited** to enforce backpressure.
* Ensured the API responds quickly with **accept or reject** without blocking on processing.

### Goal

To validate that:

* Slow providers reduce effective processing capacity
* The system applies backpressure instead of failing
* Requests are rejected early when the system is overloaded

### Load Test Result

```
Total Requests: 10000
Accepted (2xx): 1359
Rejected (429): 8641
Errors: 0
Time Taken(ms): 2299
Throughput (req/sec): 4349
```

### Key Observations

* High rejection rate confirms **early backpressure** under slow downstream conditions.
* Zero errors indicate **system stability** despite latency and failures.
* Higher throughput reflects **fast rejection**, not faster processing.
* Processing continues asynchronously after HTTP responses are returned.

This phase demonstrates how downstream latency directly impacts system capacity and why early rejection is critical for stability.

---

## Phase-4: Retry Mechanism, Dead Letter Queue & System Visibility

### Objective

Enhance the notification processing pipeline with **fault tolerance**, **controlled retries**, and **operational visibility** while maintaining system stability under heavy load.

---

### Key Enhancements

#### Bounded Retry Mechanism

* Failed notification deliveries are retried up to a **configured maximum retry limit**
* Retries are re-enqueued instead of blocking worker threads
* Prevents infinite retry loops and retry storms during high failure rates

**Observed Behavior**

* Retry attempts increased gradually during load
* Retries were spread over time, avoiding bursts
* System remained stable even with intentional provider failures

---

#### Dead Letter Queue (DLQ)

* Notifications exceeding the retry limit are moved to a **Dead Letter Queue**
* DLQ isolates permanently failing notifications from the main pipeline
* DLQ consumer logs failed notification IDs for post-mortem analysis

**Observed Behavior**

* Only a small number of notifications reached the DLQ
* DLQ growth occurred strictly after retry exhaustion
* Main processing pipeline remained unaffected

---

#### Worker Pool & Backpressure Handling

* Fixed-size worker pool processes notifications concurrently
* Internal bounded queue absorbs bursts when workers are saturated
* Protects the system from thread exhaustion and overload

**Observed Behavior**

* Worker pool consistently hit max utilization under load
* Queue size increased temporarily and drained smoothly after load completion
* No thread leaks or deadlocks observed

---

#### System Metrics & Observability

A periodic system reporter logs real-time operational metrics:

* Active worker count
* Queue depth
* Completed task count
* Total retry attempts
* Successful deliveries
* Failed attempts
* DLQ size

These metrics provided a **clear timeline view** of system behavior during stress testing.

---

### Load Test Result (Phase-4)

```
========== LOAD TEST RESULT ==========
Total Requests: 10000
Accepted (2xx): 1674
Rejected (429): 8326
Errors: 0
Time Taken(ms): 2444
Throughput (req/sec): 4091
```

**Interpretation**

* High rejection count confirms effective **backpressure** via bounded queues
* Zero errors indicate system stability under overload
* High throughput shows fast ingestion while processing remains controlled
* Retries and DLQ ensured failures were handled safely without meltdown

---

### Outcome

Phase-4 upgrades the system into a **resilient, production-ready asynchronous pipeline** capable of:

* Handling transient failures with delayed retries
* Isolating permanent failures using DLQ
* Maintaining stability under overload
* Providing actionable observability for operators

---

### What This Phase Demonstrates

* Why retries must be **bounded and delayed**
* How DLQs protect core processing paths
* How backpressure prevents cascading failures
* How metrics reveal real system behavior under load

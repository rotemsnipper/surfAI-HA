# Data Transformer - User Data Processing Pipeline

A high-performance, concurrent data transformation pipeline for processing and transforming user data from JSON files into a structured format optimized for database loading.

## Overview

This application reads user data from JSON files, transforms it into a standardized format, and outputs the results as JSON files ready for database import. The solution implements a generic, extensible framework with parallel processing capabilities.

## Requirements Met

### ✅ Functional Requirements
- **Data Reading**: Efficiently reads user data from multiple JSON files
- **Data Transformation**: Transforms raw user data into the required structured format
- **Data Storage**: Outputs transformed data as JSON files formatted for database loading
- **Parallel Processing**: Concurrent file processing using thread pools
- **Generic Framework**: Extensible architecture supporting different input types, transformations, and output formats
- **Unit Tests**: Comprehensive test coverage for all components

### ✅ Output Format

The application transforms user data into the following structure:

```json
{
  "Id": "unique-user-id",
  "external_id": "user-principal-name",
  "mail": "user@example.com",
  "type": "Member",
  "location": "US",
  "is_enabled": true,
  "first_name": "John",
  "last_name": "Doe",
  "lastSignInDateTime": "2024-01-15T10:30:00Z",
  "lastSuccessfulSignInDateTime": "2024-01-15T10:30:00Z",
  "lastNonInteractiveSignInDateTime": "2024-01-15T10:30:00Z"
}
```

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Building the Application

```bash
cd data-transformer
mvn clean install
```

This will:
1. Compile the source code
2. Run all unit tests (8 tests across 4 test classes)
3. Package the application as an executable JAR

### Running the Application

```bash
java -jar target/data-transformer-1.0-SNAPSHOT.jar --input.dir=input --output.dir=output
```

**Parameters:**
- `--input.dir`: Directory containing JSON files to process
- `--output.dir`: Directory where transformed JSON files will be written

### File Lifecycle

The application manages files through the following lifecycle:

1. **Input Directory**: Place JSON files here for processing
2. **Processing**: Files are read, transformed, and written to the output directory
3. **Processed Directory**: Successfully processed files are moved to `input/processed/`
4. **Failed Directory**: Files that fail processing are moved to `input/failed/`

## Architecture

### Design Principles

The solution follows a **generic, pipeline-based architecture** with clear separation of concerns:

```
Source → Transformer → Sink
```

### Core Components

#### 1. **Generic Interfaces**

**`Source<I, C>`**: Abstracts data reading
- `I`: Input type (e.g., `RawUser`)
- `C`: Context type (e.g., `Path` for file-based sources)
- Enables streaming data processing with `Stream<I> read(C context)`

**`Transformer<I, O>`**: Abstracts data transformation
- `I`: Input type
- `O`: Output type
- Pure transformation logic: `O transform(I input)`

**`Sink<O, C>`**: Abstracts data writing
- `O`: Output type
- `C`: Context type
- Handles output persistence: `void write(Stream<O> data, C context)`

#### 2. **Concrete Implementations**

**`JsonFileSource`**: Reads JSON files and streams `RawUser` objects
- Uses Jackson for efficient JSON parsing
- Streams data to minimize memory footprint

**`UserTransformer`**: Transforms `RawUser` to `TargetUser`
- Maps all required fields
- Handles nested `signInActivity` object
- Null-safe transformations

**`JsonFileSink`**: Writes transformed data to JSON files
- Batches writes for efficiency
- Pretty-prints JSON for readability

**`PipelineOrchestrator`**: Coordinates the entire pipeline
- Manages concurrent file processing
- Implements retry logic with exponential backoff
- Tracks metrics (success/failure counts)
- Handles file lifecycle (processed/failed directories)

### Concurrency Model

The application implements **file-level parallelism**:

- **Thread Pool**: Configurable size (defaults to number of CPU cores)
- **Bounded Queue**: Prevents memory exhaustion (default capacity: 100)
- **Backpressure**: `CallerRunsPolicy` ensures the main thread helps when queue is full
- **Graceful Shutdown**: Waits for in-flight tasks to complete

**Configuration** (via `application.properties`):
```properties
pipeline.thread-pool.size=8           # Number of worker threads
pipeline.queue.capacity=100           # Task queue capacity
pipeline.batch.size=1000              # Records per batch
```

### signInActivity Fields

The sign-in activity fields are now flattened at the root level:
- `lastSignInDateTime`: ISO 8601 timestamp of last sign-in
- `lastSuccessfulSignInDateTime`: ISO 8601 timestamp of last successful sign-in
- `lastNonInteractiveSignInDateTime`: ISO 8601 timestamp of last non-interactive sign-in

**Rationale:**
- **Database-friendly**: Flat structure is easier to insert into relational databases without JSON parsing
- **Queryable**: Enables direct querying and indexing on sign-in timestamps
- **Extensible**: Can add more sign-in related fields easily
- **Null-safe**: Uses `@JsonInclude(JsonInclude.Include.NON_NULL)` to exclude null values

### Monitoring & Observability

The application integrates **Micrometer** for metrics:
- `files.processed.success`: Counter for successfully processed files
- `files.processed.failure`: Counter for failed files

These metrics can be exported to monitoring systems (Prometheus, Grafana, etc.) for production observability.

## Key Architectural Decisions

### 1. **Streaming vs. Batch Loading**

**Decision**: Use Java Streams with batching

**Rationale**:
- Streams minimize memory usage for large files
- Batching (default: 1000 records) balances memory and I/O efficiency
- Avoids loading entire files into memory

**Tradeoff**: Slightly more complex code vs. better memory efficiency

### 2. **File-Level vs. Record-Level Parallelism**

**Decision**: File-level parallelism

**Rationale**:
- Simpler implementation and debugging
- Natural unit of work (each file is independent)
- Easier file lifecycle management
- Good performance for multiple files

**Tradeoff**: Single large file won't benefit from parallelism (addressed in scalability section)


### 3. **Retry Logic with Exponential Backoff**

**Decision**: Implement `@Retryable` with 3 attempts and exponential backoff

**Rationale**:
- Handles transient failures (network issues, temporary file locks)
- Exponential backoff prevents overwhelming failing resources
- Automatic recovery from temporary issues

**Tradeoff**: Slower failure detection for permanent errors

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PipelineOrchestratorTest

# Run with coverage report
mvn test jacoco:report
```

## Project Structure

```
data-transformer/
├── src/
│   ├── main/
│   │   └── java/com/datatransformer/
│   │       ├── DataTransformerApplication.java    # Main entry point
│   │       ├── component/                         # Concrete implementations
│   │       │   ├── JsonFileSource.java
│   │       │   ├── JsonFileSink.java
│   │       │   └── UserTransformer.java
│   │       ├── model/                             # Data models
│   │       │   ├── RawUser.java
│   │       │   ├── RawSignInActivity.java
│   │       │   ├── TargetUser.java
│   │       │   └── TargetSignInActivity.java
│   │       ├── pipeline/
│   │       │   └── interfaces/                    # Generic interfaces
│   │       │       ├── Source.java
│   │       │       ├── Transformer.java
│   │       │       └── Sink.java
│   │       └── service/
│   │           └── PipelineOrchestrator.java      # Pipeline coordinator
│   └── test/
│       └── java/com/datatransformer/
│           ├── component/                         # Component tests
│           └── service/                           # Service tests
├── input/                                         # Input directory
│   ├── processed/                                 # Successfully processed files
│   └── failed/                                    # Failed files
├── output/                                        # Transformed output files
├── pom.xml                                        # Maven configuration
└── README.md                                      # This file
```

## Configuration

The application can be configured via `application.properties` or command-line arguments:

### Thread Pool Configuration

```properties
# Number of worker threads (default: number of CPU cores)
pipeline.thread-pool.size=8

# Task queue capacity (default: 100)
pipeline.queue.capacity=100

# Batch size for processing records (default: 1000)
pipeline.batch.size=1000
```

### Retry Configuration

Retry behavior is configured via annotations in `PipelineOrchestrator`:
- **Max Attempts**: 3
- **Initial Delay**: 1000ms
- **Backoff Multiplier**: 2x

## Optimizations Implemented

1. **Streaming**: Processes records one at a time without loading entire files
2. **Batching**: Groups records for efficient I/O operations
3. **Parallel Processing**: Concurrent file processing using thread pools
4. **Bounded Resources**: Prevents memory exhaustion with bounded queues
5. **Lazy Evaluation**: Uses Java Streams for lazy data processing

---

## Optional: Multi-Core & Multi-Server Scalability

To scale this solution across multiple servers and cores, the following enhancements would be implemented:

### 1. Message Queue Architecture

Use Kafka or RabbitMQ to distribute work across multiple workers:

```
Producer → Message Queue → Multiple Consumer Workers → Output Store
```

- Producer publishes file references or individual records to the queue
- Multiple consumer workers process messages in parallel
- Enables horizontal scaling by adding more workers
- Provides fault tolerance through message reprocessing

### 2. Containerization with Docker

Package the application as a Docker container:

- Simplifies deployment and dependency management
- Enables easy scaling and version management
- Provides isolation and resource control

### 3. Orchestration with Kubernetes or Spark

Deploy containers using orchestration platforms:

**Kubernetes Approach**:
- Deploy worker pods that auto-scale based on queue depth
- Horizontal Pod Autoscaler adjusts worker count dynamically
- Built-in fault tolerance and health monitoring
- Suitable for continuous processing workloads

**Apache Spark Approach**:
- Use Spark for very large datasets requiring massive parallelism
- Distribute processing across hundreds of nodes
- Built-in data partitioning and shuffle operations
- Ideal for batch processing at scale

### 4. Distributed Storage

- All workers can access the same input/output locations
- Enables stateless workers that scale independently
- Provides durability and high availability

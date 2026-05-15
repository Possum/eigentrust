# possum.eigentrust

Eigentrust library for Clojure

A simple, clean, and strictly deterministic Clojure implementation of the **EigenTrust** algorithm.

This library computes global reputation scores in a network based on local trust relationships.
Unlike naive implementations that suffer from floating-point non-determinism, `possum.eigentrust`
guarantees identical mathematical outputs across different CPU architectures, making it safe for
distributed consensus systems, decentralized applications, and federated networks.

## Features

* **Strict Determinism:** Uses `java.math.BigDecimal` with a fixed scale of `10` and explicit half-up
  rounding rules. This completely eliminates architecture-specific floating-point drift (e.g., x86
  vs. ARM).
* **Automatic Vector Normalization:** Accepts raw peer weights (e.g., `[1 1 0]`) for the pre-trust
  seed vector; the library automatically handles balancing and scale normalization under the
* **Minimal API:** Exposes exactly one public function to keep your integration clean.
* **Dead-End Resolution:** Automatically routes nodes that trust no one back to the pre-trust seed
  vector, ensuring stable mathematical convergence.
* **Configurable:** Fully parameterized via an optional configuration map.

## Installation

Add the following dependency to your `deps.edn` or `project.clj`:

### Deps (deps.edn)

```clojure
io.github.possum/eigentrust {:mvn/version "0.1.0"}
```

### Leiningen (project.clj)

```clojure
[io.github.possum/eigentrust "0.1.0"]
```

## Quick Start

```clojure
(ns example.core
  (:require [possum.eigentrust :as et]))

;; Define your network topology as an outgoing trust matrix. Rows represent the trustor, columns
;; represent the trustee. In this network, Node 2 is a "dead end" (trusts no one).
(def trust-matrix
  [[0  10 10]   ; Node 0 trusts Node 1 and Node 2
   [5   0  0]   ; Node 1 trusts Node 0
   [0   0  0]]) ; Node 2 trusts nobody (dead-end)

;; Define your pre-trust vector (trusted peer seeds). You can pass raw weights; the library automatically normalizes them!
(def pre-trust [1 1 0])

;; Calculate the global reputation scores
(et/calculate-scores trust-matrix pre-trust)
;; => [0.4418604651M 0.4651162791M 0.0930232558M]
```

## Advanced Configuration

You can pass an optional configuration map as the third argument to customize the convergence
thresholds and behavior:

```clojure
(et/calculate-scores trust-matrix pre-trust {:alpha 0.15M
                                             :epsilon 0.00001M
                                             :max-iterations 100})
```

| Option | Default | Description |
| --- | --- | --- |
| `:alpha` | `0.1M` | The pre-trust dampening factor. Controls the probability that a node will reset back to the pre-trust seeds during the random walk. |
| `:epsilon` | `0.0001M` | The convergence threshold delta. Iteration stops when the delta between steps falls below this value. |
| `:max-iterations` | `50` | The hard limit on power iteration loops to prevent infinite execution on pathological matrices. |

## Why Cross-Platform Determinism Matters

In standard peer-to-peer reputation scoring or blockchain mechanics, multiple independent machines
must calculate identical state mutations.

Unlike naive implementations of the power iteration method that rely on native floating-point
primitives (float/double), possum.eigentrust utilizes strict java.math.BigDecimal arithmetic
with an unyielding fixed scale. Native primitive types utilize hardware execution pipelines (IEEE 754).
An x86 CPU performing intermediate operations inside an 80-bit registry will truncate values differently
than an ARM chip executing raw 64-bit instructions. Over many iterations of power-method
multiplication, these microscopic variations compound. One machine may exit the loop on iteration
12, while another exits on iteration 13, leading to a catastrophic network state split.

possum.eigentrust completely executes its arithmetic in software via a fixed decimal scale.
Running this library on a local developer laptop will yield the exact same byte-for-byte state as
running it on an enterprise cloud instance or a globally distributed validator node—making it fully
safe for consensus networks, distributed ledgers, and heterogeneous server clusters.

## Roadmap

* [ ] EigenTrust++ Extension: Introduce an alternative :algorithm :eigentrust-plus option based on
  the Fan-Liu framework to factor in Recommendation Credibility, heavily boosting the library's
  native Sybil-attack resistance.
* [ ] **Performance optimizations:** Add support for highly parallelized execution layouts for massive graph scale optimization.

## License

Copyright © 2026 Possum

Distributed under the MIT License.

## Acknowledgments & Background

This project was originally conceptualized and developed as part of an advanced software development
project within **CS 6675: Advanced Internet Systems and Applications** at the **Georgia Institute of
Technology**.

### Academic Integrity Disclaimer
If you are a current or future student at Georgia Tech taking CS 6675, CS 4675, or any other
computing course, please note the following boundaries regarding academic integrity:

* **Using this library as a dependency:** You are fully permitted to include `possum.eigentrust` as
  an external project dependency (via `deps.edn` or `project.clj`) to back larger, more complex
  system architectures required by your course deliverables.
* **Source code plagiarism:** Copying, adapting, or rewriting the internal source code, core math
  functions, or internal validation algorithms of this library and presenting them as your own
  custom course implementation constitutes a direct violation of the [Georgia Tech Academic Honor
  Code](https://osi.gatech.edu/degree-students/honor-code). 

This library represents an independent, production-grade open-source spin-off decoupled from
internal course frameworks. Plagiarism of its source files will be subject to direct referral to the
Office of Student Integrity.

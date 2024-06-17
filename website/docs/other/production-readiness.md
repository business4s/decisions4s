---
sidebar_position: 2
---

# Production Readiness

`Decisions4s` can be considered production-ready for **application usage**.

* All the functionalities are well tested and safe to use in your code.
    * The surface area is small by design and functionalities are fully deterministic.
* We provide **no backward-compatibility guarantees** for 0.x series.
    * Source-compatibility will be handled through a best-effort approach, we will try to use deprecations whenever
      possible, but we keep the right to break the API if needed.
    * Binary-compatibility is not provided for now. This means we do not recommend building _libraries_ based
      on `Decisions4s`. It has no implications for _application_ use-cases.

---
sidebar_position: 5
---

# Diagnostics

`Decisions4s` aims to help not only evaluate and visualize the decisions but also to understand them.

Results of evaluation carry all the information needed, such as:

* Table that was evaluated
* Input that was provided
* Results of individual rules

```scala file=./main/scala/decisions4s/example/docs/DiagnosticsExample.scala start=start_diagnose end=end_diagnose
```

Which produces a debug string containing
* Name of the decision
* Hit policy
* Result of the evaluation
* Input used
* All the evaluated rules, including:
  * Their predicates
  * Predicates' results (satisfied or not)
  * Rule output

Example: 
```text file=./test/resources/docs/pull-request-diagnostics.txt
```


## Customization

Currently, there is no option to customize the message. It should be fully possible to define a custom format on the
user side, by replicating the logic from `DiagnosticsPrinter`. If you believe it's worth exposing some cusomization
options from the library itself, or to modify the format in general, please reach out!
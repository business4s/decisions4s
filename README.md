# decisions4s

This repository contains an experimental approach to decision making. 
It can be seen as an alternative to Rules Engines or DMN.

The approach taken relies on higher-order data and follows DMN evaluation model, where each decision has specific 
input & output types and set of rules that match on the input and produce the output. 

TODO:
* [x] Basic decision evaluation
* [ ] Rendering DMN
* [ ] Deriving functorK for case classes (https://github.com/typelevel/cats-tagless/issues/29)
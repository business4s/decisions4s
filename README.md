# Decisions4s
![Discord](https://img.shields.io/discord/1240565362601230367?style=flat-square&logo=discord&link=https%3A%2F%2Fbit.ly%2Fbusiness4s-discord)

This repository contains an experimental approach to decision making. 
It can be seen as an alternative to Rules Engines or DMN.

The approach taken relies on higher-order data and follows DMN evaluation model, where each decision has specific 
input & output types and set of rules that match on the input and produce the output. 

TODO:
* [x] Basic decision evaluation
* [x] Rendering DMN
* [x] Deriving functorK for case classes (https://github.com/typelevel/cats-tagless/issues/29)

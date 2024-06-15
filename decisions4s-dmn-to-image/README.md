This module relies on the `js-bundle` being generated into `src/resources` and as such captured inside the scala
artifact. To trigger the generation run

```
cd js-bundle
yarn install
yarn run build
```
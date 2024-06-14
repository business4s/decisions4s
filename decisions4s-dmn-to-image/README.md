This folder contains an attempt to embed image rendering in a scala library. It doesn't work but can be used as a
baseline to pursue this approach further.

The last problem observed problem was lack of styles in pupeteer browser after rendering the dmn.

It works as follows:

1. `js-bundle` defines javascript files used for rendering
    1. `server.ts` - responsible for running `pupeteer` from node environment
    2. `web.ts` - responsible for running dmn-js from within pupeteer browser
2. `server.ts` is bundled (via webpack) as commonjs library module
    1. it can be run via `node test.js`
3. `web.ts` is bundled (via webpack) as ES module and used from `skeleton.html`
4. `scala-wrapper` was meant to use the server bundle from within graalvm polyglot context
    1. this never happened because I wasn't able to run it successfully even from the node. 
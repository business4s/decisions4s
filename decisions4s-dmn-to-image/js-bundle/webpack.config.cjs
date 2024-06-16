const path = require('path');
const CopyPlugin = require("copy-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

const outputDir = path.resolve(__dirname, '../src/main/resources/generated-web-bundle')

const webConfig = {
    name: 'web',
    target: 'web',
    entry: './src/web.ts',
    mode: 'production',
    module: {
        rules: [
            {
                test: /\.ts$/,
                use: 'ts-loader',
                exclude: /node_modules/,
            },
        ],
    },
    resolve: {
        extensions: ['.ts', '.js'],
    },
    output: {
        path: outputDir,
        filename: 'web_bundle.js',
        library: {
            type: 'module'
        }
    },
    experiments: {
        outputModule: true
    },
    plugins: [
        new CopyPlugin({
            patterns: [
                {from: "src/skeleton.html", to: "index.html"},
            ],
        }),
    ]
};

const cssConfig = {
    name: 'css',
    entry: './src/css-entry.js',
    mode: 'production',
    output: {
        path: outputDir,
        filename: 'css-bundle.js', // This will not actually generate a JS file, but we need to specify it
    },
    module: {
        rules: [
            {
                test: /\.css$/i,
                use: [MiniCssExtractPlugin.loader, 'css-loader'],
            },
        ],
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: 'bundle.css', // Output CSS file
        }),
    ],
};


module.exports = [webConfig, cssConfig];

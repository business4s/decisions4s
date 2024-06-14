const path = require('path');
const CopyPlugin = require("copy-webpack-plugin");


const serverConfig = {
    name: 'server',
    target: 'node',
    mode: 'development',
    entry: './src/server.ts',
    module: {
        rules: [
            {
                test: /\.ts$/,
                use: {
                    loader: 'ts-loader',
                    options: {
                        configFile: 'tsconfig.server.json'
                    }
                },
                exclude: /node_modules/,
            },
        ],
    },
    resolve: {
        extensions: ['.ts', '.js'],
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'server_bundle.js',
        libraryTarget: 'commonjs2' // Output as CommonJS2
    },
    plugins: [
        new CopyPlugin({
            patterns: [
                {from: "src/skeleton.html", to: "skeleton.html"},
            ],
        }),
    ]
};

const webConfig = {
    name: 'web',
    target: 'web',
    entry: './src/web.ts',
    module: {
        rules: [
            {
                test: /\.ts$/,
                use: {
                    loader: 'ts-loader',
                    options: {
                        configFile: 'tsconfig.web.json'
                    }
                },
                exclude: /node_modules/,
            },
        ],
    },
    resolve: {
        extensions: ['.ts', '.js'],
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'web_bundle.js',
        library: {
            type: 'module'
        }
    },
    experiments: {
        outputModule: true
    }
};

module.exports = [serverConfig, webConfig];

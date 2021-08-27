'use strict';
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const {CleanWebpackPlugin} = require('clean-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

let basePath = path.join(__dirname, '/src/main/webapp/');
let fs = require('fs');

const MATOMO_VERSION = process.env.MATOMO_VERSION
  ? process.env.MATOMO_VERSION
  : 'Test';

let config = {
  entry: {
    main: basePath + 'resources/app/js/main.js',
    manual: basePath + 'resources/app/js/manual.js'
  },

  output: {
    // Output directory
    path: basePath + 'WEB-INF/views/dist',
    filename: '[name].bundle.js',
    chunkFilename: '[name].bundle.js',
    publicPath: ''
  },

  module: {
    rules: [
      {test: /\.md$/, loader: 'null-loader'},
      {
        test: /\.js$/,
        use: [
          {
            loader: 'angular1-templateurl-loader'
          }
        ],
        exclude: [/(.*)\/angular-foundation-6\/(.*)/] // uses $templatecache so don't replace
      },
      {
        test: /\.html$/,
        loader: 'raw-loader',
        options: {
          esModule: false
        }
      },
      {
        test: /.(ttf|otf|eot|woff(2)?)(\?[a-z0-9]+)?$/,
        use: [
          {
            loader: 'file-loader',
            options: {
              name: '[name].[ext]',
              outputPath: '/fonts/', // where the fonts will go
              publicPath: '/fonts/' // override the default path
            }
          }
        ]
      },
      {
        test: /\.(png|jp(e*)g|svg|gif)$/,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 8000, // Convert images < 8kb to base64 strings
              name: 'images/[hash]-[name].[ext]'
            }
          }
        ]
      }
    ]
  },

  resolve: {
    alias: {
      'schema-basePath': 'mcda-web/../schema',
      'gemtc-web': 'gemtc-web/app',
      'mcda-web': 'mcda-web/app',
      app: basePath + 'resources/app/js/app'
    },
    modules: [
      // Files path which will be referenced while bundling
      'node_modules',
      basePath + 'resources/app'
    ],
    extensions: ['.css', 'html', '.js', '.json'] // File types
  },

  plugins: [
    new HtmlWebpackPlugin({
      filename: 'index.html',
      template: basePath + 'WEB-INF/templates/index.ejs',
      inject: 'head',
      chunks: ['main'],
      matomo: fs.readFileSync(
        require.resolve(
          basePath + 'WEB-INF/templates/matomo' + MATOMO_VERSION + '.html'
        )
      )
    }),
    new HtmlWebpackPlugin({
      filename: 'manual.html',
      template: basePath + 'resources/manual.ejs',
      inject: 'head',
      chunks: ['manual'],
      matomo: fs.readFileSync(
        require.resolve(
          basePath + 'WEB-INF/templates/matomo' + MATOMO_VERSION + '.html'
        )
      )
    }),
    new CleanWebpackPlugin({verbose: true}),
    new CopyWebpackPlugin({
      patterns: [
        {from: 'node_modules/gemtc-web/public/img', to: 'images/gemtc-web'},
        {from: 'node_modules/mcda-web/public/img', to: 'images/mcda-web'}
      ]
    })
  ],

  optimization: {
    splitChunks: {
      chunks: 'all',
      name: false
    }
  }
};

module.exports = config;

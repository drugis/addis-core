'use strict';
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');
let basePath = path.join(__dirname, '/src/main/webapp/');

let config = {
  entry: {
    'main': basePath + 'resources/app/js/main.js',
    'manual': basePath + 'resources/app/js/manual.js'
  },

  output: {
    // Output directory
    path: basePath + 'WEB-INF/views/dist',
    filename: '[name].bundle.js',
    chunkFilename: '[name].bundle.js'
  },

  module: {
    rules: [{
      test: /\.js$/,
      use: [{
        loader: 'angular1-templateurl-loader'
      }],
      exclude: [/(.*)\/angular-foundation-6\/(.*)/] // uses $templatecache so dont replace 
    }, {
      test: /\.html$/,
      loader: 'raw-loader'
    }, {
      test: /.(ttf|otf|eot|woff(2)?)(\?[a-z0-9]+)?$/,
      use: [{
        loader: 'file-loader',
        options: {
          name: '[name].[ext]',
          outputPath: 'fonts/', // where the fonts will go
          publicPath: 'fonts/' // override the default path
        }
      }]
    }, {
      test: /\.(png|jp(e*)g|svg)$/,
      use: [{
        loader: 'url-loader',
        options: {
          limit: 8000, // Convert images < 8kb to base64 strings
          name: 'images/[hash]-[name].[ext]'
        }
      }]
    }]
  },

  resolve: {
    alias: {
      'gemtc-web': 'gemtc-web/app',
      'mcda-web': 'mcda-web/app',
      'app': basePath + 'resources/app/js/app',
      'angular-patavi-client': 'angular-patavi-client/patavi',
      'error-reporting': 'error-reporting/errorReportingDirective',
      'export-directive': 'export-directive/export-directive',
      'help-popup': 'help-popup/help-directive'
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
      template: basePath + 'WEB-INF/templates/index.html',
      inject: 'head',
      chunks: ['main']
    }),
    //    new HtmlWebpackPlugin({
    //      filename: 'signin.html',
    //      template: 'app/signin.html',
    //      inject: 'head',
    //      chunks: ['signin']
    //    }),
    new HtmlWebpackPlugin({
      filename: 'manual.html',
      template: basePath + 'resources/manual.html',
      inject: 'head',
      chunks: ['manual']
    }),
    new CleanWebpackPlugin(['WEB-INF/views/dist'])
  ],

  optimization: {
    splitChunks: {
      chunks: 'all',
      name: false
    }
  }
};

module.exports = config;

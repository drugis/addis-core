'use strict';

 const merge = require('webpack-merge');
 const common = require('./webpack.common.js');

 const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

 let config = merge(common, {
   mode: 'development',
   devtool: 'inline-source-map',
   module: {
    rules: [
      {
        test: /\.css$/,
        loaders: ['style-loader', 'css-loader']
      }
    ]
  }, 
  plugins: [
//    new BundleAnalyzerPlugin()
  ]
 });
module.exports = config;

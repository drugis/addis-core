'use strict';

const webpackConfig = require("./webpack.dev");
delete webpackConfig.entry;
webpackConfig.plugins = [];
webpackConfig.optimization = {
    splitChunks: false,
    runtimeChunk: false
  };
webpackConfig.resolve.alias['test-resources'] = __dirname + '/src/test';
webpackConfig.module.rules.push({ test: /\.xlsx$/, loader: 'webpack-xlsx-loader' });

module.exports = function(config) {
  config.set({

    webpack: webpackConfig,

    // base path, that will be used to resolve files and exclude
    basePath: '.',

    // plugins to load
    plugins: [
      'karma-firefox-launcher',
      'karma-chrome-launcher',
      'karma-junit-reporter',
      'karma-jasmine',
      'karma-webpack',
      'karma-sourcemap-loader'
    ],

    preprocessors: {
      // add webpack as preprocessor
      'src/test-main.js' : ['webpack', 'sourcemap']
    },

    // frameworks to use
    frameworks: ['jasmine'],


    // list of files / patterns to load in the browser
    files: [
      'src/test-main.js'
    ],

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit'],
    junitReporter: {
      outputFile: './karma-test-results.xml',
      outputDir: '.'
    },

    // web server port
    port: 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_WARN,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera (has to be installed with `npm install karma-opera-launcher`)
    // - Safari (only Mac; has to be installed with `npm install karma-safari-launcher`)
    // - PhantomJS
    // - IE (only Windows; has to be installed with `npm install karma-ie-launcher`)
    browsers: ['ChromeHeadless'],


    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 10000,


    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false
  });
};

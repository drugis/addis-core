// Karma configuration
module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '../../../src/main/webapp/resources/',

    // plugins to load
    plugins : [
      'karma-phantomjs-launcher',
      'karma-jasmine',
      'karma-requirejs',
    ],

    // frameworks to use
    frameworks: ['jasmine', 'requirejs'],


    // list of files / patterns to load in the browser
    files: [
      '../../../test/js/test-main.js',
      {pattern: 'app/sparql/*.sparql', included:false, served:true},
      {pattern: 'test_graphs/*.ttl', included:false, served:true},
      {pattern: 'app/js/**/*.js', included: false},
      {pattern: '../../../test/**/*.js', included: false}
    ],

    // list of files to exclude
    exclude: [
      'app/js/main.js',
      'app/js/**/*IntSpec.js',
      'app/js/bower_components/**/*Spec.js',
      '../../../../src/test/js/rest/**/*',
    ],

    // web server port
    port: 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,

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
    browsers: ['PhantomJS'],

    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 20000,


    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: true,

  });
};

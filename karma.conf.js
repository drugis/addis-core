'use sctrict';
// Karma configuration
// Generated on Sun Jan 12 2014 11:41:44 GMT+0100 (CET)
module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '',

    // plugins to load
    plugins : [
      'karma-chrome-launcher',
      'karma-phantomjs-launcher',
      'karma-junit-reporter',
      'karma-jasmine',
      'karma-requirejs',
      'karma-coverage'
    ],


    // frameworks to use
    frameworks: ['jasmine', 'requirejs'],


    // list of files / patterns to load in the browser
    files: [
      'src/test/js/test-main.js',
      {pattern: 'src/main/webapp/resources/app/js/**/*.js', included: false},
      {pattern: 'src/test/**/*.js', included: false}
    ],

    // list of files to exclude
    exclude: [
      'src/main/webapp/resources/app/js/main.js',
      'src/main/webapp/resources/app/js/bower_components/**/*Spec.js',
      'src/test/protractor/**/*',
    ],

    preprocessors: {
     // 'src/main/webapp/resources/app/js/*.js': 'coverage',
    },

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit'],
    junitReporter :{
      outputFile: 'src/test/karma-test-results.xml',
      outputDir: 'src/test'
    },

    coverageReporter: {
      type : 'cobertura',
      dir : 'src/target/site/cobertura/',
      file : 'karma-coverage-result.xml'
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
    browsers: ['Chrome'],


    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 10000,


    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false
  });
};

'use strict';

var tests = [];
for (var file in window.__karma__.files) {
  if (window.__karma__.files.hasOwnProperty(file)) {
    if (/Spec\.js$/.test(file)) {
      tests.push(file);
    }
  }
}

console.log(tests);

require.config({
  paths: {
    'jQuery': 'bower_components/jquery/dist/jquery',
    'underscore': 'bower_components/underscore/underscore',
    'angular': 'bower_components/angular/angular',
    'angular-resource': 'bower_components/angular-resource/angular-resource',
    'angular-ui-router': 'bower_components/angular-ui-router/release/angular-ui-router',
    'foundation': 'bower_components/foundation/js/foundation',
    'jasmine': 'bower_components/jasmine/lib/jasmine-core/jasmine',
    'jasmine-html': 'bower_components/jasmine/lib/jasmine-core/jasmine-html',
    'angular-mocks': 'bower_components/angular-mocks/angular-mocks'
  },
  baseUrl: '/base/app/js',
  shim: {
    'angular': { exports : 'angular'},
    'angular-resource': { deps:['angular'], exports: 'angular-resource'},
    'angular-mocks': { deps: ['angular'], exports: 'angular.mock' },
    'underscore': { exports : '_'},
    'jQuery': { exports : 'jQuery'},
    'jasmine': { exports: 'jasmine' },
    'jasmine-html': { deps: ['jasmine'], exports: 'jasmine' }
  },
  priority: ['angular'],
  
  // ask Require.js to load these files (all our tests)
  deps: tests,

  // start test run, once Require.js is done
  callback: window.__karma__.start
});

window.name = "NG_DEFER_BOOTSTRAP!";


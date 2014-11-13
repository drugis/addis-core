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
    'angular': 'bower_components/angular/angular',
    'angular-resource': 'bower_components/angular-resource/angular-resource',
    'angular-ui-router': 'bower_components/angular-ui-router/release/angular-ui-router',
    'foundation': 'bower_components/foundation/js/foundation',
    'jasmine': 'bower_components/jasmine/lib/jasmine-core/jasmine',
    'jasmine-html': 'bower_components/jasmine/lib/jasmine-core/jasmine-html',
    'angular-mocks': 'bower_components/angular-mocks/angular-mocks',
    'mmfoundation': 'bower_components/angular-foundation/mm-foundation',
    'rdfstore': 'bower_components/rdfstore-js/index',
    'lodash': 'bower_components/lodash/dist/lodash.min'
  },
  baseUrl: '/base/app/js',
  shim: {
    'jQuery': {
      exports: 'jQuery'
    },
    'foundation': {
      deps: ['jQuery']
    },
    'angular': {
      deps: ['jQuery'],
      exports: 'angular'
    },
    'angular-select': {
      deps: ['angular', 'jQuery'],
      exports: 'angular-select'
    },
    'ngSanitize': {
      deps: ['angular'],
      exports: 'ngSanitize'
    },
    'angular-ui-router': {
      deps: ['angular']
    },
    'angularanimate': {
      deps: ['angular']
    },
    'mmfoundation': {
      deps: ['angular']
    },
    'angular-resource': {
      deps: ['angular'],
      exports: 'angular-resource'
    },
    'angular-mocks': {
      deps: ['angular'],
      exports: 'angular.mock'
    },
    'underscore': {
      exports: '_'
    },
    'jasmine': {
      exports: 'jasmine'
    },
    'jasmine-html': {
      deps: ['jasmine'],
      exports: 'jasmine'
    },
    'rdfstore': {
      exports: 'rdfstore'
    }

  },
  priority: ['angular'],

  // ask Require.js to load these files (all our tests)
  deps: tests,

  // start test run, once Require.js is done
  callback: window.__karma__.start
});

window.name = "NG_DEFER_BOOTSTRAP!";
window.config = {
  _csrf_token: 'token',
  _csrf_header: 'header',
};

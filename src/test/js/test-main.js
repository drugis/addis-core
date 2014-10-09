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
    'jQuery': 'bower_components/jquery/jquery',
    'underscore': 'bower_components/underscore/underscore',
    'angular': 'bower_components/angular/angular',
    'angular-resource': 'bower_components/angular-resource/angular-resource',
    'angular-ui-router': 'bower_components/angular-ui-router/release/angular-ui-router',
    'angular-select': 'bower_components/angular-ui-select/dist/select.min',
    'foundation': 'bower_components/foundation/js/foundation',
    'jasmine': 'bower_components/jasmine/lib/jasmine-core/jasmine',
    'jasmine-html': 'bower_components/jasmine/lib/jasmine-core/jasmine-html',
    'angular-mocks': 'bower_components/angular-mocks/angular-mocks',
    'mcda': 'bower_components/mcda-web/app/js',
    'gemtc-web': 'bower_components/gemtc-web/app/js',
    'd3': 'bower_components/d3/d3.min',
    'nvd3': 'bower_components/nvd3/nv.d3.min',
    'MathJax': 'bower_components/MathJax/MathJax.js?config=TeX-MML-AM_HTMLorMML',
    'moment': 'bower_components/moment/min/moment.min',
    'mmfoundation': 'bower_components/angular-foundation/mm-foundation',
    'angularanimate': 'bower_components/angular-animate/angular-animate',
    'ngSanitize': 'bower_components/angular-sanitize/angular-sanitize'
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
    'd3': {
      exports: 'd3'
    },
    'nvd3': {
      deps: ['d3'],
      exports: 'nv'
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
  workspacesRepository: {
    service: "LocalWorkspaces"
  }
};
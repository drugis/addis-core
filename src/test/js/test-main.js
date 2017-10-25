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
    'angular': 'bower_components/angular/angular',
    'angular-cookies': 'bower_components/angular-cookies/angular-cookies.min',
    'angular-md5': 'bower_components/angular-md5/angular-md5',
    'angular-patavi-client': 'bower_components/angular-patavi-client/patavi',
    'angular-mocks': 'bower_components/angular-mocks/angular-mocks',
    'angular-resource': 'bower_components/angular-resource/angular-resource',
    'angular-select': 'bower_components/angular-ui-select/dist/select.min',
    'angular-touch': 'bower_components/angular-touch/angular-touch',
    'angular-ui-router': 'bower_components/angular-ui-router/release/angular-ui-router',
    'angularanimate': 'bower_components/angular-animate/angular-animate',
    'clipboard': 'bower_components/clipboard/dist/clipboard.min',
    'd3': 'bower_components/d3/d3.min',
    'domReady': 'bower_components/requirejs-domready/domReady',
    'error-reporting': 'bower_components/error-reporting/errorReportingDirective',
    'foundation': 'bower_components/foundation/js/foundation',
    'gemtc-web': 'bower_components/gemtc-web/app/js',
    'help-popup': 'bower_components/help-popup/help-directive',
    'jasmine': 'bower_components/jasmine/lib/jasmine-core/jasmine',
    'jasmine-html': 'bower_components/jasmine/lib/jasmine-core/jasmine-html',
    'jQuery': 'bower_components/jquery/dist/jquery.min',
    'jquery-slider': 'bower_components/jslider/dist/jquery.slider.min',
    'lodash': 'bower_components/lodash/dist/lodash.min',
    'MathJax': 'bower_components/MathJax/MathJax.js?config=TeX-MML-AM_HTMLorMML',
    'mcda': 'bower_components/mcda-web/app/js',
    'mmfoundation': 'bower_components/angular-foundation-6/dist/angular-foundation.min',
    'modernizr': 'bower_components/modernizr/modernizr',
    'moment': 'bower_components/moment/min/moment.min',
    'ngSanitize': 'bower_components/angular-sanitize/angular-sanitize',
    'nvd3': 'bower_components/nvd3/build/nv.d3.min',
    'showdown': 'bower_components/showdown/dist/showdown.min'
  },
  baseUrl: '/base/src/main/webapp/resources/app/js',
  shim: {
    'angular-touch': {
      deps: ['angular'],
      exports: 'ngTouch'
    },
    'jQuery': {
      exports: 'jQuery'
    },
    'jquery-slider': {
      deps: ['jQuery']
    },
    'foundation': {
      deps: ['jQuery']
    },
    'angular': {
      deps: ['jQuery'],
      exports: 'angular'
    },
    'angular-cookies': {
      deps: ['angular'],
      exports: 'angular-cookies'
    },
    'angular-select': {
      deps: ['angular', 'jQuery'],
      exports: 'angular-select'
    },
    'angular-md5': {
      deps: ['angular'],
      exports: 'angular-md5'
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
      deps: ['angular'],
      exports: 'mmfoundation'
    },
    'help-popup': {
      deps: ['angular']
    },
    'error-reporting': {
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
    'domReady': {
      exports: 'domReady'
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
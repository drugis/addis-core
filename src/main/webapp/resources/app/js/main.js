'use strict';

require.config({
  paths: {
    'jQuery': 'bower_components/jquery/dist/jquery.min',
    'angular': 'bower_components/angular/angular',
    'angular-resource': 'bower_components/angular-resource/angular-resource',
    'angular-ui-router': 'bower_components/angular-ui-router/release/angular-ui-router',
    'domReady': 'bower_components/requirejs-domready/domReady',
    'foundation': 'bower_components/foundation/js/foundation.min',
    'mmfoundation': 'bower_components/angular-foundation/mm-foundation',
    'rdfstore': 'bower_components/rdfstore-js/index',
    'lodash': 'bower_components/lodash/dist/lodash.min',
    'jquery-rdfquery-core': 'bower_components/jquery-rdfquery/js/jquery.rdfquery.core',
    'jquery-rdfquery-rdfa': 'bower_components/jquery-rdfquery/js/jquery.rdfquery.rdfa',
    'jquery-rdfquery-rules': 'bower_components/jquery-rdfquery/js/jquery.rdfquery.rules'
  },
  baseUrl: 'app/js',
  shim: {
    'jQuery': {
      exports: 'jQuery'
    },
    'angular': {
      exports: 'angular'
    },
    'mmfoundation': {
      deps: ['angular']
    },
    'angular-resource': {
      deps: ['angular'],
      exports: 'angular-resource'
    },
    'angular-ui-router': {
      deps: ['angular']
    },
    'foundation': {
      deps: ['jQuery']
    },
    'domReady': {
      exports: 'domReady'
    },
    'rdfstore': {
      exports: 'rdfstore'
    },
    'jquery-rdfquery-core': {
      deps: ['jQuery'],
      exports: 'rdfqueryCore'
    },
    'jquery-rdfquery-rdfa': {
      deps: ['jQuery'],
      exports: 'rdfqueryRdfa'
    },
    'jquery-rdfquery-rules': {
      deps: ['jQuery'],
      exports: 'rdfqueryRules'
    }
  },
  priority: ['angular']
});

window.name = 'NG_DEFER_BOOTSTRAP!';

require(['require', 'angular', 'app'], function(require, angular) {
  require(['domReady!'], function(document) {
    angular.bootstrap(document, ['trialverse']);
  });
});
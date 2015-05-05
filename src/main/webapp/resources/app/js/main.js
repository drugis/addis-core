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
    'lodash': 'bower_components/lodash/dist/lodash.min',
    'moment': 'bower_components/moment/min/moment.min',
    'angular-md5': 'bower_components/angular-md5/angular-md5',
  },
  baseUrl: 'app/js',
  shim: {
    'jQuery': {
      exports: 'jQuery'
    },
    'angular': {
      deps: ['jQuery'],
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
    'angular-md5': {
      deps: ['angular'],
      exports: 'angular-md5'
    },    
  },
  priority: ['angular']
});

window.name = 'NG_DEFER_BOOTSTRAP!';

require(['require', 'angular', 'app'], function(require, angular) {
  require(['domReady!'], function(document) {
    angular.bootstrap(document, ['trialverse']);
  });
});
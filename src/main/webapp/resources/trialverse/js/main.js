'use strict';

require.config({
  paths: {
    'angular': 'bower_components/angular/angular',
    'angular-md5': 'bower_components/angular-md5/angular-md5',
    'angular-resource': 'bower_components/angular-resource/angular-resource',
    'angular-ui-router': 'bower_components/angular-ui-router/release/angular-ui-router',
    'jQuery': 'bower_components/jquery/dist/jquery.min',
    'domReady': 'bower_components/requirejs-domready/domReady',
    'foundation': 'bower_components/foundation/js/foundation.min',
    'lodash': 'bower_components/lodash/lodash.min',
    'mmfoundation': 'bower_components/angular-foundation/mm-foundation',
    'moment': 'bower_components/moment/min/moment.min',
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

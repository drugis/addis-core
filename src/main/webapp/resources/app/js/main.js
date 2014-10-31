'use strict';

require.config({
  paths: {
    'jQuery': 'bower_components/jquery/jquery.min',
    'angular': 'bower_components/angular/angular',
    'foundation': 'bower_components/foundation/js/foundation.min',
    'angular-ui-router': 'bower_components/angular-ui-router/release/angular-ui-router',
    'domReady': 'bower_components/requirejs-domready/domReady'
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
    'foundation': {
      deps: ['jQuery']
    },
    'domReady': {
      exports: 'domReady'
    },
    'angular-ui-router': {
      deps: ['angular']
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

'use strict';

require.config({
  paths: {
    'jQuery': 'bower_components/jquery/dist/jquery.min',
    'underscore': 'bower_components/underscore/underscore-min',
    'angular': 'bower_components/angular/angular',
    'angular-resource': 'bower_components/angular-resource/angular-resource',
    'angular-ui-router': 'bower_components/angular-ui-router/release/angular-ui-router',
    'domReady': 'bower_components/requirejs-domready/domReady',
    'foundation': 'bower_components/foundation/js/foundation.min',
    'select2' : 'bower_components/select2/select2',
    'angular-select2': 'bower_components/angular-ui-select2/src/select2'
  },
  baseUrl: 'app/js',
  shim: {
    'jQuery': { exports : 'jQuery' },
    'angular': { exports : 'angular' },
    'angular-resource': { deps:['angular'], exports: 'angular-resource' },
    'angular-ui-router': { deps:['angular'] },
    'underscore': { exports : '_' },
    'foundation':  { deps: ['jQuery'] },
    'domReady': { exports: 'domReady'},
    'select2' : { deps: ['jQuery'], exports: 'select2'},
    'angular-select2': {deps: ['angular', 'select2'], exports: 'angular-select2'}
  },
  priority: ['angular']
});

window.name = "NG_DEFER_BOOTSTRAP!";

require(['require', 'angular', 'app'], function (require, angular) {
  require(['domReady!'], function (document) {
    angular.bootstrap(document , ['addis']);
  });
});

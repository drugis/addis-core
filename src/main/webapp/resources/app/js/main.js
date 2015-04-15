'use strict';

require.config({
  paths: {
    'jQuery': 'bower_components/jquery/jquery.min',
    'mcda': 'bower_components/mcda-web/app/js',
    'gemtc-web': 'bower_components/gemtc-web/app/js',
    'jquery-slider': 'bower_components/jslider/dist/jquery.slider.min',
    'underscore': 'bower_components/underscore/underscore-min',
    'angular': 'bower_components/angular/angular',
    'angular-resource': 'bower_components/angular-resource/angular-resource',
    'angular-ui-router': 'bower_components/angular-ui-router/release/angular-ui-router',
    'angular-select': 'bower_components/angular-ui-select/dist/select.min',
    'domReady': 'bower_components/requirejs-domready/domReady',
    'foundation': 'bower_components/foundation/js/foundation.min',
    'd3': 'bower_components/d3/d3.min',
    'nvd3': 'bower_components/nvd3/nv.d3.min',
    'MathJax': 'bower_components/MathJax/MathJax.js?config=TeX-MML-AM_HTMLorMML',
    'moment': 'bower_components/moment/min/moment.min',
    'mmfoundation': 'bower_components/angular-foundation/mm-foundation',
    'angularanimate': 'bower_components/angular-animate/angular-animate.min',
    'ngSanitize': 'bower_components/angular-sanitize/angular-sanitize.min'
  },
  baseUrl: 'app/js',
  shim: {
    'jQuery': {
      exports: 'jQuery'
    },
    'jquery-slider': {
      deps: ['jQuery']
    },
    'angular': {
      deps: ['jQuery'],
      exports: 'angular'
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
    'angular-ui-router': {
      deps: ['angular']
    },
    'angular-select': {
      deps: ['angular', 'jQuery'],
      exports: 'angular-select'
    },
    'ngSanitize': {
      deps: ['angular'],
      exports: 'ngSanitize'
    },
    'd3': {
      exports: 'd3'
    },
    'nvd3': {
      deps: ['d3'],
      exports: 'nv'
    },
    'underscore': {
      exports: '_'
    },
    'foundation': {
      deps: ['jQuery']
    },
    'domReady': {
      exports: 'domReady'
    },
    'MathJax': {
      exports: 'MathJax'
    }
  },
  priority: ['angular']
});

window.name = 'NG_DEFER_BOOTSTRAP!';

require(['require', 'angular', 'app'], function(require, angular) {
  require(['domReady!'], function(document) {
    angular.bootstrap(document, ['addis']);
  });
});

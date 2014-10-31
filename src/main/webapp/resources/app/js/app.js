'use strict';
define(
  ['angular',
    'require',
    'jQuery',
    'foundation',
    'angular-ui-router',
    'controllers',
    'services'
  ],
  function(angular, require, $) {
    var dependencies = [
      'ui.router',
      'trialverse.controllers',
      'trialverse.services'
    ];

    var app = angular.module('trialverse', dependencies);

    app.run(['$rootScope', '$window', '$http',
      function($rootScope, $window, $http) {
        var csrfToken = $window.config._csrf_token;
        var csrfHeader = $window.config._csrf_header;

        $http.defaults.headers.common[csrfHeader] = csrfToken;
        $rootScope.$on('$viewContentLoaded', function() {
          $(document).foundation();
        });
      }
    ]);

    app.config(['$stateProvider', '$urlRouterProvider',
      function($stateProvider, $urlRouterProvider) {
        var baseTemplatePath = 'app/views/';
        $stateProvider
          .state('datasets', {
            url: '/datasets',
            templateUrl: baseTemplatePath + 'datasets.html',
            controller: 'DatasetsController'
          })
          .state('create-dataset', {
            url: '/create-dataset',
            templateUrl: baseTemplatePath + 'createDataset.html',
            controller: 'CreateDatasetController'
          })
          .state('hello', {
            url: '/hello',
            templateUrl: baseTemplatePath + 'hello.html'
          });

        // Default route
        $urlRouterProvider.otherwise('/hello');
      }
    ]);

    return app;
  });

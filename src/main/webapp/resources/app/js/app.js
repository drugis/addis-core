'use strict';
define(
  ['angular',
    'require',
    'jQuery',
    'foundation',
    'mmfoundation',
    'angular-ui-router',
    'dataset/dataset',
    'util/util',
    'study/study',
    'rdfstoreWrap/rdfstoreWrap',
    'angular-resource',
    'rdfstore',
    'lodash'
  ],
  function(angular) {
    var dependencies = [
      'ui.router',
      'mm.foundation.modal',
      'trialverse.dataset',
      'trialverse.rdfstoreWrap',
      'trialverse.util',
      'trialverse.study'
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
        $stateProvider
          .state('datasets', {
            url: '/datasets',
            templateUrl: 'app/js/dataset/datasets.html',
            controller: 'DatasetsController'
          })
          .state('create-dataset', {
            url: '/create-dataset',
            templateUrl: 'app/js/dataset/createDataset.html',
            controller: 'CreateDatasetController'
          })
          .state('dataset', {
            url: '/dataset/:datasetUUID',
            templateUrl: 'app/js/dataset/dataset.html',
            controller: 'DatasetController'
          });

        // Default route
        $urlRouterProvider.otherwise('/datasets');
      }
    ]);

    return app;
  });

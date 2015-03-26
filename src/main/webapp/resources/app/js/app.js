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
    'graph/graph',
    'arm/arm',
    'outcome/outcome',
    'populationCharacteristic/populationCharacteristic',
    'endpoint/endpoint',
    'adverseEvent/adverseEvent',
    'epoch/epoch',
    'results/results',
    'measurementMoment/measurementMoment',
    'activity/activity',
    'studyDesign/studyDesign',
    'concept/concept',
    'commit/commit',
    'angular-resource',
    'rdfstore',
    'lodash'
  ],
  function(angular) {
    var dependencies = [
      'ui.router',
      'mm.foundation.modal',
      'mm.foundation.typeahead',
      'trialverse.dataset',
      'trialverse.util',
      'trialverse.graph',
      'trialverse.study',
      'trialverse.arm',
      'trialverse.outcome',
      'trialverse.populationCharacteristic',
      'trialverse.endpoint',
      'trialverse.adverseEvent',
      'trialverse.epoch',
      'trialverse.measurementMoment',
      'trialverse.studyDesign',
      'trialverse.activity',
      'trialverse.results',
      'trialverse.concept',
      'trialverse.commit'
    ];


    Number.isInteger = Number.isInteger || function(value) {
      return typeof value === 'number' &&
        isFinite(value) &&
        Math.floor(value) === value;
    };

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

    app.constant('SCRATCH_RDF_STORE_URL', '/scratch');
    app.constant('CONCEPT_GRAPH_UUID', 'concepts');

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
          })
          .state('versionedDataset', {
            url: '/dataset/:datasetUUID/versions/:versionUuid',
            templateUrl: 'app/js/dataset/dataset.html',
            controller: 'DatasetController'
          })
          .state('datasetHistory', {
            url: '/dataset/:datasetUUID/history',
            templateUrl: 'app/js/dataset/datasetHistory.html',
            controller: 'DatasetHistoryController'
          })
          .state('dataset.concepts', {
            url: '/concepts',
            templateUrl: 'app/js/concept/concepts.html',
            controller: 'ConceptController'
          })
          .state('dataset.study', {
            url: '/study/:studyUUID',
            templateUrl: 'app/js/study/view/study.html',
            controller: 'StudyController'
          });

        // Default route
        $urlRouterProvider.otherwise('/datasets');
      }
    ]);

    return app;
  });

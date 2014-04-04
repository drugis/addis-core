'use strict';
define(
  ['angular',
    'require',
    'jQuery',
    'foundation',
    'angular-ui-router',
    'controllers',
    'directives',
    'filters',
    'resources',
    'services',
    'angular-select2'
  ],
  function (angular, require, $) {
    var dependencies = ['ui.router', 'addis.controllers', 'addis.directives', 'addis.resources',
      'addis.services', 'addis.filters', 'ui.select2'];
    var app = angular.module('addis', dependencies);

    app.run(['$rootScope', '$window', '$http',
      function ($rootScope, $window, $http) {
        var csrfToken = $window.config._csrf_token;
        var csrfHeader = $window.config._csrf_header;

        $http.defaults.headers.common[csrfHeader] = csrfToken;
        $rootScope.$on('$viewContentLoaded', function () {
          $(document).foundation();
        });

      }
    ]);

    app.config(['$stateProvider', '$urlRouterProvider',
      function ($stateProvider, $urlRouterProvider) {
        var baseTemplatePath = 'app/views/';

        $stateProvider
          .state('projects', {
            url: '/projects',
            templateUrl: baseTemplatePath + 'projects.html',
            controller: 'ProjectsController'
          })
          .state('project', {
            url: '/projects/:projectId',
            templateUrl: baseTemplatePath + 'project.html',
            controller: 'SingleProjectController'
          })
          .state('analysis', {
            url: '/projects/:projectId/analyses/:analysisId',
            templateUrl: baseTemplatePath + 'analysis.html',
            controller: 'AnalysisController'
          });

        // Default route
        $urlRouterProvider.otherwise('/projects');

      }
    ]);


    return app;
  });
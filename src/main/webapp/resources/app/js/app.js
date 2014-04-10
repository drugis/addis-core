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
    'angular-select2',
    'mcda/controllers',
    'mcda/controllers',
    'mcda/directives',
    'mcda/filters',
    'mcda/services/remoteWorkspaces',
    'mcda/services/taskDependencies',
    'mcda/services/errorHandling',
  ],
  function(angular, require, $) {
    var mcdaDependencies = [
      'elicit.remoteWorkspaces',
      'elicit.directives',
      'elicit.filters',
      'elicit.controllers',
      'elicit.taskDependencies',
      'elicit.errorHandling'
    ];
    var dependencies = [
      'ui.router',
      'addis.controllers',
      'addis.directives',
      'addis.resources',
      'addis.services',
      'addis.filters',
      'ui.select2'
    ];
    var app = angular.module('addis', dependencies.concat(mcdaDependencies));

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
          })
          .state('scenario', {
            url: '/projects/:projectId/analyses/:analysisId/scenarios/:scenarioId',
            templateUrl: 'app/js/mcda/app/views/' + 'workspace.html',
            resolve: {
              currentWorkspace: ['$stateParams', 'RemoteWorkspaces',
                function($stateParams, Workspaces) {
                  return Workspaces.get($stateParams.workspaceId);
                }
              ],
              currentScenario: function($stateParams, currentWorkspace) {
                return currentWorkspace.getScenario($stateParams.scenarioId);
              }
            },
            controller: 'WorkspaceController'
          });

        // Default route
        $urlRouterProvider.otherwise('/projects');

      }
    ]);


    return app;
  });
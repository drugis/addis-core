'use strict';
define(
  ['angular',
    'require',
    'jQuery',
    'mcda/config',
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
    'mcda/services/hashCodeService'
  ],
  function(angular, require, $, Config) {
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

    app.constant('Tasks', Config.tasks);
    app.constant('DEFAULT_VIEW', 'overview');
    app.constant('ANALYSIS_TYPES', [{
      label: 'Network meta-analysis',
      stateName: 'analysis.networkMetaAnalysis'
    }, {
      label: 'Single-study Benefit-Risk',
      stateName: 'analysis.singleStudyBenefitRisk'
    }]);

    app.run(['$rootScope', '$window', '$http',
      function($rootScope, $window, $http) {
        var csrfToken = $window.config._csrf_token;
        var csrfHeader = $window.config._csrf_header;

        $http.defaults.headers.common[csrfHeader] = csrfToken;
        $rootScope.$on('$viewContentLoaded', function() {
          $(document).foundation();
        });

        $rootScope.$safeApply = function($scope, fn) {
          var phase = $scope.$root.$$phase;
          if (phase === '$apply' || phase === '$digest') {
            this.$eval(fn);
          } else {
            this.$apply(fn);
          }
        };

        $rootScope.$on('patavi.error', function(e, message) {
          $rootScope.$safeApply($rootScope, function() {
            $rootScope.error = _.extend(message, {
              close: function() {
                delete $rootScope.errors;
              }
            });
          });

        });
      }
    ]);

    app.config(['Tasks', '$stateProvider', '$urlRouterProvider',
      function(Tasks, $stateProvider, $urlRouterProvider) {
        var baseTemplatePath = 'app/views/';
        var mcdaBaseTemplatePath = 'app/js/bower_components/mcda-web/app/views/';

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
            templateUrl: baseTemplatePath + 'analysisContainer.html',
            resolve: {
              currentAnalysis: ['$stateParams', 'AnalysisResource',
                function($stateParams, AnalysisResource) {
                  return AnalysisResource.get($stateParams);
                }
              ],
              currentProject: ['$stateParams', 'ProjectResource',
                function($stateParams, ProjectResource) {
                  return ProjectResource.get({
                    projectId: $stateParams.projectId
                  });
                }
              ]
            },
            controller: function($scope, currentAnalysis, currentProject) {
              $scope.analysis = currentAnalysis;
              $scope.project = currentProject;
            },
            abstract: true
          })
          .state('analysis.singleStudyBenefitRisk', {
            url: '',
            templateUrl: baseTemplatePath + 'singleStudyBenefitRiskAnalysisView.html',
            controller: 'SingleStudyBenefitRiskAnalysisController'
          })
          .state('analysis.scenario', {
            url: '/scenarios/:scenarioId',
            templateUrl: mcdaBaseTemplatePath + 'scenario.html',
            resolve: {
              currentWorkspace: ['$stateParams', 'RemoteWorkspaces',
                function($stateParams, Workspaces) {
                  return Workspaces.get($stateParams.analysisId);
                }
              ],
              currentScenario: function($stateParams, currentWorkspace) {
                return currentWorkspace.getScenario($stateParams.scenarioId);
              }
            },
            controller: 'ScenarioController'
          });

        _.each(Tasks.available, function(task) {
          var templateUrl = mcdaBaseTemplatePath + task.templateUrl;
          $stateProvider.state(task.id, {
            parent: 'analysis.scenario',
            url: '/' + task.id,
            templateUrl: templateUrl,
            controller: task.controller,
            resolve: {
              taskDefinition: function(currentScenario, TaskDependencies) {
                var def = TaskDependencies.extendTaskDefinition(task);
                return def;
              }
            }
          });
        });

        // Default route
        $urlRouterProvider.otherwise('/projects');

      }
    ]);


    return app;
  });
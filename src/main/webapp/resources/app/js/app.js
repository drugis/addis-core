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
    'interceptors',
    'resources',
    'services',
    'angular-select2',
    'gemtc-web/controllers',
    'gemtc-web/resources',
    'gemtc-web/constants',
    'gemtc-web/services',
    'gemtc-web/directives',
    'mcda/controllers',
    'mcda/controllers',
    'mcda/directives',
    'mcda/services/remoteWorkspaces',
    'mcda/services/taskDependencies',
    'mcda/services/errorHandling',
    'mcda/services/hashCodeService',
    'mcda/services/partialValueFunction',
    'mcda/services/remoteRemarks',
    'mcda/services/util'
  ],
  function(angular, require, $, Config) {
    var mcdaDependencies = [
      'elicit.errorHandling',
      'elicit.remoteWorkspaces',
      'elicit.remoteRemarks',
      'elicit.taskDependencies',
      'elicit.directives',
      'elicit.controllers',
      'elicit.pvfService',
      'elicit.util',
      'mm.foundation',
      'ngAnimate'
    ];
    var dependencies = [
      'ui.router',
      'addis.controllers',
      'addis.directives',
      'addis.resources',
      'addis.services',
      'addis.filters',
      'addis.interceptors',
      'addis.directives',
      'ui.select2'
    ];
    var gemtcWebDependencies = [
      'gemtc.controllers',
      'gemtc.resources',
      'gemtc.constants',
      'gemtc.services',
      'gemtc.directives'
    ];
    var app = angular.module('addis', dependencies.concat(mcdaDependencies.concat(gemtcWebDependencies)));

    app.constant('Tasks', Config.tasks);
    app.constant('DEFAULT_VIEW', 'overview');
    app.constant('ANALYSIS_TYPES', [{
      label: 'Network meta-analysis',
      stateName: 'analysis.networkMetaAnalysis'
    }, {
      label: 'Single-study Benefit-Risk',
      stateName: 'analysis.singleStudyBenefitRisk'
    }]);
    app.constant('mcdaRootPath', 'app/js/bower_components/mcda-web/app/');
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

    app.config(['Tasks', '$stateProvider', '$urlRouterProvider', 'ANALYSIS_TYPES', '$httpProvider', '$animateProvider',
      function(Tasks, $stateProvider, $urlRouterProvider, ANALYSIS_TYPES, $httpProvider, $animateProvider) {
        var baseTemplatePath = 'app/views/';
        var mcdaBaseTemplatePath = 'app/js/bower_components/mcda-web/app/views/';
        var gemtcWebBaseTemplatePath = 'app/js/bower_components/gemtc-web/app/views/';

        $httpProvider.interceptors.push('SessionExpiredInterceptor');
        $animateProvider.classNameFilter(/sidepanel/);

        $stateProvider
          .state('projects', {
            url: '/projects',
            templateUrl: baseTemplatePath + 'projects.html',
            controller: 'ProjectsController'
          })
          .state('create-project', {
            url: '/create-project',
            templateUrl: baseTemplatePath + 'createProject.html',
            controller: 'CreateProjectController'
          })
          .state('namespace', {
            url: '/namespaces/:namespaceUid',
            templateUrl: baseTemplatePath + 'namespaceView.html',
            controller: 'NamespaceController'
          })
          .state('study', {
            url: '/namespaces/:namespaceUid/study/:studyUid',
            templateUrl: baseTemplatePath + 'study.html',
            controller: 'StudyController'
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
            // AnalysisController does routing to correct type of analysis view
            controller: 'AnalysisController'
          })
          .state('analysis.singleStudyBenefitRisk', {
            templateUrl: baseTemplatePath + 'singleStudyBenefitRiskAnalysisView.html',
            controller: 'SingleStudyBenefitRiskAnalysisController'
          })
          .state('analysis.networkMetaAnalysis', {
            templateUrl: baseTemplatePath + 'networkMetaAnalysisView.html',
            controller: 'NetworkMetaAnalysisController'
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
          })
          .state('analysis.model', {
            url: '/models/:modelId',
            templateUrl: gemtcWebBaseTemplatePath + 'modelView.html',
            controller: 'ModelController'
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

'use strict';
define(
  ['angular',
    'require',
    'jQuery',
    'mcda/config',
    'mmfoundation',
    'foundation',
    'angular-ui-router',
    'angular-select',
    'angularanimate',
    'ngSanitize',
    'controllers',
    'directives',
    'filters',
    'interceptors',
    'resources',
    'services',
    'gemtc-web/controllers',
    'gemtc-web/resources',
    'gemtc-web/constants',
    'gemtc-web/services',
    'gemtc-web/directives',
    'mcda/controllers',
    'mcda/controllers',
    'mcda/directives',
    'mcda/services/workspaceResource',
    'mcda/services/taskDependencies',
    'mcda/services/errorHandling',
    'mcda/services/workspaceService',
    'mcda/services/routeFactory',
    'mcda/services/pataviService',
    'mcda/services/hashCodeService',
    'mcda/services/partialValueFunction',
    'mcda/services/scaleRangeService',
    'mcda/services/util'
  ],
  function(angular, require, $, Config) {
    var mcdaDependencies = [
      'elicit.errorHandling',
      'elicit.scaleRangeService',
      'elicit.workspaceResource',
      'elicit.workspaceService',
      'elicit.taskDependencies',
      'elicit.directives',
      'elicit.controllers',
      'elicit.pvfService',
      'elicit.pataviService',
      'elicit.util',
      'elicit.routeFactory',
      'mm.foundation',
      'ngAnimate'
    ];
    var dependencies = [
      'ui.router',
      'ngSanitize',
      'ui.select',
      'addis.controllers',
      'addis.directives',
      'addis.resources',
      'addis.services',
      'addis.filters',
      'addis.interceptors',
      'addis.directives'
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
      stateName: 'networkMetaAnalysis'
    }, {
      label: 'Single-study Benefit-Risk',
      stateName: 'singleStudyBenefitRisk'
    }]);
    app.constant('mcdaRootPath', 'app/js/bower_components/mcda-web/app/');
    app.constant('gemtcRootPath', 'app/js/bower_components/gemtc-web/app/');
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

        $rootScope.$on('error', function(e, message) {
          $rootScope.$safeApply($rootScope, function() {
            $rootScope.error = _.extend(message, {
              close: function() {
                delete $rootScope.error;
              }
            });
          });

        });
      }
    ]);

    app.config(function(uiSelectConfig) {
      uiSelectConfig.theme = 'select2';
    });

    app.config(['Tasks', '$stateProvider', '$urlRouterProvider', 'ANALYSIS_TYPES', '$httpProvider', '$animateProvider', 'MCDARouteProvider',
      function(Tasks, $stateProvider, $urlRouterProvider, ANALYSIS_TYPES, $httpProvider, $animateProvider, MCDARouteProvider) {
        var baseTemplatePath = 'app/views/';
        var mcdaBaseTemplatePath = 'app/js/bower_components/mcda-web/app/views/';
        var gemtcWebBaseTemplatePath = 'app/js/bower_components/gemtc-web/app/';

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
          .state('singleStudyBenefitRisk', {
            url: '/projects/:projectId/ssbr/:analysisId',
            resolve: {
              currentAnalysis: ['$stateParams', 'AnalysisResource',
                function($stateParams, AnalysisResource) {
                  return AnalysisResource.get($stateParams).$promise;
                }
              ],
              currentProject: ['$stateParams', 'ProjectResource',
                function($stateParams, ProjectResource) {
                  return ProjectResource.get({
                    projectId: $stateParams.projectId
                  }).$promise;
                }
              ]
            },
            templateUrl: baseTemplatePath + 'singleStudyBenefitRiskAnalysisView.html',
            controller: 'SingleStudyBenefitRiskAnalysisController'
          })
          .state('networkMetaAnalysisContainer', {
            templateUrl: baseTemplatePath + 'networkMetaAnalysisContainer.html',
            abstract:true
          })
          .state('networkMetaAnalysis', {
            parent: 'networkMetaAnalysisContainer',
            url: '/projects/:projectId/nma/:analysisId',
            resolve: {
              currentAnalysis: ['$stateParams', 'AnalysisResource',
                function($stateParams, AnalysisResource) {
                  return AnalysisResource.get($stateParams).$promise;
                }
              ],
              currentProject: ['$stateParams', 'ProjectResource',
                function($stateParams, ProjectResource) {
                  return ProjectResource.get({
                    projectId: $stateParams.projectId
                  }).$promise;
                }
              ]
            },
            views: {
              'networkMetaAnalysis': {
                templateUrl: baseTemplatePath + 'networkMetaAnalysisView.html',
                controller: 'NetworkMetaAnalysisController'
              },
              'models': {
                templateUrl: gemtcWebBaseTemplatePath + '/js/models/models.html',
                controller: 'ModelsController'
              },
              'network': {
                templateUrl: baseTemplatePath + 'network.html'
              },
              'evidenceTable': {
                templateUrl: baseTemplatePath + 'evidenceTable.html'
              }
            }
          })
          .state('createModel', {
            url: '/projects/:projectId/nma/:analysisId/models/createModel',
            templateUrl: gemtcWebBaseTemplatePath + 'js/models/createModel.html',
            controller: 'CreateModelController'
          })
          .state('nmaModelContainer', {
            templateUrl: baseTemplatePath + 'networkMetaAnalysisModelContainerView.html',
            controller: 'NetworkMetaAnalysisModelContainerController',
            url: '/projects/:projectId/nma/:analysisId/models/:modelId',
            abstract:true,
            resolve: {
              currentAnalysis: ['$stateParams', 'AnalysisResource',
                function($stateParams, AnalysisResource) {
                  return AnalysisResource.get($stateParams).$promise;
                }
              ],
              currentProject: ['$stateParams', 'ProjectResource',
                function($stateParams, ProjectResource) {
                  return ProjectResource.get({
                    projectId: $stateParams.projectId
                  }).$promise;
                }
              ]
            },
          })
          .state('model', {
            parent: 'nmaModelContainer',
            templateUrl: gemtcWebBaseTemplatePath + 'views/modelView.html',
            controller: 'ModelController'
          });

        // Default route
        $urlRouterProvider.otherwise('/projects');
        MCDARouteProvider.buildRoutes($stateProvider, 'singleStudyBenefitRisk', mcdaBaseTemplatePath);
      }
    ]);

    return app;
  });

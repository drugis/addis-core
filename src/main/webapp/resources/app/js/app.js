'use strict';
define(
  [
    'angular',
    'mcda-web/js/config',
    'mcda-web/lexicon',
    'gemtc-web/lexicon',
    'angular-foundation-6',
    'angular-animate',
    'angular-cookies',
    'angular-touch',
    'angular-ui-router',
    'angular-sanitize',
    'ui-select',
    'showdown',
    'angularjs-slider',
    'angucomplete-alt',
    'angular-patavi-client',
    'core-js',
    'error-reporting',
    'export-directive',
    'help-popup',
    'page-title-service',
    './util/constants',
    './controllers',
    './filters',
    './resources',
    './services',
    './search/search',
    './user/user',
    './dataset/dataset',
    './project/project',
    './analysis/analysis',
    './util/util',
    './study/study',
    './excelIO/excelIO',
    './unit/unit',
    './graph/graph',
    './populationInformation/populationInformation',
    './arm/arm',
    './group/group',
    './outcome/outcome',
    './intervention/intervention',
    './outcome/addisOutcomes',
    './variable/variable',
    './populationCharacteristic/populationCharacteristic',
    './endpoint/endpoint',
    './adverseEvent/adverseEvent',
    './epoch/epoch',
    './results/results',
    './measurementMoment/measurementMoment',
    './activity/activity',
    './studyDesign/studyDesign',
    './concept/concept',
    './commit/commit',
    './mapping/mapping',
    './studyInformation/studyInformation',
    'gemtc-web/js/controllers',
    'gemtc-web/js/resources',
    'gemtc-web/js/constants',
    'gemtc-web/js/services',
    'gemtc-web/js/directives',
    'gemtc-web/js/filters',
    'mcda-web/js/benefitRisk/benefitRisk',
    'mcda-web/js/directives',
    'mcda-web/js/effectsTable/effectsTable',
    'mcda-web/js/evidence/evidence',
    'mcda-web/js/services/workspaceResource',
    'mcda-web/js/services/taskDependencies',
    'mcda-web/js/services/routeFactory',
    'mcda-web/js/util',
    'mcda-web/js/preferences/preferences',
    'mcda-web/js/results/results',
    'mcda-web/js/subProblem/subProblem',
    'mcda-web/js/workspace/workspace',
    './covariates/covariates',
    './home/home'
  ],
  function(
    angular,
    Config,
    mcdaLexicon,
    gemtcLexicon
    ) {
    var mcdaDependencies = [
      'elicit.benefitRisk',
      'elicit.directives',
      'elicit.effectsTable',
      'elicit.evidence',
      'elicit.preferences',
      'elicit.results',
      'elicit.routeFactory',
      'elicit.subProblem',
      'elicit.taskDependencies',
      'elicit.util',
      'elicit.workspace',
      'elicit.workspaceResource',
      'mm.foundation',
      'ngAnimate'
    ];
    var dependencies = [
      'ui.router',
      'ngSanitize',
      'ui.select',
      'ngCookies',
      'mm.foundation.modal',
      'mm.foundation.tabs',
      'help-directive',
      'addis.constants',
      'addis.home',
      'addis.project',
      'addis.analysis',
      'addis.controllers',
      'addis.resources',
      'addis.services',
      'addis.filters',
      'addis.covariates',
      'addis.interventions',
      'addis.outcomes',
      'addis.excelIO',
      'patavi',
      'page-title-service',
      'errorReporting',
      'export-directive',
      'angucomplete-alt'
    ];
    var gemtcWebDependencies = [
      'gemtc.controllers',
      'gemtc.resources',
      'gemtc.constants',
      'gemtc.services',
      'gemtc.directives',
      'gemtc.filters'
    ];
    var trialverseDependencies = [
      'trialverse.search',
      'trialverse.user',
      'trialverse.dataset',
      'trialverse.util',
      'trialverse.graph',
      'trialverse.study',
      'trialverse.populationInformation',
      'trialverse.arm',
      'trialverse.group',
      'trialverse.outcome',
      'trialverse.variable',
      'trialverse.populationCharacteristic',
      'trialverse.endpoint',
      'trialverse.adverseEvent',
      'trialverse.epoch',
      'trialverse.measurementMoment',
      'trialverse.studyDesign',
      'trialverse.activity',
      'trialverse.results',
      'trialverse.concept',
      'trialverse.commit',
      'trialverse.mapping',
      'trialverse.studyInformation',
      'trialverse.unit'
    ];

    Number.isInteger = Number.isInteger || function(value) {
      return typeof value === 'number' &&
        isFinite(value) &&
        Math.floor(value) === value;
    };

    var app = angular.module('addis', dependencies.concat(mcdaDependencies, gemtcWebDependencies, trialverseDependencies));

    app.constant('Tasks', Config.tasks);
    app.constant('DEFAULT_VIEW', 'evidence');
    app.constant('currentSchemaVersion', '1.1.0');
    app.constant('ANALYSIS_TYPES', [{
      label: 'Evidence synthesis',
      stateName: 'networkMetaAnalysis'
    }, {
      label: 'Benefit-risk analysis',
      stateName: 'BenefitRiskCreationStep-1'
    }]);
    app.constant('isGemtcStandAlone', false);
    app.constant('isMcdaStandalone', false);

    app.config(['$locationProvider', function($locationProvider) {
      $locationProvider.hashPrefix('');
    }]);

    app.run(['$rootScope', '$q', '$window', '$http', '$location', '$templateCache', '$transitions', '$cookies',
        'HelpPopupService', 'CacheService', 'PageTitleService', 'STUDY_CATEGORY_SETTINGS',
      function($rootScope, $q, $window, $http, $location, $templateCache, $transitions, $cookies,
        HelpPopupService, CacheService, PageTitleService, STUDY_CATEGORY_SETTINGS) {
        $rootScope.$safeApply = function($scope, fn) {
          var phase = $scope.$root.$$phase;
          if (phase === '$apply' || phase === '$digest') {
            this.$eval(fn);
          } else {
            this.$apply(fn);
          }
        };

        $templateCache.put('studyInformation.html', require('./studyInformation/studyInformation.html'));
        $templateCache.put('populationInformation.html', require('./populationInformation/populationInformation.html'));
        $templateCache.put('arm.html', require('./arm/arm.html'));
        $templateCache.put('variable.html', require('./variable/variable.html'));
        $templateCache.put('epoch.html', require('./epoch/epoch.html'));
        $templateCache.put('measurementMoment.html', require('./measurementMoment/measurementMoment.html'));
        $templateCache.put('activity.html', require('./activity/activity.html'));
        $templateCache.put('activityDosingPeriodicityDirective.html', require('./activity/activityDosingPeriodicityDirective.html'));
        $templateCache.put('interventionDosingPeriodicityDirective.html', require('./intervention/interventionDosingPeriodicityDirective.html'));
        $templateCache.put('durationInputDirective.html', require('./util/directives/durationInput/durationInputDirective.html'));

        HelpPopupService.loadLexicon(mcdaLexicon);
        HelpPopupService.loadLexicon(gemtcLexicon);
        HelpPopupService.loadLexicon(require('../../addis-lexicon.json'));

        PageTitleService.loadLexicon($q.resolve(require('mcda-web/app/mcda-page-titles.json')));
        PageTitleService.loadLexicon($q.resolve(require('gemtc-web/app/gemtc-page-titles.json')));
        PageTitleService.loadLexicon($q.resolve(require('../../addis-page-titles.json')));

        $transitions.onSuccess({}, function(transition) {
          var redirectUrl = $cookies.get('returnToPage');
          if (transition.to().name === 'datasets' && transition.from().name === '' && redirectUrl && redirectUrl !== '/') {
            $cookies.remove('returnToPage');
            $location.path(redirectUrl);
          }
          $window.scrollTo(0, 0);
        });

        $rootScope.$on('modelResultsAvailable', function(event, ids) {
          CacheService.evict('modelPromises', ids.modelId);
          CacheService.evict('consistencyModelsPromises', ids.projectId);
          CacheService.evict('modelsByProjectPromises', ids.projectId);
        });

        $rootScope.$on('primaryModelSet', function(event, ids) {
          CacheService.evict('analysisPromises', ids.analysisId);
          CacheService.evict('analysesPromises', ids.projectId);
        });

        $templateCache.put('model-settings-section.html', require('gemtc-web/views/model-settings-section.html'));
        $templateCache.put('convergence-diagnostics-section.html', require('gemtc-web/views/convergence-diagnostics-section.html'));
        $templateCache.put('meta-regression-section.html', require('gemtc-web/views/meta-regression-section.html'));
        $templateCache.put('results-section.html', require('gemtc-web/views/results-section.html'));
        $templateCache.put('model-fit-section.html', require('gemtc-web/views/model-fit-section.html'));

      }
    ]);

    app.config(['uiSelectConfig', function(uiSelectConfig) {
      uiSelectConfig.theme = 'select2';
    }]);

    app.config(['$stateProvider', '$urlRouterProvider', '$httpProvider', 'MCDARouteProvider',
      function($stateProvider, $urlRouterProvider, $httpProvider, MCDARouteProvider) {

        $httpProvider.interceptors.push('SessionExpiredInterceptor');

        // Default route
        $urlRouterProvider.otherwise(function($injector) {
          var UserService = $injector.get('UserService');
          var $state = $injector.get('$state');
          UserService.getLoginUser().then(function(user) {
            if (user) {
              $state.go('datasets', {
                userUid: user.id
              });
            } else {
              $state.go('home');
            }
          });

        });

        $stateProvider
          .state('home', {
            url: '/',
            templateUrl: './home/home.html',
            controller: 'HomeController',
          })
          .state('user', {
            abstract: true,
            url: '/users/:userUid',
            templateUrl: './user/user.html',
            controller: 'UserController',
          })
          .state('projects', {
            url: '/projects',
            parent: 'user',
            templateUrl: '../views/projects.html',
            controller: 'ProjectsController'
          })
          .state('datasets', {
            url: '/datasets',
            parent: 'user',
            templateUrl: './dataset/datasets.html',
            controller: 'DatasetsController'
          })
          .state('search', {
            url: '/search?searchTerm',
            parent: 'user',
            templateUrl: './search/search.html',
            controller: 'SearchController'
          })
          .state('project', {
            url: '/users/:userUid/projects/:projectId',
            templateUrl: '../views/project.html',
            controller: 'SingleProjectController',
            resolve: {
              activeTab: function() {
                return 'definitions';
              },
              project: ['$stateParams', 'ProjectResource', function($stateParams, ProjectResource) {
                return ProjectResource.get($stateParams).$promise;
              }]
            }
          })
          .state('projectReport', {
            url: '/report',
            templateUrl: '../views/project.html',
            controller: 'SingleProjectController',
            parent: 'project',
            resolve: {
              activeTab: function() {
                return 'report';
              }
            }
          })
          .state('projectAnalyses', {
            url: '/analyses',
            templateUrl: '../views/project.html',
            controller: 'SingleProjectController',
            parent: 'project',
            resolve: {
              activeTab: function() {
                return 'analyses';
              }
            }
          })
          .state('editReport', {
            url: '/editReport',
            templateUrl: './project/report/editReport.html',
            controller: 'EditReportController',
            parent: 'project'
          })
          .state('networkMetaAnalysisContainer', {
            templateUrl: '../views/networkMetaAnalysisContainer.html',
            controller: 'NetworkMetaAnalysisContainerController',
            url: '/users/:userUid/projects/:projectId/nma/:analysisId',
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
            abstract: true
          })
          // Evidence synthesis states
          .state('networkMetaAnalysis', {
            parent: 'networkMetaAnalysisContainer',
            url: '',
            views: {
              'networkMetaAnalysis': {
                templateUrl: '../views/networkMetaAnalysisView.html'
              },
              'models': {
                templateUrl: 'gemtc-web/js/models/models.html',
                controller: 'ModelsController'
              },
              'network': {
                templateUrl: '../views/network.html'
              },
              'evidenceTable': {
                templateUrl: '../views/evidenceTable.html'
              }
            }
          })
          .state('nmaModelContainer', {
            templateUrl: '../views/networkMetaAnalysisModelContainerView.html',
            controller: 'NetworkMetaAnalysisModelContainerController',
            abstract: true,
          })
          .state('createModel', {
            parent: 'nmaModelContainer',
            url: '/users/:userUid/projects/:projectId/nma/:analysisId/models/createModel',
            templateUrl: 'gemtc-web/js/models/createModel.html',
            controller: 'CreateModelController'
          })
          .state('refineModel', {
            parent: 'nmaModelContainer',
            url: '/users/:userUid/projects/:projectId/nma/:analysisId/models/:modelId/refineModel',
            templateUrl: 'gemtc-web/js/models/createModel.html',
            controller: 'CreateModelController',
            resolve: {
              model: ['$stateParams', 'RefineModelService',
                function($stateParams, RefineModelService) {
                  return RefineModelService.getRefinedModel($stateParams);
                }
              ]
            }
          })
          .state('model', {
            url: '/users/:userUid/projects/:projectId/nma/:analysisId/models/:modelId',
            parent: 'nmaModelContainer',
            templateUrl: 'gemtc-web/views/modelView.html',
            controller: 'ModelController',
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
          .state('nodeSplitOverview', {
            parent: 'model',
            url: '/nodeSplitOverview',
            templateUrl: 'gemtc-web/js/models/nodeSplitOverview.html',
            controller: 'NodeSplitOverviewController',
            resolve: {
              models: ['$stateParams', 'ModelResource',
                function($stateParams, ModelResource) {
                  return ModelResource.query({
                    projectId: $stateParams.projectId,
                    analysisId: $stateParams.analysisId
                  }).$promise;
                }
              ],
              problem: ['$stateParams', 'ProblemResource',
                function($stateParams, ProblemResource) {
                  return ProblemResource.get({
                    projectId: $stateParams.projectId,
                    analysisId: $stateParams.analysisId
                  }).$promise;
                }
              ]

            }
          })
          // meta-benefit-risk states
          .state('BenefitRiskCreationStep-1', {
            url: '/users/:userUid/projects/:projectId/br/:analysisId/step-1',
            templateUrl: './analysis/brStep-1.html',
            controller: 'BenefitRiskStep1Controller'
          })
          .state('BenefitRiskCreationStep-2', {
            url: '/users/:userUid/projects/:projectId/br/:analysisId/step-2',
            templateUrl: './analysis/brStep-2.html',
            controller: 'BenefitRiskStep2Controller'
          })
          .state('BenefitRisk', {
            url: '/users/:userUid/projects/:projectId/br/:analysisId',
            templateUrl: './analysis/br.html',
            controller: 'BenefitRiskController'
          })
          // trialverse states
          .state('dataset', {
            url: '/users/:userUid/datasets/:datasetUuid',
            templateUrl: './dataset/dataset.html',
            controller: 'DatasetController'
          })
          .state('dataset.concepts', {
            url: '/concepts',
            templateUrl: './concept/concepts.html',
            controller: 'ConceptsController'
          })
          .state('dataset.study', {
            url: '/studies/:studyGraphUuid',
            templateUrl: './study/view/study.html',
            controller: 'StudyController'
          })
          .state('datasetHistory', {
            url: '/users/:userUid/datasets/:datasetUuid/history',
            templateUrl: './dataset/datasetHistory.html',
            controller: 'DatasetHistoryController'
          })
          .state('studyHistory', {
            url: '/users/:userUid/datasets/:datasetUuid/studies/:studyGraphUuid/history',
            templateUrl: './dataset/studyHistory.html',
            controller: 'StudyHistoryController'
          })
          .state('versionedDataset', {
            url: '/users/:userUid/datasets/:datasetUuid/versions/:versionUuid',
            templateUrl: './dataset/dataset.html',
            controller: 'DatasetController'
          })
          .state('versionedDataset.concepts', {
            url: '/concepts',
            templateUrl: './concept/concepts.html',
            controller: 'ConceptsController'
          })
          .state('versionedDataset.study', {
            url: '/studies/:studyGraphUuid',
            templateUrl: './study/view/study.html',
            controller: 'StudyController'
          })
          // mcda states
          .state('benefitRisk', {
            abstract: true,
            controller: 'AbstractBenefitRiskController',
            url: '/users/:userUid/projects/:projectId/benefitRisk/:analysisId',
            templateUrl: './analysis/benefitRiskContainer.html',
            resolve: {
              currentAnalysis: ['$stateParams', 'AnalysisResource',
                function($stateParams, AnalysisResource) {
                  return AnalysisResource.get($stateParams).$promise;
                }
              ],
              currentProject: ['$stateParams', 'ProjectResource',
                function($stateParams, ProjectResource) {
                  return ProjectResource.get($stateParams).$promise;
                }
              ]
            }
          });

        MCDARouteProvider.buildRoutes($stateProvider, 'benefitRisk', 'mcda-web');

      }
    ]);
    app.constant('CONCEPT_GRAPH_UUID', 'concepts');

    return app;
  });

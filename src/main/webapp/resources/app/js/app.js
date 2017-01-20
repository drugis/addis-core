'use strict';
define(
  ['angular',
    'require',
    'jQuery',
    'mcda/config',
    'lodash',
    'mmfoundation',
    'foundation',
    'angularanimate',
    'angular-cookies',
    'angular-select',
    'angular-ui-router',
    'angular-md5',
    'ngSanitize',
    'showdown',
    'angular-patavi-client',
    'error-reporting',
    'controllers',
    'filters',
    'resources',
    'services',
    'help-popup',
    'search/search',
    'user/user',
    'dataset/dataset',
    'project/project',
    'analysis/analysis',
    'util/util',
    'study/study',
    'unit/unit',
    'graph/graph',
    'populationInformation/populationInformation',
    'arm/arm',
    'group/group',
    'outcome/outcome',
    'intervention/intervention',
    'outcome/addisOutcomes',
    'variable/variable',
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
    'mapping/mapping',
    'studyInformation/studyInformation',
    'gemtc-web/controllers',
    'gemtc-web/resources',
    'gemtc-web/constants',
    'gemtc-web/services',
    'gemtc-web/directives',
    'mcda/services/effectsTableService',
    'mcda/controllers',
    'mcda/directives',
    'mcda/services/workspaceResource',
    'mcda/services/taskDependencies',
    'mcda/services/workspaceService',
    'mcda/services/scalesService',
    'mcda/services/routeFactory',
    'mcda/services/resultsService',
    'mcda/services/hashCodeService',
    'mcda/services/partialValueFunction',
    'mcda/services/scaleRangeService',
    'mcda/services/util',
    'covariates/covariates',
    'home/home'
  ],
  function(angular, require, $, Config, _) {
    var mcdaDependencies = [
      'elicit.effectsTableService',
      'elicit.scaleRangeService',
      'elicit.workspaceResource',
      'elicit.workspaceService',
      'elicit.scalesService',
      'elicit.taskDependencies',
      'elicit.directives',
      'elicit.controllers',
      'elicit.pvfService',
      'elicit.resultsService',
      'elicit.util',
      'elicit.routeFactory',
      'mm.foundation',
      'ngAnimate'
    ];
    var dependencies = [
      'ui.router',
      'ngSanitize',
      'ui.select',
      'angular-md5',
      'ngCookies',
      'mm.foundation.tpls',
      'mm.foundation.modal',
      'mm.foundation.typeahead',
      'mm.foundation.tabs',
      'help-directive',
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
      'patavi',
      'errorReporting'
    ];
    var gemtcWebDependencies = [
      'gemtc.controllers',
      'gemtc.resources',
      'gemtc.constants',
      'gemtc.services',
      'gemtc.directives'
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
    app.constant('DEFAULT_VIEW', 'overview');
    app.constant('ANALYSIS_TYPES', [{
      label: 'Evidence synthesis',
      stateName: 'networkMetaAnalysis'
    }, {
      label: 'Benefit-risk analysis based on a single study',
      stateName: 'singleStudyBenefitRisk'
    }, {
      label: 'Benefit-risk analysis based on meta-analyses',
      stateName: 'MetaBenefitRiskCreationStep-1'
    }]);
    app.constant('mcdaRootPath', 'app/js/bower_components/mcda-web/app/');
    app.constant('gemtcRootPath', 'app/js/bower_components/gemtc-web/app/');

    app.run(['$rootScope', '$window', '$http', '$location', '$cookies', 'HelpPopupService',
      function($rootScope, $window, $http, $location, $cookies, HelpPopupService) {
        $rootScope.$safeApply = function($scope, fn) {
          var phase = $scope.$root.$$phase;
          if (phase === '$apply' || phase === '$digest') {
            this.$eval(fn);
          } else {
            this.$apply(fn);
          }
        };

        HelpPopupService.loadLexicon($http.get('app/js/bower_components/gemtc-web/app/lexicon.json'));
        HelpPopupService.loadLexicon($http.get('addis-lexicon.json'));

        $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState) {
          var redirectUrl = $cookies.get('returnToPage');
          if (toState.name === 'datasets' && fromState.name === '' && redirectUrl && redirectUrl !== '/') {
            $cookies.remove('returnToPage');
            $location.path(redirectUrl);
          }
          $window.scrollTo(0, 0);
        });
      }
    ]);

    app.config(function(uiSelectConfig) {
      uiSelectConfig.theme = 'select2';
    });

    app.config(['Tasks', '$stateProvider', '$urlRouterProvider', 'ANALYSIS_TYPES', '$httpProvider', 'MCDARouteProvider',
      function(Tasks, $stateProvider, $urlRouterProvider, ANALYSIS_TYPES, $httpProvider, MCDARouteProvider) {
        var baseTemplatePath = 'app/views/';
        var mcdaBaseTemplatePath = 'app/js/bower_components/mcda-web/app/views/';
        var gemtcWebBaseTemplatePath = 'app/js/bower_components/gemtc-web/app/';

        $httpProvider.interceptors.push('SessionExpiredInterceptor');

        // Default route
        $urlRouterProvider.otherwise(function($injector) {
          var $window = $injector.get('$window');
          var $state = $injector.get('$state');
          if ($window.config && $window.config.user) {
            $state.go('datasets', {
              userUid: $window.config.user.id
            });
          } else {
            $state.go('home');
          }

        });

        $stateProvider
          .state('home', {
            url: '/',
            templateUrl: 'app/js/home/home.html',
            controller: 'HomeController',
          })
          .state('user', {
            abstract: true,
            url: '/users/:userUid',
            templateUrl: 'app/js/user/user.html',
            controller: 'UserController',
          })
          .state('projects', {
            url: '/projects',
            parent: 'user',
            templateUrl: baseTemplatePath + 'projects.html',
            controller: 'ProjectsController'
          })
          .state('datasets', {
            url: '/datasets',
            parent: 'user',
            templateUrl: 'app/js/dataset/datasets.html',
            controller: 'DatasetsController'
          })
          .state('search', {
            url: '/search?searchTerm',
            parent: 'user',
            templateUrl: 'app/js/search/search.html',
            controller: 'SearchController'
          })
          .state('create-project', {
            url: '/users/:userUid/projects/create-project',
            templateUrl: baseTemplatePath + 'createProject.html',
            controller: 'CreateProjectController'
          })
          .state('project', {
            url: '/users/:userUid/projects/:projectId',
            templateUrl: baseTemplatePath + 'project.html',
            controller: 'SingleProjectController',
            resolve: {
              activeTab: function() {
                return 'details';
              },
              project: ['$stateParams', 'ProjectResource', function($stateParams, ProjectResource) {
                return  ProjectResource.get($stateParams).$promise;
              }]
            }
          })
          .state('projectReport', {
            url: '/report',
            templateUrl: baseTemplatePath + 'project.html',
            controller: 'SingleProjectController',
            parent:'project',
            resolve: {
              activeTab: function() {
                return 'report';
              }
            }
          })
          .state('editReport', {
            url: '/editReport',
            templateUrl: 'app/js/project/editReport.html',
            controller: 'EditReportController',
            parent: 'project'
          })
          .state('singleStudyBenefitRisk', {
            url: '/users/:userUid/projects/:projectId/ssbr/:analysisId',
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
                templateUrl: baseTemplatePath + 'networkMetaAnalysisView.html'
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
          .state('nmaModelContainer', {
            templateUrl: baseTemplatePath + 'networkMetaAnalysisModelContainerView.html',
            controller: 'NetworkMetaAnalysisModelContainerController',
            abstract: true,
          })
          .state('createModel', {
            parent: 'nmaModelContainer',
            url: '/users/:userUid/projects/:projectId/nma/:analysisId/models/createModel',
            templateUrl: gemtcWebBaseTemplatePath + 'js/models/createModel.html',
            controller: 'CreateModelController',
            resolve: {  // FIXME why on earth not just initialise in controller
              model: function() {
                return {
                  linearModel: 'random',
                  modelType: {
                    mainType: 'network'
                  },
                  outcomeScale: {
                    type: 'heuristically'
                  },
                  burnInIterations: 5000,
                  inferenceIterations: 20000,
                  thinningFactor: 10,
                  heterogeneityPrior: {
                    type: 'automatic'
                  },
                  treatmentInteraction: 'shared',
                  leaveOneOut: {}
                };
              }
            }
          })
          .state('refineModel', {
            parent: 'nmaModelContainer',
            url: '/users/:userUid/projects/:projectId/nma/:analysisId/models/:modelId/refineModel',
            templateUrl: gemtcWebBaseTemplatePath + 'js/models/createModel.html',
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
            templateUrl: gemtcWebBaseTemplatePath + 'views/modelView.html',
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
            templateUrl: gemtcWebBaseTemplatePath + 'js/models/nodeSplitOverview.html',
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
          .state('MetaBenefitRiskCreationStep-1', {
            url: '/users/:userUid/projects/:projectId/metabr/:analysisId/step-1',
            templateUrl: 'app/js/analysis/metabrStep-1.html',
            controller: 'MetaBenefitRiskStep1Controller'
          })
          .state('MetaBenefitRiskCreationStep-2', {
            url: '/users/:userUid/projects/:projectId/metabr/:analysisId/step-2',
            templateUrl: 'app/js/analysis/metabrStep-2.html',
            controller: 'MetaBenefitRiskStep2Controller'
          })
          .state('metaBenefitRisk', {
            url: '/users/:userUid/projects/:projectId/metabr/:analysisId',
            templateUrl: 'app/js/analysis/metabr.html',
            controller: 'MetaBenefitRiskController'
          })
          // trialverse states
          .state('dataset', {
            url: '/users/:userUid/datasets/:datasetUuid',
            templateUrl: 'app/js/dataset/dataset.html',
            controller: 'DatasetController'
          })
          .state('dataset.concepts', {
            url: '/concepts',
            templateUrl: 'app/js/concept/concepts.html',
            controller: 'ConceptController'
          })
          .state('dataset.study', {
            url: '/studies/:studyGraphUuid',
            templateUrl: 'app/js/study/view/study.html',
            controller: 'StudyController'
          })
          .state('datasetHistory', {
            url: '/users/:userUid/datasets/:datasetUuid/history',
            templateUrl: 'app/js/dataset/datasetHistory.html',
            controller: 'DatasetHistoryController'
          })
          .state('studyHistory', {
            url: '/users/:userUid/datasets/:datasetUuid/studies/:studyGraphUuid/history',
            templateUrl: 'app/js/dataset/studyHistory.html',
            controller: 'StudyHistoryController'
          })
          .state('versionedDataset', {
            url: '/users/:userUid/datasets/:datasetUuid/versions/:versionUuid',
            templateUrl: 'app/js/dataset/dataset.html',
            controller: 'DatasetController'
          })
          .state('versionedDataset.concepts', {
            url: '/concepts',
            templateUrl: 'app/js/concept/concepts.html',
            controller: 'ConceptController'
          })
          .state('versionedDataset.study', {
            url: '/studies/:studyGraphUuid',
            templateUrl: 'app/js/study/view/study.html',
            controller: 'StudyController'
          })
          // mcda states
          .state('benefitRisk', {
            abstract: true,
            controller: 'BenefitRiskController',
            url: '/users/:userUid/projects/:projectId/benefitRisk/:analysisId',
            templateUrl: 'app/js/analysis/benefitRiskContainer.html',
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

        MCDARouteProvider.buildRoutes($stateProvider, 'benefitRisk', mcdaBaseTemplatePath);

      }
    ]);
    app.constant('CONCEPT_GRAPH_UUID', 'concepts');
    app.constant('GROUP_ALLOCATION_OPTIONS', _.keyBy([{
      uri: 'ontology:AllocationRandomized',
      label: 'Randomized'
    }, {
      uri: 'ontology:AllocationNonRandomized',
      label: 'Non-Randomized'
    }, {
      uri: 'unknown',
      label: 'Unknown'
    }], 'uri'));
    app.constant('BLINDING_OPTIONS', _.keyBy([{
      uri: 'ontology:OpenLabel',
      label: 'Open'
    }, {
      uri: 'ontology:SingleBlind',
      label: 'Single blind'
    }, {
      uri: 'ontology:DoubleBlind',
      label: 'Double blind'
    }, {
      uri: 'ontology:TripleBlind',
      label: 'Triple blind'
    }, {
      uri: 'unknown',
      label: 'Unknown'
    }], 'uri'));
    app.constant('STATUS_OPTIONS', _.keyBy([{
      uri: 'ontology:StatusRecruiting',
      label: 'Recruiting'
    }, {
      uri: 'ontology:StatusEnrolling',
      label: 'Enrolling'
    }, {
      uri: 'ontology:StatusActive',
      label: 'Active'
    }, {
      uri: 'ontology:StatusCompleted',
      label: 'Completed'
    }, {
      uri: 'ontology:StatusSuspended',
      label: 'Suspended'
    }, {
      uri: 'ontology:StatusTerminated',
      label: 'Terminated'
    }, {
      uri: 'ontology:StatusWithdrawn',
      label: 'Withdrawn'
    }, {
      uri: 'unknown',
      label: 'Unknown'
    }], 'uri'));

    return app;
  });

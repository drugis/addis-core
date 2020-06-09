'use strict';
define(['angular', 'angular-mocks', '../controllers'], function() {
  describe('the network meta-analysis controller', function() {
    var scope;
    var state;
    var q;
    var timeout = function(fn) { // fake timeout which triggers immediately
      fn();
      return {
        then: function(fn2) {
          fn2();
        }
      };
    };
    var analysisDeferred;
    var interventionDeferred;
    var covariateDeferred;
    var outcomesDeferred;
    var interventionResource;
    var analysisService;
    var analysisResource;
    var userService;
    var networkMetaAnalysisService;
    var covariates = [{
      id: 1
    }, {
      id: 2
    }];
    var covariateResource = jasmine.createSpyObj('CovariateResource', ['query']);
    var trialverseTrialDataDeferred;
    var mockAnalysis = {
      id: 101,
      outcome: {
        id: 2,
        semanticOutcomeUri: 'semanticOutcomeUri'
      },
      includedMeasurementMoments: []
    };
    var projectDeferred;
    var mockWindow = {
      config: {
        user: 'user'
      }
    };
    var mockProject = {
      id: 11,
      namespaceUid: '123-a-dda456',
      datasetVersion: 'version',
      owner: 'owner'
    };
    var mockStateParams = {
      analysisId: 1,
      projectId: 11
    };
    var mockOutcomes = [{
      id: 1,
      semanticOutcomeUri: 'semanticOutcomeUri-1'
    }, {
      id: 2,
      semanticOutcomeUri: 'semanticOutcomeUri-2'
    }];
    var mockTrialData = {
      studies: [1, 2, 3]
    };
    var outcomeResource;
    var mockInterventions = [{
      id: 1,
      name: 'intervention-name1',
      semanticInterventionUri: 'semanticInterventionUri1'
    }, {
      id: 2,
      name: 'intervention-name2',
      semanticInterventionUri: 'semanticInterventionUri2'
    }, {
      id: 3,
      name: 'intervention-name3',
      semanticInterventionUri: 'semanticInterventionUri3'
    },];
    var EvidenceTableResource;
    var mockModel = {
      id: 512,
      analysisId: 600
    };
    var modelResource;
    var pageTitleServiceMock;
    var modelDeferred;
    var userDefer;
    var isLoginUserDefer;
    var modal;
    var cacheService;

    beforeEach(angular.mock.module('addis.controllers'));

    beforeEach(inject(function($rootScope, $controller, $q) {
      q = $q;
      analysisDeferred = $q.defer();
      mockAnalysis.$promise = analysisDeferred.promise;
      projectDeferred = $q.defer();
      mockProject.$promise = projectDeferred.promise;
      outcomesDeferred = $q.defer();
      mockOutcomes.$promise = outcomesDeferred.promise;
      interventionDeferred = $q.defer();
      mockInterventions.$promise = interventionDeferred.promise;
      trialverseTrialDataDeferred = $q.defer();
      mockTrialData.$promise = trialverseTrialDataDeferred.promise;

      scope = $rootScope;
      scope.analysis = mockAnalysis;
      scope.project = mockProject;
      userService = jasmine.createSpyObj('UserService', ['getLoginUser', 'isLoginUserId']);
      pageTitleServiceMock = jasmine.createSpyObj('PageTitleService', ['setPageTitle']);
      outcomeResource = jasmine.createSpyObj('OutcomeResource', ['query']);
      outcomeResource.query.and.returnValue(mockOutcomes);
      interventionResource = jasmine.createSpyObj('InterventionResource', ['query']);
      interventionResource.query.and.returnValue(mockInterventions);
      EvidenceTableResource = jasmine.createSpyObj('EvidenceTableResource', ['query', 'get']);
      EvidenceTableResource.query.and.returnValue(mockTrialData);
      analysisService = jasmine.createSpyObj('AnalysisService', ['isNetworkDisconnected']);
      analysisResource = jasmine.createSpyObj('AnalysisResource', ['save']);
      networkMetaAnalysisService = jasmine.createSpyObj('NetworkMetaAnalysisService', [
        'addInclusionsToCovariates',
        'addInclusionsToInterventions',
        'buildInterventionInclusions',
        'buildMissingValueByStudy',
        'buildMomentSelections',
        'buildOverlappingTreatmentMap',
        'changeCovariateInclusion',
        'cleanUpExcludedArms',
        'doesModelHaveAmbiguousArms',
        'doesModelHaveInsufficientCovariateValues',
        'transformStudiesToNetwork',
        'transformStudiesToTableRows',
        'getIncludedInterventions',
        'hasInterventionOverlap',
        'doesModelContainTooManyResultProperties',
        'hasMissingCovariateValues'
      ]);
      cacheService = jasmine.createSpyObj('CacheService', ['evict']);
      var mockNetwork = {
        interventions: []
      };
      networkMetaAnalysisService.transformStudiesToNetwork.and.returnValue(mockNetwork);
      networkMetaAnalysisService.transformStudiesToTableRows.and.returnValue({ absolute: [], contrast: [] });
      networkMetaAnalysisService.addInclusionsToInterventions.and.returnValue(mockInterventions);
      networkMetaAnalysisService.getIncludedInterventions.and.returnValue(mockInterventions);

      covariateDeferred = $q.defer();
      covariates.$promise = covariateDeferred.promise;
      covariateResource.query.and.returnValue(covariates);

      modelResource = jasmine.createSpyObj('modelResource', ['save', 'query']);
      modelDeferred = $q.defer();
      mockModel.$promise = modelDeferred.promise;
      modelResource.save.and.returnValue(mockModel);
      modelResource.query.and.returnValue([mockModel]);
      state = jasmine.createSpyObj('$state', ['go']);

      userDefer = $q.defer();
      isLoginUserDefer = $q.defer();
      userService.getLoginUser.and.returnValue(userDefer.promise);
      userService.isLoginUserId.and.returnValue(isLoginUserDefer.promise);
      modal = jasmine.createSpyObj('$modal', ['open']);
      $controller('NetworkMetaAnalysisContainerController', {
        $window: mockWindow,
        $scope: scope,
        $q: q,
        $timeout: timeout,
        $state: state,
        $stateParams: mockStateParams,
        $modal: modal,
        currentAnalysis: mockAnalysis,
        currentProject: mockProject,
        OutcomeResource: outcomeResource,
        InterventionResource: interventionResource,
        CovariateResource: covariateResource,
        EvidenceTableResource: EvidenceTableResource,
        NetworkMetaAnalysisService: networkMetaAnalysisService,
        AnalysisService: analysisService,
        AnalysisResource: analysisResource,
        ModelResource: modelResource,
        UserService: userService,
        PageTitleService: pageTitleServiceMock,
        CacheService: cacheService
      });
    }));

    describe('when first initialised', function() {
      it('should place the list of selectable outcomes on the scope', function() {
        expect(outcomeResource.query).toHaveBeenCalledWith({
          projectId: mockProject.id
        });
      });

      it('should set the parent\'s isNetworkDisconnected to true', function() {
        expect(scope.errors.isNetworkDisconnected).toBeTruthy();
      });

      it('should query the model to see if the analyis is used in a model', function() {
        expect(scope.editMode.hasModel).toBeDefined();
        expect(modelResource.query).toHaveBeenCalledWith(mockStateParams);
      });
    });

    describe('when the analysis, outcomes, interventions, project, models and covariates are loaded', function() {
      beforeEach(function() {
        analysisDeferred.resolve(mockAnalysis);
        projectDeferred.resolve(mockProject);
        interventionDeferred.resolve(mockInterventions);
        outcomesDeferred.resolve(mockOutcomes);
        modelDeferred.resolve(mockModel);
        covariateDeferred.resolve(covariates);
        scope.$apply();
      });

      it('should save the analysis when the selected outcome changes', function() {
        mockAnalysis.outcome = mockOutcomes[0];
        scope.changeSelectedOutcome();
        expect(analysisResource.save).toHaveBeenCalled();
      });

      describe('and there is already an outcome defined on the analysis', function() {
        it('should get the tabledata and transform it to table rows and network', function() {
          expect(EvidenceTableResource.query).toHaveBeenCalledWith({
            projectId: mockProject.id,
            analysisId: mockAnalysis.id
          });
          trialverseTrialDataDeferred.resolve();
          scope.$apply();
          expect(networkMetaAnalysisService.transformStudiesToTableRows).toHaveBeenCalled();
          expect(analysisService.isNetworkDisconnected).toHaveBeenCalled();
          expect(networkMetaAnalysisService.transformStudiesToNetwork).toHaveBeenCalled();
        });
      });

      describe('and the intervention inclusion is changed', function() {
        it('should update the analysis\' included interventions, clean up its arm exclusions when applicable and save the analysis', function() {
          var intervention = {
            isIncluded: false
          };
          scope.studies = [];
          scope.changeInterventionInclusion(intervention);
          expect(networkMetaAnalysisService.buildInterventionInclusions).toHaveBeenCalled();
          expect(networkMetaAnalysisService.cleanUpExcludedArms).toHaveBeenCalled();
          expect(analysisResource.save).toHaveBeenCalled();
        });
      });
    });
  });
});

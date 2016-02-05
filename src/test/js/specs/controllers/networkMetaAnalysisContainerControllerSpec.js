'use strict';
define(['angular', 'angular-mocks', 'controllers'], function() {
  describe('the network meta-analysis controller', function() {
    var scope,
      state,
      q,
      analysisDeferred,
      interventionDeferred,
      covariateDeferred,
      outcomesDeferred,
      interventionResource,
      networkMetaAnalysisService,
      covariates = [{
        id: 1
      }, {
        id: 2
      }],
      covariateResource = jasmine.createSpyObj('CovariateResource', ['query']),
      trialverseTrialDataDeferred,
      mockAnalysis = {
        $save: function() {},
        outcome: {
          id: 2,
          semanticOutcomeUri: 'semanticOutcomeUri'
        }
      },
      projectDeferred,
      mockWindow = {
        config: {
          user: 'user'
        }
      },
      mockProject = {
        id: 11,
        namespaceUid: '123-a-dda456',
        datasetVersion: 'version',
        owner: 'owner'
      },
      mockStateParams = {
        analysisId: 1,
        projectId: 11
      },
      mockOutcomes = [{
        id: 1,
        semanticOutcomeUri: 'semanticOutcomeUri-1'
      }, {
        id: 2,
        semanticOutcomeUri: 'semanticOutcomeUri-2'
      }],
      mockTrialData = {
        studies: [1, 2, 3]
      },
      outcomeResource,
      mockInterventions = [{
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
      }, ],
      trialverseTrialDataResource,
      mockModel = {
        id: 512,
        analysisId: 600
      },
      modelResource,
      modelDeferred;
    beforeEach(module('addis.controllers'));
    beforeEach(inject(function($rootScope, $controller, $q) {
      q = $q;
      analysisDeferred = $q.defer();
      mockAnalysis.$promise = analysisDeferred.promise;
      spyOn(mockAnalysis, '$save');
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
      outcomeResource = jasmine.createSpyObj('OutcomeResource', ['query']);
      outcomeResource.query.and.returnValue(mockOutcomes);
      interventionResource = jasmine.createSpyObj('InterventionResource', ['query']);
      interventionResource.query.and.returnValue(mockInterventions);
      trialverseTrialDataResource = jasmine.createSpyObj('TrialverseTrialDataResource', ['query', 'get']);
      trialverseTrialDataResource.query.and.returnValue(mockTrialData);
      trialverseTrialDataResource.get.and.returnValue(mockTrialData);
      networkMetaAnalysisService = jasmine.createSpyObj('NetworkMetaAnalysisService', ['transformTrialDataToTableRows',
        'transformTrialDataToNetwork', 'isNetworkDisconnected', 'addInclusionsToInterventions', 'changeArmExclusion',
        'buildInterventionInclusions', 'doesInterventionHaveAmbiguousArms', 'doesModelHaveAmbiguousArms', 'cleanUpExcludedArms',
        'addInclusionsToCovariates', 'changeCovariateInclusion'
      ]);
      var mockNetwork = {
        interventions: []
      };
      networkMetaAnalysisService.transformTrialDataToNetwork.and.returnValue(mockNetwork);
      networkMetaAnalysisService.transformTrialDataToTableRows.and.returnValue([]);
      networkMetaAnalysisService.isNetworkDisconnected.and.returnValue(true);
      networkMetaAnalysisService.changeArmExclusion.and.returnValue({
        $save: function() {}
      });
      networkMetaAnalysisService.doesInterventionHaveAmbiguousArms.and.returnValue(true);
      networkMetaAnalysisService.addInclusionsToInterventions.and.returnValue(mockInterventions);

      covariateDeferred = $q.defer();
      covariates.$promise = covariateDeferred.promise;
      covariateResource.query.and.returnValue(covariates);

      modelResource = jasmine.createSpyObj('modelResource', ['save', 'query']);
      modelDeferred = $q.defer();
      mockModel.$promise = modelDeferred.promise;
      modelResource.save.and.returnValue(mockModel);
      modelResource.query.and.returnValue([mockModel]);
      state = jasmine.createSpyObj('$state', ['go']);
      $controller('NetworkMetaAnalysisContainerController', {
        $window: mockWindow,
        $scope: scope,
        $q: q,
        $state: state,
        $stateParams: mockStateParams,
        currentAnalysis: mockAnalysis,
        currentProject: mockProject,
        OutcomeResource: outcomeResource,
        InterventionResource: interventionResource,
        CovariateResource: covariateResource,
        TrialverseTrialDataResource: trialverseTrialDataResource,
        NetworkMetaAnalysisService: networkMetaAnalysisService,
        ModelResource: modelResource
      });
    }));
    describe('when first initialised', function() {
      it('should place the list of selectable outcomes on the scope', function() {
        expect(outcomeResource.query).toHaveBeenCalledWith({
          projectId: mockProject.id
        });
        expect(scope.outcomes).toEqual(mockOutcomes);
      });
      it('should set the parent\'s isNetworkDisconnected to true', function() {
        expect(scope.isNetworkDisconnected).toBeTruthy();
      });
      it('should query the model to see if the analyis is used in a model', function() {
        expect(scope.hasModel).toBeDefined();
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
        expect(scope.analysis.$save).toHaveBeenCalled();
      });
      describe('and there is already an outcome defined on the analysis', function() {
        it('should get the tabledata and transform it to table rows and network', function() {
          expect(trialverseTrialDataResource.get).toHaveBeenCalledWith({
            namespaceUid: mockProject.namespaceUid,
            outcomeUri: mockOutcomes[0].semanticOutcomeUri,
            interventionUris: [],
            covariateKeys: [],
            version: mockProject.datasetVersion
          });
          trialverseTrialDataDeferred.resolve();
          scope.$apply();
          expect(networkMetaAnalysisService.transformTrialDataToTableRows).toHaveBeenCalled();
          expect(networkMetaAnalysisService.isNetworkDisconnected).toHaveBeenCalled();
          expect(networkMetaAnalysisService.transformTrialDataToNetwork).toHaveBeenCalled();
        });
      });
      describe('and the arm exclusion is changed ', function() {
        beforeEach(function() {
          var dataRow = {};
          scope.tableHasAmbiguousArm = true;
          scope.changeArmExclusion(dataRow);
        });
        it('should set tableHasAmbiguousArm to false', function() {
          expect(scope.tableHasAmbiguousArm).toBeFalsy();
          expect(networkMetaAnalysisService.changeArmExclusion).toHaveBeenCalled();
        });
      });
      describe('and the intervention inclusion is changed', function() {
        it('should update the analysis\' included interventions, clean up its arm exclusions when applicable and save the analysis', function() {
          var intervention = {
            isIncluded: false
          };
          scope.trialverseData = {};
          scope.changeInterventionInclusion(intervention);
          expect(networkMetaAnalysisService.buildInterventionInclusions).toHaveBeenCalled();
          expect(networkMetaAnalysisService.cleanUpExcludedArms).toHaveBeenCalled();
          expect(scope.analysis.$save).toHaveBeenCalled();
        });
      });
      describe('and the doesInterventionHaveAmbiguousArms function is called', function() {
        beforeEach(function() {
          var drugId = 1;
          scope.tableHasAmbiguousArm = false;
          networkMetaAnalysisService.doesInterventionHaveAmbiguousArms.and.returnValue(true);
          scope.doesInterventionHaveAmbiguousArms(drugId);
        });
        it('should call the doesInterventionHaveAmbiguousArms function on the NetworkMetaAnalysisService', function() {
          expect(networkMetaAnalysisService.doesInterventionHaveAmbiguousArms).toHaveBeenCalled();
        });
      });
    });
  });
});

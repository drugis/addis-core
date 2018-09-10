'use strict';
define(['angular', 'angular-mocks', '../controllers'], function() {
  describe('the network meta-analysis controller', function() {
    var scope,
      state,
      q,
      timeout = function(fn) { // fake timeout which triggers immediately
        fn();
        return {
          then: function(fn2) {
            fn2();
          }
        };
      },
      analysisDeferred,
      interventionDeferred,
      covariateDeferred,
      outcomesDeferred,
      interventionResource,
      analysisService,
      analysisResource,
      userService,
      networkMetaAnalysisService,
      covariates = [{
        id: 1
      }, {
        id: 2
      }],
      covariateResource = jasmine.createSpyObj('CovariateResource', ['query']),
      trialverseTrialDataDeferred,
      mockAnalysis = {
        id: 101,
        outcome: {
          id: 2,
          semanticOutcomeUri: 'semanticOutcomeUri'
        },
        includedMeasurementMoments: []
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
      },],
      EvidenceTableResource,
      mockModel = {
        id: 512,
        analysisId: 600
      },
      modelResource,
      pageTitleServiceMock,
      modelDeferred,
      userDefer;
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
      userService = jasmine.createSpyObj('UserService', ['getLoginUser']);
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
        'buildMissingValueByStudyMap',
        'buildMomentSelections',
        'buildOverlappingTreatmentMap',
        'changeArmExclusion',
        'changeCovariateInclusion',
        'checkSigmaNShow',
        'checkStdErrShow',
        'cleanUpExcludedArms',
        'doesInterventionHaveAmbiguousArms',
        'doesModelHaveAmbiguousArms',
        'doesModelHaveInsufficientCovariateValues',
        'getIncludedInterventions',
        'transformTrialDataToNetwork',
        'transformTrialDataToTableRows',
        'getMeasurementType',
        'checkColumnsToShow'
      ]);
      var mockNetwork = {
        interventions: []
      };
      networkMetaAnalysisService.transformTrialDataToNetwork.and.returnValue(mockNetwork);
      networkMetaAnalysisService.transformTrialDataToTableRows.and.returnValue([]);
      networkMetaAnalysisService.doesInterventionHaveAmbiguousArms.and.returnValue(true);
      networkMetaAnalysisService.addInclusionsToInterventions.and.returnValue(mockInterventions);
      networkMetaAnalysisService.getIncludedInterventions.and.returnValue(mockInterventions);
      networkMetaAnalysisService.changeArmExclusion.and.returnValue(mockAnalysis);

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
      userService.getLoginUser.and.returnValue(userDefer.promise);
      $controller('NetworkMetaAnalysisContainerController', {
        $window: mockWindow,
        $scope: scope,
        $q: q,
        $timeout: timeout,
        $state: state,
        $stateParams: mockStateParams,
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
        PageTitleService: pageTitleServiceMock
      });
    }));

    describe('when first initialised', function() {
      it('should place the list of selectable outcomes on the scope', function() {
        expect(outcomeResource.query).toHaveBeenCalledWith({
          projectId: mockProject.id
        });
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
          expect(networkMetaAnalysisService.transformTrialDataToTableRows).toHaveBeenCalled();
          expect(analysisService.isNetworkDisconnected).toHaveBeenCalled();
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
          expect(analysisResource.save).toHaveBeenCalled();
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
          expect(analysisResource.save).toHaveBeenCalled();
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
      describe('and the changeMeasurementMoment function is called', function() {
        var newMoment = {
          uri: 'mmUri',
          isDefault: false
        };
        var dataRow = {
          studyUri: 'studyUri'
        };
        beforeEach(function() {
          mockAnalysis.includedMeasurementMoments = [{
            analysisId: mockAnalysis.id,
            study: dataRow.studyUri,
            measurementMoment: 'oldMeasurementMoment'
          }];
          scope.changeMeasurementMoment(newMoment, dataRow);
        });
        it('clean up old inclusions for the study and make a new one for non-default measurement moment', function() {
          expect(analysisResource.save).toHaveBeenCalled();
          expect(mockAnalysis.includedMeasurementMoments).toEqual([{
            analysisId: mockAnalysis.id,
            study: dataRow.studyUri,
            measurementMoment: newMoment.uri
          }]);
        });

      });
    });
  });
});

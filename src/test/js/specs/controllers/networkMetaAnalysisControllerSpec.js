define(['angular', 'angular-mocks', 'controllers'], function() {
  describe('the network meta-analysis controller', function() {
    var scope,
      state,
      analysisDeferred,
      interventionDeferred,
      trialverseTrailDataDeferred,
      mockAnalysis = {
        $save: function() {},
        outcome: {
          id: 2,
          semanticOutcomeUri: 'semanticOutcomeUri'
        }
      },
      projectDeferred,
      mockProject = {
        id: 11,
        trialverseId: 123456
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
      analysisDeferred = $q.defer();
      mockAnalysis.$promise = analysisDeferred.promise;

      spyOn(mockAnalysis, '$save');

      projectDeferred = $q.defer();
      mockProject.$promise = projectDeferred.promise;

      outcomesDeferred = $q.defer();
      mockOutcomes.$promise = outcomesDeferred.promise;

      interventionDeferred = $q.defer();
      mockInterventions.$promise = interventionDeferred.promise;

      trialverseTrailDataDeferred = $q.defer();
      mockTrialData.$promise = trialverseTrailDataDeferred.promise;

      scope = $rootScope;
      scope.$parent = {
        analysis: mockAnalysis,
        project: mockProject
      };
      outcomeResource = jasmine.createSpyObj('OutcomeResource', ['query']);
      outcomeResource.query.and.returnValue(mockOutcomes);

      interventionResource = jasmine.createSpyObj('InterventionResource', ['query']);
      interventionResource.query.and.returnValue(mockInterventions);

      trialverseTrialDataResource = jasmine.createSpyObj('TrialverseTrialDataResource', ['query', 'get']);
      trialverseTrialDataResource.query.and.returnValue(mockTrialData);
      trialverseTrialDataResource.get.and.returnValue(mockTrialData);

      networkMetaAnalysisService = jasmine.createSpyObj('NetworkMetaAnalysisService', ['transformTrialDataToTableRows',
        'transformTrialDataToNetwork',
        'isNetworkDisconnected',
        'addInclusionsToInterventions',
        'changeArmExclusion',
        'buildInterventionExclusions',
        'doesInterventionHaveAmbiguousArms',
        'doesModelHaveAmbiguousArms'
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

      modelResource = jasmine.createSpyObj('modelResource', ['save', 'query']);
      modelDeferred = $q.defer();
      mockModel.$promise = modelDeferred.promise;
      modelResource.save.and.returnValue(mockModel);
      modelResource.query.and.returnValue([mockModel]);

      state = jasmine.createSpyObj('$state', ['go']);

      $controller('NetworkMetaAnalysisController', {
        $scope: scope,
        $state: state,
        $stateParams: mockStateParams,
        OutcomeResource: outcomeResource,
        InterventionResource: interventionResource,
        TrialverseTrialDataResource: trialverseTrialDataResource,
        NetworkMetaAnalysisService: networkMetaAnalysisService,
        ModelResource: modelResource
      });
    }));

    describe('when first initialised', function() {
      it('should inherit the parent\'s analysis and project', function() {
        expect(scope.analysis).toEqual(scope.$parent.analysis);
        expect(scope.project).toEqual(scope.$parent.project);
      });

      it('should place the list of selectable outcomes on the scope', function() {
        expect(outcomeResource.query).toHaveBeenCalledWith({
          projectId: mockProject.id
        });
        expect(scope.outcomes).toEqual(mockOutcomes);
      });

      it('should place a createModelAndGoToModel function on the scope that navigates to the analysis.model state', function() {
        expect(scope.createModelAndGoToModel).toBeDefined();
        scope.createModelAndGoToModel();
        expect(modelResource.save).toHaveBeenCalledWith(mockStateParams, {});
      });

      it('should set isNetworkDisconnected to true', function() {
        expect(scope.isNetworkDisconnected).toBeTruthy();
      });

      it('should query the model to see if the analyis is used in a model', function() {
        expect(scope.hasModel).toBeDefined();
        expect(modelResource.query).toHaveBeenCalledWith(mockStateParams);
      });

    });

    describe('when the analysis, outcomes, interventions, project, models are loaded', function() {

      beforeEach(inject(function($controller) {
        analysisDeferred.resolve(mockAnalysis);
        projectDeferred.resolve(mockProject);
        interventionDeferred.resolve(mockInterventions);
        outcomesDeferred.resolve(mockOutcomes);
        modelDeferred.resolve(mockModel);
        scope.$apply();
      }));

      it('should save the analysis when the selected outcome changes', function() {
        mockAnalysis.outcome = mockOutcomes[0];
        scope.changeSelectedOutcome();
        expect(scope.analysis.$save).toHaveBeenCalled();
      });

      describe('and there is already an outcome defined on the analysis', function() {

        it('should get the tabledata and transform it to table rows and network', function() {
          expect(trialverseTrialDataResource.get).toHaveBeenCalledWith({
            id: mockProject.trialverseId,
            outcomeUri: mockOutcomes[0].semanticOutcomeUri,
            interventionUris: []
          });
          trialverseTrailDataDeferred.resolve();
          scope.$apply();
          expect(networkMetaAnalysisService.transformTrialDataToTableRows).toHaveBeenCalled();
          expect(networkMetaAnalysisService.isNetworkDisconnected).toHaveBeenCalled();
          expect(networkMetaAnalysisService.transformTrialDataToNetwork).toHaveBeenCalled();
        });

      });

      describe('and the create model button is clicked', function() {

        it('should create a model and go to the model view', function() {
          scope.createModelAndGoToModel();
          expect(modelResource.save).toHaveBeenCalledWith(mockStateParams, {});
          modelDeferred.resolve(mockModel);
          scope.$apply();
          expect(state.go).toHaveBeenCalledWith('analysis.model', {
            modelId: mockModel.id
          });
        });
      });

      describe('and the go to model button is clicked', function() {

        it('should go to the model view', function() {
          scope.goToModel();
          expect(state.go).toHaveBeenCalledWith('analysis.model', {
              modelId: mockModel.id
          });
        });
      });

      describe('and the arm is exclusion is changed ', function() {


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
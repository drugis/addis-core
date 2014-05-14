define(['angular', 'angular-mocks', 'controllers'], function() {
  describe('the network meta-analysis controller', function() {
    var scope,
      analysisDeferred,
      interventionDeferred,
      trailverseTrailDataDefered,
      mockAnalysis = {$save: function(){},
      outcome: {
        id: 2,
        semanticOutcomeUri: 'semanticOutcomeUri'
      }},
      projectDeferred,
      mockProject = {
        id: 11,
        trialverseId: 123456
      },
      mockStateParams = {
        analysisId: 1,
        projectId: 11
      },
      mockOutcomes = [
        {id: 1, semanticOutcomeUri: 'semanticOutcomeUri-1'},
        {id: 2, semanticOutcomeUri: 'semanticOutcomeUri-2'}
      ],
      mockTrialData = {studies: [1,2,3]},
      outcomeResource,
      mockInterventions = [
        {id: 1, name: 'intervention-name1', semanticInterventionUri: 'semanticInterventionUri1'},
        {id: 2, name: 'intervention-name2', semanticInterventionUri: 'semanticInterventionUri2'},
        {id: 3, name: 'intervention-name3', semanticInterventionUri: 'semanticInterventionUri2'},
      ],
      trialverseTrialDataResource;

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

      trailverseTrailDataDefered = $q.defer();
      mockTrialData.$promise = trailverseTrailDataDefered.promise;

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

      networkMetaAnalysisService = jasmine.createSpyObj('NetworkMetaAnalysisService', ['transformTrialDataToTableRows']);


      $controller('NetworkMetaAnalysisController', {
        $scope: scope,
        $stateParams: mockStateParams,
        OutcomeResource: outcomeResource,
        InterventionResource: interventionResource,
        TrialverseTrialDataResource: trialverseTrialDataResource,
        NetworkMetaAnalysisService: networkMetaAnalysisService
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

    });

    describe('when the analysis, outcomes, interventions and project are loaded', function() {

      beforeEach(inject(function($controller) {
        analysisDeferred.resolve(mockAnalysis);
        projectDeferred.resolve(mockProject);
        interventionDeferred.resolve(mockInterventions);
        outcomesDeferred.resolve(mockOutcomes);
        scope.$apply();
      }));

      it('should save the analysis when the selected outcome changes', function() {
        scope.analysis.outcome = {id: 1};
        scope.saveAnalysis();
        expect(scope.analysis.$save).toHaveBeenCalled();
      });

      describe('and there is already an outcome defined on the analysis', function () {
        
        it('should get the tabledata', function () {
          expect(trialverseTrialDataResource.get).toHaveBeenCalledWith({
            id: mockProject.trialverseId,
            outcomeUri: mockOutcomes[0].semanticOutcomeUri,
            interventionUris: [
              mockInterventions[0].semanticInterventionUri,
              mockInterventions[1].semanticInterventionUri, 
              mockInterventions[2].semanticInterventionUri]
          });
        });
      });

    });

  });
});
define(['angular', 'angular-mocks', 'controllers'], function() {
  describe("The NEW Single Study Benefit-Risk AnalysisController", function() {
    var scope;
    var mockStateParams = {};
    var state = jasmine.createSpyObj('state', ['go']);
    var mockWindow = {};
    var outcomeResource = jasmine.createSpyObj('outcomeResource', ['query']);
    var interventionResource = jasmine.createSpyObj('InterventionResource', ['query']);
    var analysisResource = jasmine.createSpyObj('AnalysisResource', ['save']);
    var trialverseStudyResource = jasmine.createSpyObj('TrialverseStudyResource', ['query']);
    var problemResource = jasmine.createSpyObj('problemResource', ['get']);
    var singleStudyBenefitRiskAnalysisService = jasmine.createSpyObj('singleStudyBenefitRiskAnalysisService', ['getProblem', 'getDefaultScenario', 'validateAnalysis', 'validateProblem', 'concatWithNoDuplicates', 'findMissing']);
    var outcomesDeferred;
    var interventionDeferred;
    var mockOutcomes = [{
      name: 'mock outcome 1'
    }];
    var mockInterventions = [{
      name: 'mock intervention 1'
    }];
    var mockStudies = [{
      uid: 'uid1',
    }, {
      uid: 'uid2'
    }];


    beforeEach(module('addis.controllers'));

    beforeEach(inject(function($controller, $q, $rootScope) {

      scope = $rootScope;

      // set the mock state params 
      mockStateParams.projectId = 1;
      mockStateParams.analysisId = 2;

      // set a mockNameSpace for the current project
      scope.project = {
        namespaceUid: 456
      };

      // set some mock outcomes and interventions
      scope.analysis = {
        selectedOutcomes: [],
        selectedInterventions: [],
        studyUid: 'uid2'
      };
      scope.analysis.selectedOutcomes = [{
        a: 'a'
      }, {
        b: 'b'
      }];
      scope.analysis.selectedInterventions = [{
        c: 'c'
      }, {
        d: 'd'
      }];

      // set a mock result value for the service call 
      singleStudyBenefitRiskAnalysisService.validateAnalysis.and.returnValue(false);
      singleStudyBenefitRiskAnalysisService.concatWithNoDuplicates.and.returnValue(mockOutcomes);
      singleStudyBenefitRiskAnalysisService.findMissing.and.returnValue([]);

      outcomesDeferred = $q.defer();
      mockOutcomes.$promise = outcomesDeferred.promise;
      outcomeResource.query.and.returnValue(mockOutcomes);

      interventionDeferred = $q.defer();
      mockInterventions.$promise = interventionDeferred.promise;
      interventionResource.query.and.returnValue(mockInterventions);

      studiesDeferred = $q.defer();
      mockStudies.$promise = studiesDeferred.promise;
      trialverseStudyResource.query.and.returnValue(mockStudies);

      ctrl = $controller('SingleStudyBenefitRiskAnalysisController', {
        $scope: scope,
        $stateParams: mockStateParams,
        $state: state,
        $q: $q,
        $window: mockWindow,
        'OutcomeResource': outcomeResource,
        'InterventionResource': interventionResource,
        'TrialverseStudyResource': trialverseStudyResource,
        'ProblemResource': problemResource,
        'SingleStudyBenefitRiskAnalysisService': singleStudyBenefitRiskAnalysisService,
        'DEFAULT_VIEW': 'someview',
        'AnalysisResource': analysisResource
      });
    }));

    describe('on load', function() {

      it('should set the outcomes to equal the already selected outcomes', function() {
        expect(scope.outcomes).toEqual(scope.analysis.selectedOutcomes);
      });

      it('should set the interventions to equal the already selected interventions', function() {
        expect(scope.interventions).toEqual(scope.analysis.selectedInterventions);
      });

      it('should set isValidAnalysis flag to false', function() {
        expect(scope.isValidAnalysis).toEqual(false);
      });

      it('should query the outcomes for the current project', function() {
        expect(outcomeResource.query).toHaveBeenCalledWith({
          projectId: 1
        });
      });

      it('should query the interventions for the current project', function() {
        expect(interventionResource.query).toHaveBeenCalledWith({
          projectId: 1
        });
      });

      it('should query studies for the current project', function() {
        expect(trialverseStudyResource.query).toHaveBeenCalledWith({
          namespaceUid: 456
        });
      });
    });

    describe('when the outcome query resolves', function() {

      beforeEach(function() {
        outcomesDeferred.resolve(mockOutcomes);
        scope.$apply();
      });

      it('update the outcome list to contain the already selectedOutcomes as well as the outcome options (that have not been selected)', function() {
        expect(scope.outcomes).toBe(mockOutcomes);
      });

    });

    describe('when the interventions query resolves', function() {

      beforeEach(function() {
        interventionDeferred.resolve(mockInterventions);
        scope.$apply();
      });

      it('update the outcome list to contain the already selectedOutcomes as well as the outcome options (that have not been selected)', function() {
        expect(singleStudyBenefitRiskAnalysisService.concatWithNoDuplicates).toHaveBeenCalled();
      });

    });

    describe('when the study query resolves', function() {

      beforeEach(function() {
        studiesDeferred.resolve(mockStudies);
        scope.$apply();
      });

      it('should place the studies on the scope', function() {
        expect(scope.studies).toEqual(mockStudies);
      });

      it('should place the selected study on the studyModel', function() {
        expect(scope.studyModel.selectedStudy).toBe(mockStudies[1]);
        // expect()
      });

      it('should look for missing outcomes and interventions', function() {
        expect(singleStudyBenefitRiskAnalysisService.findMissing).toHaveBeenCalled();
      });
    });

    describe('when a study is selected', function() {
      it('should place the selected items ui on the analysis as the studyUid', function() {
        scope.onStudySelect({
          uid: 'yo uid'
        });
        expect(scope.analysis.studyUid).toEqual('yo uid');
      });
    });

  });
});
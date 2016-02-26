'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('meta benefit-risk step 1 controller', function() {

    var scope, q,
      stateParamsMock = {
        projectId: 1
      },
      analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['get', 'query']),
      interventionResourceMock = jasmine.createSpyObj('InterventionResource', ['query']),
      outcomeResourceMock = jasmine.createSpyObj('OutcomeResource', ['query']),
      analysisDefer,
      analysisQueryDefer,
      interventionDefer,
      outcomeDefer;

    beforeEach(module('addis.analysis'));


    beforeEach(angularMocks.inject(function($rootScope, $q, $controller) {
      scope = $rootScope;
      q = $q;
      analysisDefer = q.defer();
      analysisQueryDefer = q.defer();
      interventionDefer = q.defer();
      outcomeDefer = q.defer();

      analysisResourceMock.get.and.returnValue({
        $promise: analysisDefer.promise
      });
      analysisResourceMock.query.and.returnValue({
        $promise: analysisQueryDefer.promise
      });
      interventionResourceMock.query.and.returnValue({
        $promise: interventionDefer.promise
      });
      outcomeResourceMock.query.and.returnValue({
        $promise: outcomeDefer.promise
      });

      $controller('MetaBenefitRiskStep1Controller', {
        $scope: scope,
        $q: q,
        $stateParams: stateParamsMock,
        AnalysisResource: analysisResourceMock,
        InterventionResource: interventionResourceMock,
        OutcomeResource: outcomeResourceMock
      });

    }));

    describe('when the crap has loaded', function() {
      beforeEach(function() {
        var analysis = {
          includedAlternatives: [{
            id: 1
          }],
          mbrOutcomeInclusions: [{
            outcome: {
              id: 1
            },
            networkMetaAnalysis: undefined
          }]
        };
        var interventions = [{
          id: 1
        }, {
          id: 2
        }];
        var outcomes = [{
          id: 1
        }, {
          id: 2
        }];
        var networkAnalyses = [{
          outcome: {
            id: 1
          }
        }, {
          outcome: {
            id: 2
          }
        }];
        analysisDefer.resolve(analysis);
        interventionDefer.resolve(interventions);
        outcomeDefer.resolve(outcomes);
        analysisQueryDefer.resolve(networkAnalyses);
        scope.$digest();
      });
      it('should set included alternatives isIncluded to true', function() {
        expect(scope.alternatives[0].isIncluded).toBeTruthy();
        expect(scope.alternatives[1].isIncluded).toBeFalsy();
      });
      it('should set included outcomes isIncluded to true', function() {
        expect(scope.outcomes[0].isIncluded).toBeTruthy();
        expect(scope.outcomes[1].isIncluded).toBeFalsy();
      });
      it('should query the network analyses', function() {
        expect(analysisResourceMock.query).toHaveBeenCalledWith({
          projectId: 1,
          outcomeIds: [1, 2]
        });
      });
      it('should filter and join the networkAnalyses with the outcomes', function() {

        var expectedOutcomesWithAnlyses = [{
          outcome: {
            id: 1,
            isIncluded: true
          },
          analyses: [{
            outcome: {
              id: 1
            }
          }]
        }, {
          outcome: {
            id: 2
          },
          analyses: [{
            outcome: {
              id: 2
            }
          }]
        }];

        expect(scope.outcomesWithAnalyses).toEqual(expectedOutcomesWithAnlyses);
      });

    });

  });
});

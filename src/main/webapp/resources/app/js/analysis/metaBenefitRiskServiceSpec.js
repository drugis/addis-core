'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('meta-benefit-risk service', function() {
    var metaBenefitRiskService;

    beforeEach(angularMocks.module('addis.analysis'));

    beforeEach(inject(function(MetaBenefitRiskService) {
      metaBenefitRiskService = MetaBenefitRiskService;
    }));

    describe('buildOutcomesWithAnalyses', function() {
      it('should build inclusions when the outcome is included', function() {
        var outcome = {
          id: 1,
          isIncluded: true
        };
        var analysis = {
          id: 7,
          mbrOutcomeInclusions: [{
            outcomeId: 1,
            networkMetaAnalysisId: 3
          }]
        };
        var networkMetaAnalyses = [{
          id: 3,
          outcome: outcome
        }, {
          id: 5,
          outcome: outcome
        }];
        var expectedResult = {
          outcome: outcome,
          networkMetaAnalyses: [{
            id: 3,
            outcome: {
              id: 1,
              isIncluded: true
            }
          }, {
            id: 5,
            outcome: {
              id: 1,
              isIncluded: true
            }
          }],
          selectedAnalysisId: 3
        };

        var result = metaBenefitRiskService.buildOutcomesWithAnalyses(outcome, analysis, networkMetaAnalyses);

        expect(result).toEqual(expectedResult);
      });

      it('should include the first analysis if no inclusions are set', function() {
        var outcome = {
          id: 1,
          isIncluded: true
        };
        var analysis = {
          id: 7,
          mbrOutcomeInclusions: []
        };
        var networkMetaAnalyses = [{
          id: 3,
          outcome: outcome
        }, {
          id: 5,
          outcome: outcome
        }];

        var expectedResult = {
          outcome: outcome,
          networkMetaAnalyses: [{
            id: 3,
            outcome: {
              id: 1,
              isIncluded: true
            }
          }, {
            id: 5,
            outcome: {
              id: 1,
              isIncluded: true
            }
          }],
          selectedAnalysisId: 3
        };

        var result = metaBenefitRiskService.buildOutcomesWithAnalyses(outcome, analysis, networkMetaAnalyses);

        expect(result).toEqual(expectedResult);
      });

    });

  });
});

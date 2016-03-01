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

        var result = metaBenefitRiskService.buildOutcomesWithAnalyses(analysis, networkMetaAnalyses, outcome);

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

        var result = metaBenefitRiskService.buildOutcomesWithAnalyses(analysis, networkMetaAnalyses, outcome);

        expect(result).toEqual(expectedResult);
      });
    });

    describe('joinModelsWithAnalysis', function(){
      it('should add the models to the analyses if they belong to it',function(){
        var models = [{analysisId: 1}, {analysisId: 2}, {analysisId: 1}];
        var analysis = {
          id: 1
        };
        var result = metaBenefitRiskService.joinModelsWithAnalysis(models, analysis);
        expect(result.models).toEqual([models[0], models[2]]);
      });
    });

  });
});

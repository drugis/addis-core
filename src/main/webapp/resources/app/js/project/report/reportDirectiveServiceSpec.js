'use strict';
define(['angular-mocks'], function() {
  describe('the report directive service', function() {
    var reportDirectiveService;

    beforeEach(module('addis.project'));

    beforeEach(inject(function(ReportDirectiveService) {
      reportDirectiveService = ReportDirectiveService;
    }));

    describe('inlineDirectives', function() {
      it('should substitute the network-plot', function() {
        var input =
          'report text\n' +
          '===========\n' +
          '[[[network-plot analysis-id=&#34;93&#34;]]]';
        var expectedResult =
          'report text\n' +
          '===========\n' +
          '<div style="max-width:500px"><network-plot analysis-id="93"></network-plot></div>';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });

      it('should substitute the comparison directive', function() {
        var input = '[[[comparison-result analysis-id=&#34;37&#34; model-id=&#34;42&#34; t1=&#34;123&#34; t2=&#34;321&#34;]]]';
        var expectedResult = '<comparison-result analysis-id="37" model-id="42" t1="123" t2="321"></comparison-result>';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });

      it('should ignore non-whitelisted stuff', function() {
        var input = '[[[leethaxxor-directive sql-inject="pwned"]]]';
        var expectedResult = '[[[leethaxxor-directive sql-inject="pwned"]]]';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
      it('should work for multiple instances of network-plot', function() {
        var input = '[[[network-plot analysis-id=&#34;93&#34;]]]  [[[network-plot analysis-id=&#34;93&#34;]]]';
        var expectedResult = '<div style="max-width:500px"><network-plot analysis-id="93"></network-plot></div>  <div style="max-width:500px"><network-plot analysis-id="93"></network-plot></div>';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
      it('should work for multiple instances of comparison-result', function() {
        var input = '[[[comparison-result analysis-id=&#34;37&#34; model-id=&#34;42&#34; t1=&#34;123&#34; t2=&#34;321&#34;]]] [[[comparison-result analysis-id=&#34;37&#34; model-id=&#34;42&#34; t1=&#34;123&#34; t2=&#34;321&#34;]]]';
        var expectedResult = '<comparison-result analysis-id="37" model-id="42" t1="123" t2="321"></comparison-result> <comparison-result analysis-id="37" model-id="42" t1="123" t2="321"></comparison-result>';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
      it('should work for instances of relative-effects-table', function() {
        var input = '[[[relative-effects-table analysis-id=&#34;37&#34; model-id=&#34;42&#34; regression-level=&#34;100&#34;]]] [[[relative-effects-table analysis-id=&#34;37&#34; model-id=&#34;42&#34;]]]';
        var expectedResult = '<relative-effects-table analysis-id="37" model-id="42" regression-level="100"></relative-effects-table> <relative-effects-table analysis-id="37" model-id="42"></relative-effects-table>';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
      it('should work for instances of relative-effects-plot', function() {
        var input = '[[[relative-effects-plot analysis-id=&#34;37&#34; model-id=&#34;42&#34; baseline-treatment-id=&#34;5&#34; regression-level=&#34;100&#34;]]] [[[relative-effects-plot analysis-id=&#34;37&#34; model-id=&#34;42&#34; baseline-treatment-id=&#34;5&#34;]]]';
        var expectedResult = '<relative-effects-plot analysis-id="37" model-id="42" baseline-treatment-id="5" regression-level="100"></relative-effects-plot> <relative-effects-plot analysis-id="37" model-id="42" baseline-treatment-id="5"></relative-effects-plot>';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
      it('should work for instances of rank-probabilities-table', function() {
        var input = '[[[rank-probabilities-table analysis-id=&#34;37&#34; model-id=&#34;42&#34; regression-level=&#34;100&#34;]]] [[[rank-probabilities-table analysis-id=&#34;37&#34; model-id=&#34;42&#34;]]]';
        var expectedResult = '<rank-probabilities-table analysis-id="37" model-id="42" regression-level="100"></rank-probabilities-table> <rank-probabilities-table analysis-id="37" model-id="42"></rank-probabilities-table>';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
      it('should work for instances of rank-probabilities-plot', function() {
        var input = '[[[rank-probabilities-plot analysis-id=&#34;37&#34; model-id=&#34;42&#34; baseline-treatment-id=&#34;5&#34; regression-level=&#34;100&#34;]]] [[[rank-probabilities-plot analysis-id=&#34;37&#34; model-id=&#34;42&#34; baseline-treatment-id=&#34;5&#34;]]]';
        var expectedResult = '<rank-probabilities-plot analysis-id="37" model-id="42" baseline-treatment-id="5" regression-level="100"></rank-probabilities-plot> <rank-probabilities-plot analysis-id="37" model-id="42" baseline-treatment-id="5"></rank-probabilities-plot>';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
    });
    describe('getNonNodeSplitModels', function() {
      it('should retrieve the non-nodesplit models and for regression models with levels add the "centering" level', function() {
        var synthesisModel = {
          modelType: {
            type: 'evidence synthesis'
          }
        };
        var regressionModelNoLevels = {
          modelType: {
            type: 'regression'
          },
          regressor: {}
        };
        var regressionModelLevels = {
          modelType: {
            type: 'regression'
          },
          regressor: {
            levels: [1, 2]
          }
        };
        var nodesplitModel = {
          modelType: {
            type: 'node-split'
          }
        };
        var models = [synthesisModel, regressionModelNoLevels, regressionModelLevels, nodesplitModel];
        var expectedResult = [synthesisModel, regressionModelNoLevels, {
          modelType: {
            type: 'regression'
          },
          regressor: {
            levels: ['centering', 1, 2]
          }
        }];
        var result = reportDirectiveService.getNonNodeSplitModels(models);
        expect(result).toEqual(expectedResult);
      });
    });
  });
});

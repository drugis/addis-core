'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('the report directive service', function() {
    var reportDirectiveService;

    beforeEach(angular.mock.module('addis.project'));

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
        var input = '[[[rank-probabilities-plot analysis-id=&#34;37&#34; model-id=&#34;42&#34; regression-level=&#34;100&#34;]]] [[[rank-probabilities-plot analysis-id=&#34;37&#34; model-id=&#34;42&#34;]]]';
        var expectedResult = '<rank-probabilities-plot analysis-id="37" model-id="42" regression-level="100"></rank-probabilities-plot> <rank-probabilities-plot analysis-id="37" model-id="42"></rank-probabilities-plot>';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
      it('should work for instances of treatment-effects', function() {
        var input = '[[[treatment-effects analysis-id=&#34;37&#34; model-id=&#34;42&#34; baseline-treatment-id=&#34;34&#34; sorting-type=&#34;\'alphabetical\'&#34; regression-level=&#34;100&#34;]]] [[[treatment-effects analysis-id=&#34;37&#34; model-id=&#34;42&#34; baseline-treatment-id=&#34;34&#34; sorting-type=&#34;\'alphabetical\'&#34;]]]';
        var expectedResult = '<treatment-effects analysis-id="37" model-id="42" baseline-treatment-id="34" sorting-type="\'alphabetical\'" regression-level="100"></treatment-effects> <treatment-effects analysis-id="37" model-id="42" baseline-treatment-id="34" sorting-type="\'alphabetical\'"></treatment-effects>';
        var result = reportDirectiveService.inlineDirectives(input);
        expect(result).toEqual(expectedResult);
      });
    });

    describe('getDirectiveBuilder', function() {
      it('for network-plot should return a network plot builder', function() {
        var builder = reportDirectiveService.getDirectiveBuilder('network-plot');
        var selections = {
          analysis: {
            id: 3
          }
        };
        expect(builder(selections)).toEqual('[[[network-plot analysis-id="3"]]]');
      });
      it('for comparison-result should return a comparison result builder', function() {
        var builder = reportDirectiveService.getDirectiveBuilder('comparison-result');
        var selections = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          },
          t1: {
            id: 300
          },
          t2: {
            id: 3000
          }
        };
        expect(builder(selections)).toEqual('[[[comparison-result analysis-id="3" model-id="30" t1="300" t2="3000"]]]');
      });
      it('for relative-effects-table should return a relative effects table builder (both with and without regression level)', function() {
        var builder = reportDirectiveService.getDirectiveBuilder('relative-effects-table');
        var selectionsNoRegression = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          }
        };
        var selectionsWithRegression = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          },
          regressionLevel: 300
        };
        expect(builder(selectionsNoRegression)).toEqual('[[[relative-effects-table analysis-id="3" model-id="30"]]]');
        expect(builder(selectionsWithRegression)).toEqual('[[[relative-effects-table analysis-id="3" model-id="30" regression-level="300"]]]');
      });
      it('for relative-effects-plot should return a relative effects plot builder (both with and without regression level)', function() {
        var builder = reportDirectiveService.getDirectiveBuilder('relative-effects-plot');
        var selectionsNoRegression = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          },
          baselineIntervention: {
            id: 300
          }
        };
        var selectionsWithRegression = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          },
          regressionLevel: 300,
          baselineIntervention: {
            id: 3000
          }
        };
        expect(builder(selectionsNoRegression)).toEqual('[[[relative-effects-plot analysis-id="3" model-id="30" baseline-treatment-id="300"]]]');
        expect(builder(selectionsWithRegression)).toEqual('[[[relative-effects-plot analysis-id="3" model-id="30" baseline-treatment-id="3000" regression-level="300"]]]');
      });
      it('for rank-probabilities-table should return a rank probabilities table builder (both with and without regression level)', function() {
        var builder = reportDirectiveService.getDirectiveBuilder('rank-probabilities-table');
        var selectionsNoRegression = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          }
        };
        var selectionsWithRegression = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          },
          regressionLevel: 300
        };
        expect(builder(selectionsNoRegression)).toEqual('[[[rank-probabilities-table analysis-id="3" model-id="30"]]]');
        expect(builder(selectionsWithRegression)).toEqual('[[[rank-probabilities-table analysis-id="3" model-id="30" regression-level="300"]]]');
      });
      it('for rank-probabilities-plot should return a rank probabilities plot builder (both with and without regression level)', function() {
        var builder = reportDirectiveService.getDirectiveBuilder('rank-probabilities-plot');
        var selectionsNoRegression = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          },
          baselineIntervention: {
            id: 300
          }
        };
        var selectionsWithRegression = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          },
          regressionLevel: 300,
          baselineIntervention: {
            id: 3000
          }
        };
        expect(builder(selectionsNoRegression)).toEqual('[[[rank-probabilities-plot analysis-id="3" model-id="30"]]]');
        expect(builder(selectionsWithRegression)).toEqual('[[[rank-probabilities-plot analysis-id="3" model-id="30" regression-level="300"]]]');
      });
      it('for forest-plot should return a forest plot builder', function() {
        var builder = reportDirectiveService.getDirectiveBuilder('forest-plot');
        var selections = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          }
        };
        expect(builder(selections)).toEqual('[[[forest-plot analysis-id="3" model-id="30"]]]');
      });
      it('for treatment-effects should return a treatment effects builder', function() {
        var builder = reportDirectiveService.getDirectiveBuilder('treatment-effects');
        var selectionsNoRegression = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          },
          baselineIntervention: {
            id: 300
          },
          sortingType: 'alphabetical'
        };
        var selectionsWithRegression = {
          analysis: {
            id: 3
          },
          model: {
            id: 30
          },
          regressionLevel: 300,
          baselineIntervention: {
            id: 3000
          },
          sortingType: 'alphabetical'
        };
        expect(builder(selectionsNoRegression)).toEqual('[[[treatment-effects analysis-id="3" model-id="30" baseline-treatment-id="300" sorting-type="\'alphabetical\'"]]]');
        expect(builder(selectionsWithRegression)).toEqual('[[[treatment-effects analysis-id="3" model-id="30" baseline-treatment-id="3000" sorting-type="\'alphabetical\'" regression-level="300"]]]');
      });
    });

    describe('getAllowedModels', function() {
      it('should retrieve the non-nodesplit models and for regression models with levels add the "centering" level', function() {
        var synthesisModel = {
          modelType: {
            type: 'network'
          },
          runStatus: 'done'
        };
        var regressionModelNoLevels = {
          modelType: {
            type: 'regression'
          },
          regressor: {},
          runStatus: 'done'
        };
        var regressionModelLevels = {
          modelType: {
            type: 'regression'
          },
          regressor: {
            levels: [1, 2]
          },
          runStatus: 'done'
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
          },
          runStatus: 'done'
        }];
        var result = reportDirectiveService.getAllowedModels(models, 'relative-effects-table');
        expect(result).toEqual(expectedResult);
      });
    });

  });
});

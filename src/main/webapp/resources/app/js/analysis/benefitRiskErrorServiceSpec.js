'use strict';
define(['lodash', 'angular-mocks', './analysis'], function(_) {
  describe('benefit-risk error service', function() {
    var benefitRiskErrorService;

    beforeEach(angular.mock.module('addis.analysis'));

    beforeEach(inject(function(BenefitRiskErrorService) {
      benefitRiskErrorService = BenefitRiskErrorService;
    }));

    describe('hasMissingStudy', function() {
      it('should be false if everything is fine', function() {
        var outcomeInclusions = [{
          dataType: 'network'
        }, {
          dataType: 'single-study',
          selectedStudy: {
            studyUri: 'http://cool.ninja'
          }
        }];
        expect(benefitRiskErrorService.hasMissingStudy(outcomeInclusions)).toBeFalsy();
      });
      it('should be true if a study is not selected', function() {
        var networkOnly = [{
          dataType: 'network'
        }];
        expect(benefitRiskErrorService.hasMissingStudy(networkOnly)).toBeFalsy();

        var withGoodSelection = [{
          dataType: 'single-study',
          selectedStudy: {
            studyUri: 'http://cool.ninja'
          }
        }];
        expect(benefitRiskErrorService.hasMissingStudy(withGoodSelection)).toBeFalsy();

        var withMissingStudy1 = [{
          dataType: 'single-study',
          selectedStudy: {}
        }];
        expect(benefitRiskErrorService.hasMissingStudy(withMissingStudy1)).toBeTruthy();
        var withMissingStudy2 = [{
          dataType: 'single-study',
        }];
        expect(benefitRiskErrorService.hasMissingStudy(withMissingStudy2)).toBeTruthy();
      });
    });

    describe('isInvalidStudySelected', function() {
      it('should be false if everything is ok', function() {
        var outcomeInclusions = [{
          dataType: 'network'
        }, {
          dataType: 'single-study',
          selectedStudy: {
            missingInterventions: [],
            missingOutcomes: []
          }
        }];
        expect(benefitRiskErrorService.isInvalidStudySelected(outcomeInclusions)).toBeFalsy();
      });
      it('should be true if there are missing interventions', function() {
        var outcomeInclusions = [{
          dataType: 'single-study',
          selectedStudy: {
            missingInterventions: [{}],
            missingOutcomes: []
          }
        }];
        expect(benefitRiskErrorService.isInvalidStudySelected(outcomeInclusions)).toBeTruthy();
      });
      it('should be true if there are missing outcomes', function() {
        var outcomeInclusions = [{
          dataType: 'single-study',
          selectedStudy: {
            missingInterventions: [],
            missingOutcomes: [{}]
          }
        }];
        expect(benefitRiskErrorService.isInvalidStudySelected(outcomeInclusions)).toBeTruthy();
      });
    });

    describe('numberOfSelectedOutcomes', function() {
      it('should return the number of selected outcomes, which do not contain archived analyses or models', function() {
        var outcomesWithAnalyses = [{
          outcome: {
            isIncluded: false
          },
          selectedAnalysis: {
            archived: true
          },
          selectedModel: {
            archived: false
          }
        }, {
          outcome: {
            isIncluded: true
          }
        }, {
          outcome: {
            isIncluded: true
          },
          selectedStudy: {}
        }, {
          outcome: {
            isIncluded: true
          },
          selectedAnalysis: {
            archived: true
          },
          selectedModel: {
            archived: false
          }
        }, {
          outcome: {
            isIncluded: true
          },
          selectedAnalysis: {
            archived: false
          },
          selectedModel: {
            archived: false
          }
        }];
        expect(benefitRiskErrorService.numberOfSelectedOutcomes(outcomesWithAnalyses)).toBe(2);
      });
    });

    describe('isMissingAnalysis', function() {
      it('should return true if any outcome selection is a network but missing a selected analysis', function() {
        var noIncludedOutcome = [{
          dataType: 'network',
          outcome: {
            isIncluded: false
          },
          selectedAnalysis: {
            id: 1
          }
        }];
        expect(benefitRiskErrorService.isMissingAnalysis(noIncludedOutcome)).toBeFalsy();
        var includedWithAnalysis = [{
          dataType: 'network',
          outcome: {
            isIncluded: true
          },
          selectedAnalysis: {
            id: 1
          }
        }];
        expect(benefitRiskErrorService.isMissingAnalysis(includedWithAnalysis)).toBeFalsy();
        var includedWithoutAnalysis = [{
          dataType: 'network',
          outcome: {
            isIncluded: true
          }
        }, {
          dataType: 'study',
          outcome: {
            isIncluded: true
          },
          selectedAnalysis: {
            nonsense: 'yo'
          }
        }];
        expect(benefitRiskErrorService.isMissingAnalysis(includedWithoutAnalysis)).toBeTruthy();
      });
    });

    describe('isMissingDataType', function() {
      it('should return true if there is an outcome for which the data source type has not yet been chosen', function() {
        var includedButNoType = [{
          outcome: {
            isIncluded: true
          }
        }];
        expect(benefitRiskErrorService.isMissingDataType(includedButNoType)).toBeTruthy();
        var notIncluded = [{
          outcome: {
            isIncluded: false
          }
        }];
        expect(benefitRiskErrorService.isMissingDataType(notIncluded)).toBeFalsy();
        var includedWithType = [{
          dataType: 'network',
          outcome: {
            isIncluded: true
          }
        }];
        expect(benefitRiskErrorService.isMissingDataType(includedWithType)).toBeFalsy();
      });
    });

    describe('isModelWithMissingAlternatives', function() {
      it('should return true if any selected outcome has a model with missing alternatives', function() {
        var outcomesWithAnalyses = [{
          outcome: {
            isIncluded: false
          },
          selectedModel: {
            missingAlternatives: []
          }
        }, {
          outcome: {
            isIncluded: true
          },
          selectedModel: {
            missingAlternatives: [1, 2, 3]
          }
        }, {
          outcome: {
            isIncluded: true
          },
          selectedModel: {
            missingAlternatives: []
          }
        }];
        expect(benefitRiskErrorService.isModelWithMissingAlternatives(outcomesWithAnalyses)).toBeTruthy();
      });
      it('should return false if none of the selected outcomes has a model with missing alternatives', function() {
        var outcomesWithAnalyses = [{
          outcome: {
            isIncluded: false
          },
          selectedModel: {
            missingAlternatives: []
          }
        }, {
          outcome: {
            isIncluded: true
          },
          selectedModel: {
            missingAlternatives: []
          }
        }, {
          outcome: {
            isIncluded: true
          },
          selectedModel: {
            missingAlternatives: []
          }
        }];
        expect(benefitRiskErrorService.isModelWithMissingAlternatives(outcomesWithAnalyses)).toBeFalsy();
      });
    });

    describe('isModelWithoutResults', function() {
      it('should return true if any selected outcome has a model with missing results', function() {
        var outcomesWithAnalyses = [{
          outcome: {
            isIncluded: false
          },
          selectedModel: {
            runStatus: 'done'
          }
        }, {
          outcome: {
            isIncluded: true
          },
          selectedModel: {
            runStatus: 'running'
          }
        }, {
          outcome: {
            isIncluded: true
          },
          selectedModel: {
            runStatus: 'done'
          }
        }];
        expect(benefitRiskErrorService.isModelWithoutResults(outcomesWithAnalyses)).toBeTruthy();
      });

      it('should return false if none of the selected outcomes has a model with missing results', function() {
        var outcomesWithAnalyses = [{
          outcome: {
            isIncluded: false
          },
          selectedModel: {
            runStatus: 'done'
          }
        }, {
          outcome: {
            isIncluded: true
          },
          selectedModel: {
            runStatus: 'done'
          }
        }];
        expect(benefitRiskErrorService.isModelWithoutResults(outcomesWithAnalyses)).toBeFalsy();
      });
    });
    
    describe('findOverlappingOutcomes', function() {
      it('should return a list with a error ifof outcomes which share concepts with an other', function() {
        var outcomes = [{
          outcome: {
            isIncluded: true,
            title: 'outcome 1',
            semanticOutcomeUri: '123'
          }
        }, {
          outcome: {
            isIncluded: true,
            title: 'outcome 2',
            semanticOutcomeUri: '123'
          }
        }, {
          outcome: {
            isIncluded: false,
            title: 'outcome 3',
            semanticOutcomeUri: '123'
          }
        }];
        var result = benefitRiskErrorService.findOverlappingOutcomes(outcomes);
        var expectedResult = [
          [{
            isIncluded: true,
            title: 'outcome 1',
            semanticOutcomeUri: '123'
          }, {
            isIncluded: true,
            title: 'outcome 2',
            semanticOutcomeUri: '123'
          }]
        ];
        expect(result).toEqual(expectedResult);
      });
    });
  });
});

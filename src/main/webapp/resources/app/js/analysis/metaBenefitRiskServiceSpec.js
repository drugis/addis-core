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
            networkMetaAnalysisId: 3,
            modelId: 1
          }]
        };
        var networkMetaAnalyses = [{
          id: 3,
          outcome: outcome
        }, {
          id: 5,
          outcome: outcome
        }];
        var models = [{
          id: 1
        }, {
          id: 2
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
          selectedAnalysis: {
            id: 3,
            outcome: {
              id: 1,
              isIncluded: true
            }
          },
          selectedModel: models[0]
        };

        var result = metaBenefitRiskService.buildOutcomesWithAnalyses(analysis, networkMetaAnalyses, models, outcome);

        expect(result).toEqual(expectedResult);
      });
    });

    describe('joinModelsWithAnalysis', function() {
      it('should add the models to the analyses if they belong to it', function() {
        var models = [{
          analysisId: 1
        }, {
          analysisId: 2
        }, {
          analysisId: 1
        }];
        var analysis = {
          id: 1
        };
        var result = metaBenefitRiskService.joinModelsWithAnalysis(models, analysis);
        expect(result.models).toEqual([models[0], models[2]]);
      });
    });

    describe('compareAnalysesByModels', function() {
      it('should place analyses with models before those without', function() {
        var a = {
          models: [1]
        };
        var b = {
          models: []
        };
        expect(metaBenefitRiskService.compareAnalysesByModels(a, b)).toBe(-1);
        expect(metaBenefitRiskService.compareAnalysesByModels(b, a)).toBe(1);
      });
      it('should do nothing if both analyses have models', function() {
        var a = {
          models: [1]
        };
        var b = {
          models: [1]
        };
        expect(metaBenefitRiskService.compareAnalysesByModels(a, b)).toBe(0);
        expect(metaBenefitRiskService.compareAnalysesByModels(b, a)).toBe(0);
      });
      it('should do nothing if neither analysis has a model', function() {
        var a = {
          models: []
        };
        var b = {
          models: []
        };
        expect(metaBenefitRiskService.compareAnalysesByModels(a, b)).toBe(0);
        expect(metaBenefitRiskService.compareAnalysesByModels(b, a)).toBe(0);
      });
    });

    describe('addModelsGroup', function() {
      it('should decorate the models with their group', function() {

        var result = metaBenefitRiskService.addModelsGroup({
          primaryModel: 1,
          models: [{
            id: 1
          }, {
            id: 2
          }]
        });
        expect(result).toEqual({
          primaryModel: 1,
          models: [{
            id: 1,
            group: 'Primary model'
          }, {
            id: 2,
            group: 'Other models'
          }]
        });
      });
    });

    describe('numberOfSelectedInterventions', function() {
      it('should return the number of selected interventions', function() {
        var alternatives = [{
          isIncluded: false
        }, {}, {
          isIncluded: true
        }, {
          isIncluded: true
        }];
        expect(metaBenefitRiskService.numberOfSelectedInterventions(alternatives)).toBe(2);
      });
    });

    describe('numberOfSelectedOutcomes', function() {
      it('should return the number of selected outcomes, which do not contain archived analyses or models', function() {
        var outcomesWithAnalyses = [{
          outcome: {
            isIncluded: false
          }, selectedAnalysis: {
            archived: true
          }, selectedModel: {
            archived: false
          }
        }, {
          outcome: {
            isIncluded: true
          }
        }, {
          outcome: {
            isIncluded: true
          }, selectedAnalysis: {
            archived: true
          }, selectedModel: {
            archived: false
          }
        }, {
          outcome: {
            isIncluded: true
          }, selectedAnalysis: {
            archived: false
          }, selectedModel: {
            archived: false
          }
        }];
        expect(metaBenefitRiskService.numberOfSelectedOutcomes(outcomesWithAnalyses)).toBe(1);
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
        expect(metaBenefitRiskService.isModelWithMissingAlternatives(outcomesWithAnalyses)).toBeTruthy();
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
        expect(metaBenefitRiskService.isModelWithMissingAlternatives(outcomesWithAnalyses)).toBeFalsy();
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
        expect(metaBenefitRiskService.isModelWithoutResults(outcomesWithAnalyses)).toBeTruthy();
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
        expect(metaBenefitRiskService.isModelWithoutResults(outcomesWithAnalyses)).toBeFalsy();
      });
    });
    describe('findMissingAlternatives for a pairwise analysis', function() {
      it('should return a list of alternatives that are included in the analysis but not in the model', function() {
        var interventionInclusions = [{
          id: 1
        }, {
          id: 2
        }, {
          id: 3
        }, {
          id: 4
        }];
        var expectedResult = interventionInclusions.slice(2);
        var owa = {
          selectedModel: {
            modelType: {
              type: 'pairwise',
              details: {
                from: {
                  id: 1
                },
                to: {
                  id: 2
                }
              }
            }
          }
        };
        var result = metaBenefitRiskService.findMissingAlternatives(interventionInclusions, owa);
        expect(result).toEqual(expectedResult);
      });
    });
    describe('findMissingAlternatives for evidence synthesis', function() {
      it('should return a list of alternatives that are included in the analysis but not in the model', function() {
        var interventionInclusions = [{
          id: 1
        }, {
          id: 2
        }, {
          id: 3
        }, {
          id: 4
        }];
        var expectedResult = interventionInclusions.slice(2);
        var owa = {
          selectedModel: {
            modelType: {
              type: 'network'
            }
          },
          selectedAnalysis: {
            interventionInclusions: [{
              interventionId: 1
            }, {
              interventionId: 2
            }]
          }
        };
        var result = metaBenefitRiskService.findMissingAlternatives(interventionInclusions, owa);
        expect(result).toEqual(expectedResult);
      });
    });

    describe('addScales', function() {
      it('should add the scales to the effects table', function() {
        var owas = [{
          outcome: {
            name: 'hamd'
          }
        }, {
          outcome: {
            name: 'headache'
          }
        }];
        var alternatives = [{
          name: 'fluox'
        }, {
          name: 'sertra'
        }];
        var scaleResults = {
          hamd: {
            fluox: {
              result: {
                '2.5%': 1,
                '50%': 2,
                '97.5%': 3
              }
            },
            sertra: {
              result: {
                '2.5%': 4,
                '50%': 5,
                '97.5%': 6
              }
            }
          },
          headache: {
            fluox: {
              result: {
                '2.5%': 7,
                '50%': 8,
                '97.5%': 9
              }
            },
            sertra: {
              result: {
                '2.5%': 10,
                '50%': 11,
                '97.5%': 12
              }
            }
          }
        };

        var expected = [{
          'outcome': {
            'name': 'hamd'
          },
          'scales': {
            'fluox': {
              'result': {
                '2.5%': 1,
                '50%': 2,
                '97.5%': 3
              }
            },
            'sertra': {
              'result': {
                '2.5%': 4,
                '50%': 5,
                '97.5%': 6
              }
            }
          }
        }, {
          'outcome': {
            'name': 'headache'
          },
          'scales': {
            'fluox': {
              'result': {
                '2.5%': 7,
                '50%': 8,
                '97.5%': 9
              }
            },
            'sertra': {
              'result': {
                '2.5%': 10,
                '50%': 11,
                '97.5%': 12
              }
            }
          }
        }];

        var result = metaBenefitRiskService.addScales(owas, alternatives, scaleResults);
        expect(result.length).toBe(owas.length);
        expect(result[0].scales).not.toBeNull();
        expect(result).toEqual(expected);
      });
    });
  });
});

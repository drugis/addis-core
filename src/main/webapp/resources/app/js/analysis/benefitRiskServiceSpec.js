'use strict';
define(['angular-mocks', './analysis'], function() {
  describe('benefit-risk service', function() {
    var benefitRiskService;

    var workspaceServiceMock = jasmine.createSpyObj('WorkspaceService', ['reduceProblem']);
    var problemResourceMock = jasmine.createSpyObj('ProblemResource', ['get']);
    var analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['save']);
    var scope;

    beforeEach(angular.mock.module('addis.analysis', function($provide) {
      $provide.value('$state', {
        params: { id: 'params'}
      });
      $provide.constant('DEFAULT_VIEW', 'foo');
      $provide.value('WorkspaceService', workspaceServiceMock);
      $provide.value('ProblemResource', problemResourceMock);
      $provide.value('AnalysisResource', analysisResourceMock);
    }));

    beforeEach(inject(function($rootScope, $q, BenefitRiskService) {
      scope = $rootScope;
      problemResourceMock.get.and.returnValue({
        $promise: $q.resolve({ problemId: 3 })
      });
      benefitRiskService = BenefitRiskService;
    }));

    describe('addStudiesToOutcomes', function() {
      it('should set the selected study on the outcomesWithAnalyses if possible', function() {
        var outcomesWithAnalyses = [{
          outcome: {
            id: 1
          }
        }, {
          outcome: {
            id: 2
          }
        }];
        var studyInclusions = [{
          studyGraphUri: 'http://study1.uri',
          outcomeId: 1
        }, {
          outcomeId: 2
        }];
        var studies = [{
          studyUri: 'http://study1.uri'
        }];
        var result = benefitRiskService.addStudiesToOutcomes(outcomesWithAnalyses, studyInclusions, studies);
        var expectedResult = [{
          outcome: {
            id: 1
          },
          selectedStudy: {
            studyUri: 'http://study1.uri'
          },
          dataType: 'single-study'
        }, {
          outcome: {
            id: 2
          },
          selectedStudy: {},
          dataType: 'single-study'
        }];
        expect(result).toEqual(expectedResult);
      });

    });

    describe('buildOutcomeWithAnalyses', function() {
      it('should build inclusions when the outcome is included, and ignore excluded outcomes', function() {
        var outcome1 = {
          id: 1
        };
        var analysis = {
          id: 7,
          benefitRiskNMAOutcomeInclusions: [{
            outcomeId: 1,
            networkMetaAnalysisId: 3,
            modelId: 1
          }]
        };
        var models = [{
          id: 1
        }, {
          id: 2
        }];
        var networkMetaAnalyses = [{
          id: 3,
          outcome: outcome1,
          models: models
        }, {
          id: 5,
          outcome: outcome1,
          models: models
        }];
        var expectedResult = {
          outcome: outcome1,
          dataType: 'network',
          networkMetaAnalyses: [{
            id: 3,
            outcome: {
              id: 1
            },
            models: models
          }, {
            id: 5,
            outcome: {
              id: 1
            },
            models: models
          }],
          selectedAnalysis: {
            id: 3,
            outcome: {
              id: 1
            },
            models: models
          },
          selectedModel: models[0]
        };

        var resultIncluded = benefitRiskService.buildOutcomeWithAnalyses(analysis, networkMetaAnalyses, outcome1);
        expect(resultIncluded).toEqual(expectedResult);
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
        var result = benefitRiskService.joinModelsWithAnalysis(models, analysis);
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
        expect(benefitRiskService.compareAnalysesByModels(a, b)).toBe(-1);
        expect(benefitRiskService.compareAnalysesByModels(b, a)).toBe(1);
      });
      it('should do nothing if both analyses have models', function() {
        var a = {
          models: [1]
        };
        var b = {
          models: [1]
        };
        expect(benefitRiskService.compareAnalysesByModels(a, b)).toBe(0);
        expect(benefitRiskService.compareAnalysesByModels(b, a)).toBe(0);
      });
      it('should do nothing if neither analysis has a model', function() {
        var a = {
          models: []
        };
        var b = {
          models: []
        };
        expect(benefitRiskService.compareAnalysesByModels(a, b)).toBe(0);
        expect(benefitRiskService.compareAnalysesByModels(b, a)).toBe(0);
      });
    });

    describe('addModelsGroup', function() {
      it('should decorate the models with their group', function() {

        var result = benefitRiskService.addModelsGroup({
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
        expect(benefitRiskService.numberOfSelectedOutcomes(outcomesWithAnalyses)).toBe(2);
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
        expect(benefitRiskService.isMissingAnalysis(noIncludedOutcome)).toBeFalsy();
        var includedWithAnalysis = [{
          dataType: 'network',
          outcome: {
            isIncluded: true
          },
          selectedAnalysis: {
            id: 1
          }
        }];
        expect(benefitRiskService.isMissingAnalysis(includedWithAnalysis)).toBeFalsy();
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
        expect(benefitRiskService.isMissingAnalysis(includedWithoutAnalysis)).toBeTruthy();
      });
    });

    describe('isMissingDataType', function() {
      it('should return true if there is an outcome for which the data source type has not yet been chosen', function() {
        var includedButNoType = [{
          outcome: {
            isIncluded: true
          }
        }];
        expect(benefitRiskService.isMissingDataType(includedButNoType)).toBeTruthy();
        var notIncluded = [{
          outcome: {
            isIncluded: false
          }
        }];
        expect(benefitRiskService.isMissingDataType(notIncluded)).toBeFalsy();
        var includedWithType = [{
          dataType: 'network',
          outcome: {
            isIncluded: true
          }
        }];
        expect(benefitRiskService.isMissingDataType(includedWithType)).toBeFalsy();
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
        expect(benefitRiskService.isModelWithMissingAlternatives(outcomesWithAnalyses)).toBeTruthy();
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
        expect(benefitRiskService.isModelWithMissingAlternatives(outcomesWithAnalyses)).toBeFalsy();
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
        expect(benefitRiskService.isModelWithoutResults(outcomesWithAnalyses)).toBeTruthy();
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
        expect(benefitRiskService.isModelWithoutResults(outcomesWithAnalyses)).toBeFalsy();
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
        var result = benefitRiskService.findMissingAlternatives(interventionInclusions, owa);
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
        var result = benefitRiskService.findMissingAlternatives(interventionInclusions, owa);
        expect(result).toEqual(expectedResult);
      });
    });

    describe('addScales', function() {
      it('should add the scales to the effects table', function() {
        var owas = [{
          outcome: {
            name: 'hamd',
            semanticOutcomeUri: 'http://outcomes/hamd'
          }
        }, {
          outcome: {
            name: 'headache',
            semanticOutcomeUri: 'http://outcomes/headache'

          }
        }];
        var alternatives = [{
          name: 'fluox',
          id: 1
        }, {
          name: 'sertra',
          id: 2
        }];
        var criteria = {
          'http://outcomes/hamd': {
             dataSources: [{
               id: 'hamdDataSource'
             }]
          },
          'http://outcomes/headache': {
             dataSources: [{
               id: 'headacheDataSource'
             }]
          }
        };
        var scaleResults = {
          hamdDataSource: {
            1: {
              result: {
                '2.5%': 1,
                '50%': 2,
                '97.5%': 3
              }
            },
            2: {
              result: {
                '2.5%': 4,
                '50%': 5,
                '97.5%': 6
              }
            }
          },
          headacheDataSource: {
            1: {
              result: {
                '2.5%': 7,
                '50%': 8,
                '97.5%': 9
              }
            },
            2: {
              result: {
                '2.5%': 10,
                '50%': 11,
                '97.5%': 12
              }
            }
          }
        };

        var expected = [{
          outcome: {
            name: 'hamd',
            semanticOutcomeUri: 'http://outcomes/hamd'
          },
          scales: {
            '1': {
              result: {
                '2.5%': 1,
                '50%': 2,
                '97.5%': 3
              }
            },
            '2': {
              result: {
                '2.5%': 4,
                '50%': 5,
                '97.5%': 6
              }
            }
          }
        }, {
          outcome: {
            name: 'headache',
            semanticOutcomeUri: 'http://outcomes/headache'
          },
          scales: {
            '1': {
              result: {
                '2.5%': 7,
                '50%': 8,
                '97.5%': 9
              }
            },
            '2': {
              result: {
                '2.5%': 10,
                '50%': 11,
                '97.5%': 12
              }
            }
          }
        }];

        var result = benefitRiskService.addScales(owas, alternatives, criteria, scaleResults);
        expect(result.length).toBe(owas.length);
        expect(result[0].scales).not.toBeNull();
        expect(result).toEqual(expected);
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
        expect(benefitRiskService.isInvalidStudySelected(outcomeInclusions)).toBeFalsy();
      });
      it('should be true if there are missing interventions', function() {
        var outcomeInclusions = [{
          dataType: 'single-study',
          selectedStudy: {
            missingInterventions: [{}],
            missingOutcomes: []
          }
        }];
        expect(benefitRiskService.isInvalidStudySelected(outcomeInclusions)).toBeTruthy();
      });
      it('should be true if there are missing outcomes', function() {
        var outcomeInclusions = [{
          dataType: 'single-study',
          selectedStudy: {
            missingInterventions: [],
            missingOutcomes: [{}]
          }
        }];
        expect(benefitRiskService.isInvalidStudySelected(outcomeInclusions)).toBeTruthy();
      });
    });

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
        expect(benefitRiskService.hasMissingStudy(outcomeInclusions)).toBeFalsy();
      });
      it('should be true if a study is not selected', function() {
        var networkOnly = [{
          dataType: 'network'
        }];
        expect(benefitRiskService.hasMissingStudy(networkOnly)).toBeFalsy();

        var withGoodSelection = [{
          dataType: 'single-study',
          selectedStudy: {
            studyUri: 'http://cool.ninja'
          }
        }];
        expect(benefitRiskService.hasMissingStudy(withGoodSelection)).toBeFalsy();

        var withMissingStudy1 = [{
          dataType: 'single-study',
          selectedStudy: {}
        }];
        expect(benefitRiskService.hasMissingStudy(withMissingStudy1)).toBeTruthy();
        var withMissingStudy2 = [{
          dataType: 'single-study',
        }];
        expect(benefitRiskService.hasMissingStudy(withMissingStudy2)).toBeTruthy();
      });
    });

    describe('findOverlappingInterventions', function() {
      it('should build a list of uniqueue overlapping interventions', function() {
        var studies = [{
          overlappingInterventions: []
        }, {
          overlappingInterventions: [{
            id: 1
          }, {
            id: 2
          }]
        }, {
          overlappingInterventions: [{
            id: 2
          }, {
            id: 3
          }]
        }];
        var expectedInterventions = [{
          id: 1
        }, {
          id: 2
        }, {
          id: 3
        }];
        var result = benefitRiskService.findOverlappingInterventions(studies);
        expect(result).toEqual(expectedInterventions);
      });
    });

    describe('findOverlappingOutcomes', function() {
      it('should return a list of outcomes which share concepts with an other', function() {
        var outcomeInclusions = [{
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
        var result = benefitRiskService.findOverlappingOutcomes(outcomeInclusions);
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

    describe('addModelBaseline', function() {
      it('should add the baseline to the models if possible', function() {
        var analysis = {
          benefitRiskNMAOutcomeInclusions: [{
            modelId: 'modelId1'
          }, {
            modelId: 'modelId2',
            baseline: 'somebaseline'
          }, {
            modelId: 'modelId3'
          }],
          interventionInclusions: [{
            interventionId: 'interventionId1'
          }, {
            interventionId: 'interventionId2'
          }]
        };
        var models = [{
            id: 'modelId1',
            baseline: {
              baseline: {
                name: 'interventionName1'
              }
            }
          }, {
            id: 'modelId2',
            baseline: {
              baseline: {
                baseline: {
                  name: 'interventionName1'
                }
              }
            }
          },
          {
            id: 'modelId3',
            baseline: {
              baseline: {
                name: 'interventionName2'
              }
            }
          }
        ];

        var alternatives = [{
          id: 'interventionId1',
          name: 'interventionName1'
        }, {
          id: 'interventionId2',
          name: 'interventionName2'
        }];
        var result = benefitRiskService.addModelBaseline(analysis, models, alternatives);
        var expectedResult = {
          benefitRiskNMAOutcomeInclusions: [{
            modelId: 'modelId1',
            baseline: {
              name: 'interventionName1'
            }
          }, {
            modelId: 'modelId2',
            baseline: 'somebaseline'
          }, {
            modelId: 'modelId3',
            baseline: {
              name: 'interventionName2'
            }
          }],
          interventionInclusions: [{
            interventionId: 'interventionId1'
          }, {
            interventionId: 'interventionId2'
          }]
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('finalizeAndGoToDefaultScenario', function() {
      var analysis = {
        id: -3,
        projectId: -30
      };
      var mockProblem = { id: 'reducedProblem' };
      beforeEach(function() {
        workspaceServiceMock.reduceProblem.and.returnValue(mockProblem);
        benefitRiskService.finalizeAndGoToDefaultScenario(analysis)
        scope.$apply();
      });
      it('should get the problem and save the analysis', function() {
        var saveCommand = angular.copy(analysis);
        saveCommand.analysis = analysis;
        saveCommand.scenarioState = JSON.stringify({problem: mockProblem}, null, 2);
        expect(problemResourceMock.get).toHaveBeenCalledWith({id: 'params'});
        expect(analysisResourceMock.save).toHaveBeenCalledWith(saveCommand, jasmine.any(Function));
      });
    });
  });
});

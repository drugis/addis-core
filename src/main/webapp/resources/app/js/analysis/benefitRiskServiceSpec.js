'use strict';
define(['lodash', 'angular-mocks', './analysis'], function(_) {
  describe('benefit-risk service', function() {
    var benefitRiskService;

    var workspaceServiceMock = jasmine.createSpyObj('WorkspaceService', ['reduceProblem']);
    var problemResourceMock = jasmine.createSpyObj('ProblemResource', ['get']);
    var analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['save', 'query']);
    var subProblemResourceMock = jasmine.createSpyObj('SubProblemResource', ['query']);
    var scenarioResourceMock = jasmine.createSpyObj('ScenarioResource', ['query']);
    var benefitRiskErrorServiceMock = jasmine.createSpyObj('BenefitRiskErrorService', [
      'hasMissingStudy',
      'isMissingDataType',
      'isMissingAnalysis',
      'isModelWithMissingAlternatives',
      'isModelWithoutResults',
      'isInvalidStudySelected',
      'numberOfSelectedOutcomes',
      'findOverlappingOutcomes'
    ]);

    var scope;
    var q;
    var state = {
      params: {
        projectId: 37,
        id: 'params'
      },
      go: function() {
        return;
      }
    };

    beforeEach(angular.mock.module('addis.analysis', function($provide) {
      $provide.value('$state', state);
      $provide.constant('DEFAULT_VIEW', 'foo');
      $provide.value('WorkspaceService', workspaceServiceMock);
      $provide.value('ProblemResource', problemResourceMock);
      $provide.value('AnalysisResource', analysisResourceMock);
      $provide.value('SubProblemResource', subProblemResourceMock);
      $provide.value('ScenarioResource', scenarioResourceMock);
      $provide.value('BenefitRiskErrorService', benefitRiskErrorServiceMock);
    }));

    beforeEach(inject(function($rootScope, $q, BenefitRiskService) {
      q = $q;
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
          dataType: 'single-study',
          isContrastOutcome: false
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
        var outcomesWithAnalyses = [{
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
          id: 1,
          isIncluded: true
        }, {
          name: 'sertra',
          id: 2,
          isIncluded: true
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

        var result = benefitRiskService.addScales(outcomesWithAnalyses, alternatives, criteria, scaleResults);
        expect(result.length).toBe(outcomesWithAnalyses.length);
        expect(result[0].scales).not.toBeNull();
        expect(result).toEqual(expected);
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

    describe('addBaseline', function() {
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
        var result = benefitRiskService.addBaseline(analysis, models, alternatives);
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
        projectId: 37
      };
      var mockProblem = { id: 'reducedProblem' };
      beforeEach(function() {
        workspaceServiceMock.reduceProblem.and.returnValue(mockProblem);
        benefitRiskService.finalizeAndGoToDefaultScenario(analysis);
        scope.$apply();
      });
      it('should get the problem and save the analysis', function() {
        var saveCommand = angular.copy(analysis);
        saveCommand.analysis = analysis;
        saveCommand.scenarioState = JSON.stringify({ problem: mockProblem }, null, 2);
        expect(problemResourceMock.get).toHaveBeenCalledWith({
          id: 'params',
          projectId: 37
        });
        expect(analysisResourceMock.save).toHaveBeenCalledWith(saveCommand, jasmine.any(Function));
      });
    });

    describe('analysisUpdateCommand', function() {
      it('should turn the analysis and selected interventions into a command for the server', function() {
        var analysis = {
          id: 1,
          projectId: 2
        };
        var includedAlternatives = [{ id: 3 }];
        var result = benefitRiskService.analysisUpdateCommand(analysis, includedAlternatives);
        var expectedResult = {
          id: 1,
          projectId: 2,
          analysis: {
            id: 1,
            projectId: 2,
            interventionInclusions: [{
              interventionId: 3,
              analysisId: 1
            }]
          }
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('goToDefaultScenario', function() {
      var scenarioPromise;
      var subProblemPromise;
      beforeEach(function() {
        var subProblemDefer = q.defer();
        subProblemPromise = subProblemDefer.promise;
        subProblemDefer.resolve([{ id: 1 }]);
        subProblemResourceMock.query.and.returnValue({ $promise: subProblemPromise });

        var scenarioDefer = q.defer();
        scenarioPromise = scenarioDefer.promise;
        scenarioDefer.resolve([{ id: 2 }]);
        scenarioResourceMock.query.and.returnValue({ $promise: scenarioPromise });

        spyOn(state, 'go');

        scope.$digest();
      });

      it('should query then subproblem for the analysis and navigate to the analysis', function(done) {
        benefitRiskService.goToDefaultScenario().then(function() {
          expect(subProblemResourceMock.query).toHaveBeenCalledWith(state.params);
          expect(scenarioResourceMock.query).toHaveBeenCalledWith(
            _.extend({}, state.params, { problemId: 1 })
          );
          expect(state.go).toHaveBeenCalledWith('foo',
            _.extend({}, state.params, {
              problemId: 1,
              id: 2
            })
          );
          done();
        });
        scope.$digest();
      });
    });

    describe('analysisToSaveCommand', function() {
      it('should turn the analysis into a save command', function() {
        var analysis = {
          id: 1,
          projectId: 2,
          some: 'thing'
        };
        var problem = {
          id: 3
        };
        var result = benefitRiskService.analysisToSaveCommand(analysis, problem);
        var expectedResult = {
          id: 1,
          projectId: 2,
          analysis: analysis,
          scenarioState: '{\n  "id": 3\n}'
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildOutcomes', function() {
      it('should return a list of all outcomes with an nma and together with all outcomes with a single study', function() {
        var networkMetaAnalyses = {};
        var outcomes = [
          { id: 'studyOutcomeId' },
          { id: 'notIncludedStudyOutcome' }
        ];
        var analysis = {
          benefitRiskNMAOutcomeInclusions: [],
          benefitRiskStudyOutcomeInclusions: [{ outcomeId: 'studyOutcomeId' }]
        };
        var result = benefitRiskService.buildOutcomes(analysis, outcomes, networkMetaAnalyses);
        var expectedResult = [{
          outcome: {
            id: 'studyOutcomeId'
          },
          dataType: 'single-study',
          selectedStudy: {}
        }];
        expect(result).toEqual(expectedResult);
      });
    });

    describe('isContrastStudySelected', function() {
      it('should return true if atleast one study with contrast data for the outcome is selected', function() {
        var inclusions = [{ studyGraphUri: 'graphUri' }];
        var studies = [{
          studyUri: 'graphUri',
          arms: [{
            referenceArm: {}
          }]
        }];
        var result = benefitRiskService.isContrastStudySelected(inclusions, studies);
        expect(result).toBeTruthy();
      });

      it('should return false if there is no study with contrast data', function() {
        var inclusions = [{ studyGraphUri: 'graphUri' }];
        var studies = [{
          studyUri: 'graphUri',
          arms: [{
          }]
        }];
        var result = benefitRiskService.isContrastStudySelected(inclusions, studies);
        expect(result).toBeFalsy();
      });

    });

    describe('getOutcomesWithInclusions', function() {
      it('return the outcomes with their inclusion and isIncluded set', function() {
        var outcomes = [{
          id: 1
        }, {
          id: 2
        }];
        var analysis = {
          benefitRiskNMAOutcomeInclusions: [{ outcomeId: 1 }],
          benefitRiskStudyOutcomeInclusions: [{ outcomeId: 2 }]
        };
        var result = benefitRiskService.getOutcomesWithInclusions(outcomes, analysis);
        var expectedResult = [
          {
            id: 1,
            isIncluded: true
          },
          {
            id: 2,
            isIncluded: true
          }
        ];
        expect(result).toEqual(expectedResult);
      });
    });

    describe('getStep1Errors', function() {
      var outcomes = [
        { outcome: { isIncluded: true } },
        { outcome: { isIncluded: true } }
      ];
      beforeEach(function() {
        benefitRiskErrorServiceMock.findOverlappingOutcomes.and.returnValue([]);
        benefitRiskService.getStep1Errors(outcomes);
      });

      it('a list of all errors occuring in a list with outcomes', function() {
        expect(benefitRiskErrorServiceMock.isInvalidStudySelected).toHaveBeenCalled();
        expect(benefitRiskErrorServiceMock.numberOfSelectedOutcomes).toHaveBeenCalled();
        expect(benefitRiskErrorServiceMock.isMissingAnalysis).toHaveBeenCalled();
        expect(benefitRiskErrorServiceMock.isMissingDataType).toHaveBeenCalled();
        expect(benefitRiskErrorServiceMock.isModelWithMissingAlternatives).toHaveBeenCalled();
        expect(benefitRiskErrorServiceMock.isModelWithoutResults).toHaveBeenCalled();
        expect(benefitRiskErrorServiceMock.hasMissingStudy).toHaveBeenCalled();
        expect(benefitRiskErrorServiceMock.findOverlappingOutcomes).toHaveBeenCalled();
      });
    });

    describe('prepareEffectsTable', function() {
      beforeEach(function() {
        analysisResourceMock.query.and.returnValue({ $promise: {} });
      });
      it('should query then analysis', function() {
        var outcomes = [{
          id: 1,
          isIncluded: true
        }, {
          id: 2,
          isIncluded: true
        }, {
          id: 3,
          isIncluded: false
        }];
        benefitRiskService.prepareEffectsTable(outcomes);
        var expectedCall = {
          projectId: 37,
          outcomeIds: [1, 2]
        };
        expect(analysisResourceMock.query).toHaveBeenCalledWith(expectedCall);
      });
    });

    describe('getStudyOutcomeInclusions', function() {
      it('should return all studyoutcome inclusions', function() {
        var outcomes = [{
          outcome: {
            isIncluded: true,
            id: 1
          },
          dataType: 'single-study',
          selectedStudy: {
            studyUri: 'someStudyUri'
          }
        }, {
          outcome: {
            isIncluded: true,
            id: 2
          },
          dataType: 'network'
        }];
        var analysisId = 1337;
        var result = benefitRiskService.getStudyOutcomeInclusions(outcomes, analysisId);
        var expectedResult = [{
          analysisId: analysisId,
          outcomeId: 1,
          studyGraphUri: 'someStudyUri'
        }];
        expect(result).toEqual(expectedResult);
      });
    });

    describe('getNMAOutcomeInclusions', function() {
      it('should return all nmaoutcome inclusions', function() {
        var outcomes = [{
          outcome: {
            isIncluded: true,
            id: 1
          },
          dataType: 'single-study'
        }, {
          outcome: {
            isIncluded: true,
            id: 2
          },
          dataType: 'network',
          selectedAnalysis: {
            id: 3
          },
          selectedModel: {
            id: 4
          }
        }];
        var analysisId = 1337;
        var result = benefitRiskService.getNMAOutcomeInclusions(outcomes, analysisId);
        var expectedResult = [{
          analysisId: analysisId,
          outcomeId: 2,
          networkMetaAnalysisId:3,
          modelId: 4
        }];
        expect(result).toEqual(expectedResult);
      });
    });
  });
});

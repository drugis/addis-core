'use strict';
define(['lodash', 'angular-mocks', './analysis'], function(_) {
  describe('benefit-risk service', function() {
    var benefitRiskService;

    var workspaceServiceMock = jasmine.createSpyObj('WorkspaceService', ['reduceProblem']);
    var problemResourceMock = jasmine.createSpyObj('ProblemResource', ['get']);
    var analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['save', 'query']);
    var subProblemResourceMock = jasmine.createSpyObj('SubProblemResource', ['query']);
    var scenarioResourceMock = jasmine.createSpyObj('ScenarioResource', ['query']);

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

    describe('getOutcomesWithInclusions', function() {
      it('return the outcomes with their inclusion and isIncluded set', function() {
        var outcomes = [{
          id: 1
        }, {
          id: 2
        }];
        var inclusions = [{ outcomeId: 2 },{ outcomeId: 1 }];
        var result = benefitRiskService.getOutcomesWithInclusions(outcomes, inclusions);
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

    describe('hasMissingBaseline', function() {
      describe('should return true', function() {
        it('if a network outcome has a missing baseline', function() {
          var outcomes = [{
            dataType: 'network',
            baseline: undefined
          }];
          var result = benefitRiskService.hasMissingBaseline(outcomes);
          expect(result).toBeTruthy();
        });

        it('if a single-study contrast outcome has a missing basline', function() {
          var outcomes = [{
            dataType: 'single-study',
            isContrastOutcome: true,
            baseline: undefined
          }];
          var result = benefitRiskService.hasMissingBaseline(outcomes);
          expect(result).toBeTruthy();
        });
      });

      it('should return false if there is no missing outcome', function() {
        var outcomes = [{
          dataType: 'network',
          baseline: 'baseline'
        }, {
          dataType: 'single-study',
          isContrastOutcome: true,
          baseline: 'baseline'
        }, {
          dataType: 'single-study',
        }];
        var result = benefitRiskService.hasMissingBaseline(outcomes);
        expect(result).toBeFalsy();
      });
    });
  });
});

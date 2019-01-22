'use strict';
define(['lodash', 'angular-mocks', './analysis'], function(_) {
  describe('benefit-risk step1 service', function() {
    var benefitRiskStep1Service;

    var benefitRiskServiceMock = jasmine.createSpyObj('BenefitRiskService', [
      'addStudiesToOutcomes',
      'buildOutcomeWithAnalyses',
      'filterArchivedAndAddModels',
      'findMissingAlternatives'
    ]);
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

    beforeEach(angular.mock.module('addis.analysis', function($provide) {
      $provide.value('BenefitRiskService', benefitRiskServiceMock);
      $provide.value('BenefitRiskErrorService', benefitRiskErrorServiceMock);
    }));

    beforeEach(inject(function(BenefitRiskStep1Service) {
      benefitRiskStep1Service = BenefitRiskStep1Service;
    }));

    describe('analysisUpdateCommand', function() {
      it('should turn the analysis and selected interventions into a command for the server', function() {
        var analysis = {
          id: 1,
          projectId: 2
        };
        var includedAlternatives = [{ id: 3 }];
        var result = benefitRiskStep1Service.analysisUpdateCommand(analysis, includedAlternatives);
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

    describe('buildOutcomesWithAnalyses', function() {
      it('fails', function() { 
        fail();
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
        expect(benefitRiskStep1Service.compareAnalysesByModels(a, b)).toBe(-1);
        expect(benefitRiskStep1Service.compareAnalysesByModels(b, a)).toBe(1);
      });
      it('should do nothing if both analyses have models', function() {
        var a = {
          models: [1]
        };
        var b = {
          models: [1]
        };
        expect(benefitRiskStep1Service.compareAnalysesByModels(a, b)).toBe(0);
        expect(benefitRiskStep1Service.compareAnalysesByModels(b, a)).toBe(0);
      });
      it('should do nothing if neither analysis has a model', function() {
        var a = {
          models: []
        };
        var b = {
          models: []
        };
        expect(benefitRiskStep1Service.compareAnalysesByModels(a, b)).toBe(0);
        expect(benefitRiskStep1Service.compareAnalysesByModels(b, a)).toBe(0);
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
        var result = benefitRiskStep1Service.findMissingAlternatives(interventionInclusions, owa);
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
        var result = benefitRiskStep1Service.findMissingAlternatives(interventionInclusions, owa);
        expect(result).toEqual(expectedResult);
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
        var result = benefitRiskStep1Service.findOverlappingInterventions(studies);
        expect(result).toEqual(expectedInterventions);
      });
    });

    describe('getModelSelection', function() {
      it('should return the primary model of the selected analysis if it is set', function() {
        var selectedAnalysis = {
          models: [{
            id: 2
          }, {
            id: 1
          }],
          primaryModel: 1
        };
        var result = benefitRiskStep1Service.getModelSelection(selectedAnalysis);
        expect(result).toEqual({ id: 1 });
      });

      it('should return the first model in the list if no primary model is set', function() {
        var selectedAnalysis = {
          models: [{
            id: 2
          }, {
            id: 1
          }]
        };
        var result = benefitRiskStep1Service.getModelSelection(selectedAnalysis);
        expect(result).toEqual({ id: 2 });
      });

      it('should return undefined if no nma is selected', function() {
        var selectedAnalysis;
        var result = benefitRiskStep1Service.getModelSelection(selectedAnalysis);
        expect(result).toBe(undefined);
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
        var result = benefitRiskStep1Service.getNMAOutcomeInclusions(outcomes, analysisId);
        var expectedResult = [{
          analysisId: analysisId,
          outcomeId: 2,
          networkMetaAnalysisId: 3,
          modelId: 4
        }];
        expect(result).toEqual(expectedResult);
      });
    });

    describe('getReferenceAlternativeName', function() {
      it('should return the name of the reference alternative', function() {
        var outcome = {
          selectedStudy: {
            arms: [{
              referenceArm: 'referenceUri',
              uri: 'nonReferenceUri'
            }, {
              referenceArm: 'referenceUri',
              uri: 'referenceUri',
              matchedProjectInterventionIds: [1]
            }]
          }
        };
        var alternatives = [{
          id: 1,
          name: 'referenceAlternativeName'
        }, {
          id: 2
        }];
        var result = benefitRiskStep1Service.getReferenceAlternativeName(outcome, alternatives);
        var expectedResult = 'referenceAlternativeName';
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
        benefitRiskStep1Service.getStep1Errors(outcomes);
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
        var result = benefitRiskStep1Service.getStudyOutcomeInclusions(outcomes, analysisId);
        var expectedResult = [{
          analysisId: analysisId,
          outcomeId: 1,
          studyGraphUri: 'someStudyUri'
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
        var result = benefitRiskStep1Service.isContrastStudySelected(inclusions, studies);
        expect(result).toBeTruthy();
      });

      it('should return false if there is no study with contrast data', function() {
        var inclusions = [{ studyGraphUri: 'graphUri' }];
        var studies = [{
          studyUri: 'graphUri',
          arms: [{
          }]
        }];
        var result = benefitRiskStep1Service.isContrastStudySelected(inclusions, studies);
        expect(result).toBeFalsy();
      });

    });

    describe('updateOutcomeInclusion', function() {
      it('should set selected model, study, analysis, and data type to undefined if the outcome is not included', function() {
        var inclusion = {
          outcome: { isIncluded: false }
        };
        var result = benefitRiskStep1Service.updateOutcomeInclusion(inclusion);
        var expectedResult = {
          selectedAnalysis: undefined,
          selectedStudy: undefined,
          selectedModel: undefined,
          dataType: undefined,
          outcome: { isIncluded: false }
        };
        expect(result).toEqual(expectedResult);
      });

      it('should set selected analysis and model to undefined and study to an empty object', function() {
        var inclusion = {
          outcome: { isIncluded: true },
          dataType: 'single-study'
        };
        var result = benefitRiskStep1Service.updateOutcomeInclusion(inclusion);
        var expectedResult = {
          selectedAnalysis: undefined,
          selectedStudy: {},
          selectedModel: undefined,
          outcome: { isIncluded: true },
          dataType: 'single-study'
        };
        expect(result).toEqual(expectedResult);
      });

      it('should set the selected study to undefined for a network outcome without an analysis without a model', function() {
        var inclusion = {
          outcome: { isIncluded: true },
          dataType: 'network',
          networkMetaAnalyses: [{
            models: []
          }]
        };
        var result = benefitRiskStep1Service.updateOutcomeInclusion(inclusion);
        var expectedResult = {
          selectedAnalysis: undefined,
          selectedStudy: undefined,
          selectedModel: undefined,
          outcome: { isIncluded: true },
          dataType: 'network',
          networkMetaAnalyses: [{
            models: []
          }]
        };
        expect(result).toEqual(expectedResult);
      });

      it('should set the selected study to undefined, set the selected analysis to the analysis and no model for a network outcome an analysis with an invalid model. It should also update the missing alternatives on the model.', function() {
        var model = { id: 1 };
        var inclusion = {
          outcome: { isIncluded: true },
          dataType: 'network',
          networkMetaAnalyses: [{
            models: [model]
          }]
        };
        var alternatives = [];
        var result = benefitRiskStep1Service.updateOutcomeInclusion(inclusion, alternatives);
        var expectedResult = {
          selectedAnalysis: {
            models: [model]
          },
          selectedStudy: undefined,
          selectedModel: {
            id: 1,
            missingAlternatives: [],
            missingAlternativesNames: []
          },
          outcome: { isIncluded: true },
          dataType: 'network',
          networkMetaAnalyses: [{
            models: [model]
          }]
        };
        expect(result).toEqual(expectedResult);
      });
    });
  });
});

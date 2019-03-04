'use strict';
define(['lodash', 'angular-mocks', './analysis'], function(_) {
  describe('benefit-risk step 2 service', function() {
    var benefitRiskStep2Service;
    var analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['query']);
    var benefitRiskServiceMock = jasmine.createSpyObj('BenefitRiskService', ['addModels']);
    var stateParams = {
      projectId: 37,
      id: 'params'
    };

    beforeEach(angular.mock.module('addis.analysis', function($provide) {
      $provide.value('$stateParams', stateParams);
      $provide.value('AnalysisResource', analysisResourceMock);
      $provide.value('BenefitRiskService', benefitRiskServiceMock);
    }));

    beforeEach(inject(function(BenefitRiskStep2Service) {
      benefitRiskStep2Service = BenefitRiskStep2Service;
    }));

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
        var result = benefitRiskStep2Service.addBaseline(analysis, models, alternatives);
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

    describe('addBaselineToInclusions', function() {
      it('should add the provided baseline to the correct inclusion', function() {
        var inclusions = [{
          outcomeId: 1
        }, {
          outcomeId: 2
        }];
        var baseline = 'baseline';
        var outcome = {
          outcome: {
            id: 1
          }
        };
        var result = benefitRiskStep2Service.addBaselineToInclusion(outcome, inclusions, baseline);
        var expectedResult = [{
          outcomeId: 1,
          baseline: 'baseline'
        }, {
          outcomeId: 2
        }];
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

        var result = benefitRiskStep2Service.addScales(outcomesWithAnalyses, alternatives, criteria, scaleResults);
        expect(result.length).toBe(outcomesWithAnalyses.length);
        expect(result[0].scales).not.toBeNull();
        expect(result).toEqual(expected);
      });
    });

    describe('filterArchivedAndAddModels', function() {
      it('should filter all archived analyses, and ask the benefitrisk service to add models', function() {
        var analyses = [{
          archived: true
        }, {
          archived: false,
          id: 1
        }];
        var models = [];
        benefitRiskStep2Service.filterArchivedAndAddModels(analyses, models);
        expect(benefitRiskServiceMock.addModels).toHaveBeenCalledWith([analyses[1]], models);
      });
    });

    describe('getMeasurementType', function() {
      var outcome;

      beforeEach(function() {
        outcome = {
          outcome: {
            semanticOutcomeUri: 'varcon'
          },
          selectedStudy: {
            defaultMeasurementMoment: 'defaultMomentUri',
            arms: [{
              uri: 'nonReferenceArmUri',
              measurements: {
                defaultMomentUri: [{
                  variableConceptUri: 'varcon'
                }]
              }
            }]
          }
        };
      });

      it('should return the type for an outcome measured in odds ratio', function() {
        outcome.selectedStudy.arms[0].measurements.defaultMomentUri[0].oddsRatio = 1;
        var result = benefitRiskStep2Service.getMeasurementType(outcome);
        expect(result).toBe('oddsRatio');
      });

      it('should return the type for an outcome measured in risk ratio', function() {
        outcome.selectedStudy.arms[0].measurements.defaultMomentUri[0].riskRatio = 1;
        var result = benefitRiskStep2Service.getMeasurementType(outcome);
        expect(result).toBe('riskRatio');
      });

      it('should return the type for an outcome measured in mean difference', function() {
        outcome.selectedStudy.arms[0].measurements.defaultMomentUri[0].meanDifference = 1;
        var result = benefitRiskStep2Service.getMeasurementType(outcome);
        expect(result).toBe('meanDifference');
      });

      it('should return the type for an outcome measured in hazard ratio', function() {
        outcome.selectedStudy.arms[0].measurements.defaultMomentUri[0].hazardRatio = 1;
        var result = benefitRiskStep2Service.getMeasurementType(outcome);
        expect(result).toBe('hazardRatio');
      });

      it('should return the type for an outcome measured in standardized mean difference', function() {
        outcome.selectedStudy.arms[0].measurements.defaultMomentUri[0].standardizedMeanDifference = 1;
        var result = benefitRiskStep2Service.getMeasurementType(outcome);
        expect(result).toBe('standardizedMeanDifference');
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
        benefitRiskStep2Service.prepareEffectsTable(outcomes);
        var expectedCall = {
          projectId: 37,
          outcomeIds: [1, 2]
        };
        expect(analysisResourceMock.query).toHaveBeenCalledWith(expectedCall);
      });
    });

    describe('getReferenceAlternativeName', function() {
      it('should return the name of the reference alternative', function() {
        var outcome = {
          outcome: {
            semanticOutcomeUri: 'varcon'
          },
          selectedStudy: {
            defaultMeasurementMoment: 'defmom',
            arms: [{
              measurements: {
                defmom: [{
                  referenceArm: 'referenceUri',
                  variableConceptUri: 'varcon'
                }]
              },
              uri: 'nonReferenceUri'
            }, {
              uri: 'referenceUri',
              matchedProjectInterventionIds: [1],
              measurements: undefined
            }]
          }
        };
        var alternatives = [{
          id: 1,
          name: 'referenceAlternativeName'
        }, {
          id: 2
        }];
        var result = benefitRiskStep2Service.getReferenceAlternativeName(outcome, alternatives);
        var expectedResult = 'referenceAlternativeName';
        expect(result).toEqual(expectedResult);
      });
    });
  });
});

'use strict';
define(['lodash', 'angular-mocks', './results'], function(_) {
  describe('the resultsTableService', function() {

    var INTEGER_TYPE = '<http://www.w3.org/2001/XMLSchema#integer>';
    var DOUBLE_TYPE = '<http://www.w3.org/2001/XMLSchema#double>';
    var CONTRAST = 'ontology:contrast_data';

    var resultsTableService,
      armService = jasmine.createSpyObj('ArmService', ['queryItems']),
      measurementMomentService = jasmine.createSpyObj('MeasurementMomentService', ['queryItems']);
    beforeEach(angular.mock.module('trialverse.util'));

    beforeEach(function() {
      angular.mock.module('trialverse.arm', function($provide) {
        $provide.value('ArmService', armService);

      });
      angular.mock.module('trialverse.measurementMoment', function($provide) {
        $provide.value('MeasurementMomentService', measurementMomentService);
      });
    });

    beforeEach(inject(function(ResultsTableService) {
      resultsTableService = ResultsTableService;
    }));

    describe('createHeaders', function() {
      describe('for arm level', function() {
        describe('for continuous data', function() {
          it('should return a list containting the mean, standard deviation, and sample size result properties', function() {
            var testType = {
              resultProperties: [
                'http://trials.drugis.org/ontology#mean',
                'http://trials.drugis.org/ontology#sample_size',
                'http://trials.drugis.org/ontology#standard_deviation'
              ]
            };
            var expectedResult = [{
              label: 'mean',
              lexiconKey: 'mean',
              analysisReady: true
            }, {
              label: 'standard deviation',
              lexiconKey: 'standard-deviation',
              analysisReady: true
            }, {
              label: 'N',
              lexiconKey: 'sample-size',
              analysisReady: true
            }];
            expect(resultsTableService.createHeaders(testType)).toEqual(expectedResult);
          });
        });

        describe('for dichotomous data', function() {
          it('should return a list containting the count and sample size result properties', function() {
            var testType = {
              resultProperties: [
                'http://trials.drugis.org/ontology#count',
                'http://trials.drugis.org/ontology#sample_size'
              ]
            };
            var expectedResult = [{
              label: 'subjects with event',
              lexiconKey: 'count',
              analysisReady: true
            }, {
              label: 'N',
              lexiconKey: 'sample-size',
              analysisReady: true
            }];
            expect(resultsTableService.createHeaders(testType)).toEqual(expectedResult);
          });
        });

        describe('for categorical data', function() {
          it('should return a list of the categories', function() {
            var testType = {
              categoryList: [
                'Male',
                'Female'
              ]
            };
            expect(resultsTableService.createHeaders(testType)).toEqual(['Male', 'Female']);
          });
        });

        describe('for survival data ', function() {
          it('should return a list containting the hazard ratio and exposure result properties', function() {
            var testType = {
              resultProperties: [
                'http://trials.drugis.org/ontology#hazard_ratio',
                'http://trials.drugis.org/ontology#exposure'
              ],
              timeScale: 'P1M'
            };
            var expectedResult = [{
              label: 'hazard ratio',
              lexiconKey: 'hazard-ratio',
              analysisReady: false
            }, {
              label: 'total observation time (months)',
              lexiconKey: 'exposure',
              analysisReady: true
            }];
            expect(resultsTableService.createHeaders(testType)).toEqual(expectedResult);
          });
        });
      });

      describe('for contrast', function() {
        describe('for non-log', function() {
          describe('continuous data', function() {
            it('should return a list containting odds ratio and standard error the result properties', function() {
              var testType = {
                resultProperties: [
                  'http://trials.drugis.org/ontology#odds_ratio',
                  'http://trials.drugis.org/ontology#standard_error'
                ],
                armOrContrast: CONTRAST
              };
              var expectedResult = [{
                label: 'odds ratio',
                lexiconKey: 'odds-ratio',
                analysisReady: true
              }, {
                label: 'standard error',
                lexiconKey: 'standard-error',
                analysisReady: true
              }];
              expect(resultsTableService.createHeaders(testType)).toEqual(expectedResult);
            });
          });

          describe('dichotomous data', function() {
            it('should return a list containting the standardized mean difference and confidence interval width result properties', function() {
              var testType = {
                resultProperties: [
                  'http://trials.drugis.org/ontology#standardized_mean_difference',
                  'http://trials.drugis.org/ontology#confidence_interval_width'
                ],
                armOrContrast: CONTRAST,
                confidenceIntervalWidth: 70
              };
              var expectedResult = [{
                label: 'standardized mean difference',
                lexiconKey: 'standardized-mean-difference',
                analysisReady: true
              }, {
                label: '70% confidence interval lower bound',
                lexiconKey: 'confidence-interval',
                analysisReady: true
              }, {
                label: '70% confidence interval upper bound',
                lexiconKey: 'confidence-interval',
                analysisReady: true
              }];
              expect(resultsTableService.createHeaders(testType)).toEqual(expectedResult);
            });
          });

          describe(' survival data ', function() {
            it('should return a list containting the hazard ratio and standard error result properties', function() {
              var testType = {
                resultProperties: [
                  'http://trials.drugis.org/ontology#hazard_ratio',
                  'http://trials.drugis.org/ontology#standard_error'
                ],
                armOrContrast: CONTRAST
              };
              var expectedResult = [{
                label: 'hazard ratio',
                lexiconKey: 'hazard-ratio',
                analysisReady: true
              }, {
                label: 'standard error',
                lexiconKey: 'standard-error',
                analysisReady: true
              }];
              expect(resultsTableService.createHeaders(testType)).toEqual(expectedResult);
            });
          });
        });
        describe('for log', function() {
          describe('continuous data', function() {
            it('should return a list containting odds ratio and standard error the result properties', function() {
              var testType = {
                resultProperties: [
                  'http://trials.drugis.org/ontology#odds_ratio',
                  'http://trials.drugis.org/ontology#standard_error'
                ],
                armOrContrast: CONTRAST,
                isLog: true
              };
              var expectedResult = [{
                label: 'log odds ratio',
                lexiconKey: 'odds-ratio',
                analysisReady: true
              }, {
                label: 'standard error',
                lexiconKey: 'standard-error',
                analysisReady: true
              }];
              expect(resultsTableService.createHeaders(testType)).toEqual(expectedResult);
            });
          });

          describe(' survival data ', function() {
            it('should return a list containting the hazard ratio and standard error result properties', function() {
              var testType = {
                resultProperties: [
                  'http://trials.drugis.org/ontology#hazard_ratio',
                  'http://trials.drugis.org/ontology#standard_error'
                ],
                armOrContrast: CONTRAST,
                isLog: true
              };
              var expectedResult = [{
                label: 'log hazard ratio',
                lexiconKey: 'hazard-ratio',
                analysisReady: true
              }, {
                label: 'standard error',
                lexiconKey: 'standard-error',
                analysisReady: true
              }];
              expect(resultsTableService.createHeaders(testType)).toEqual(expectedResult);
            });
          });
        });
      });
    });

    describe('createInputRows for arm level data', function() {
      var resultRows, variable, arms, measurementMoments;

      beforeEach(function() {
        variable = {
          measuredAtMoments: [{
            uri: 'uri 1'
          }, {
            uri: 'uri 3'
          }],
          measurementType: {},
          resultProperties: [
            'http://trials.drugis.org/ontology#mean',
            'http://trials.drugis.org/ontology#sample_size',
            'http://trials.drugis.org/ontology#standard_deviation'
          ]
        };
        arms = [{
          label: 'xyz arm 1',
          armURI: 'http://arms/arm1'
        }, {
          label: 'Overall population',
          groupUri: 'http://groups/overall'
        }, {
          label: 'arm 3 20 mg',
          armURI: 'http://arms/arm3'
        }, {
          label: 'aab arm 2',
          armURI: 'http://arms/arm2'
        }];
        measurementMoments = [{
          uri: 'uri 1',
          label: 'moment aa'
        }, {
          uri: 'uri 2',
          label: 'moment ab'
        }, {
          uri: 'uri 3',
          label: 'moment ac'
        }];
        resultRows = resultsTableService.createInputRows(variable, arms, [], measurementMoments);
      });

      it('overal population should be last, always', function() {
        expect(resultRows[3].group.label).toEqual(arms[1].label);
      });

      it('should set the number of arms', function() {
        expect(resultRows[0].numberOfGroups).toEqual(4);
      });

      it('should return one row for each combination of arm and measurement moment at which the variable is measured', function() {
        expect(resultRows.length).toEqual(8);
      });

      it('should apply some sort of sane sorting to the arms', function() {
        expect(resultRows[0].group.label).toEqual(arms[3].label);
        expect(resultRows[1].group.label).toEqual(arms[2].label);
        expect(resultRows[2].group.label).toEqual(arms[0].label);
        expect(resultRows[3].group.label).toEqual(arms[1].label);
        expect(resultRows[4].group.label).toEqual(arms[3].label);
        expect(resultRows[5].group.label).toEqual(arms[2].label);
      });

      it('should place the appropriate measurement moment and arm on each row', function() {
        expect(resultRows[0].measurementMoment).toEqual(measurementMoments[0]);
        expect(resultRows[0].group).toEqual(arms[3]);
        expect(resultRows[1].measurementMoment).toEqual(measurementMoments[0]);
        expect(resultRows[1].group).toEqual(arms[2]);
        expect(resultRows[2].measurementMoment).toEqual(measurementMoments[0]);
        expect(resultRows[2].group).toEqual(arms[0]);
        expect(resultRows[3].measurementMoment).toEqual(measurementMoments[0]);
        expect(resultRows[3].group).toEqual(arms[1]);
        expect(resultRows[4].measurementMoment).toEqual(measurementMoments[2]);
        expect(resultRows[4].group).toEqual(arms[3]);
        expect(resultRows[5].measurementMoment).toEqual(measurementMoments[2]);
        expect(resultRows[5].group).toEqual(arms[2]);
      });

      describe('for a continuous type', function() {
        beforeEach(function() {
          var results = [{
            armUri: arms[0].armURI,
            instance: 'http://instance/1',
            momentUri: measurementMoments[0].uri,
            result_property: 'mean',
            value: '2'
          }];
          variable.measurementType = resultsTableService.CONTINUOUS_TYPE;
          resultRows = resultsTableService.createInputRows(variable, arms, [], measurementMoments, results);
        });

        it('should create input columns', function() {
          expect(resultRows[2].inputColumns).toBeDefined();
          expect(resultRows[2].inputColumns.length).toBe(3);
          expect(resultRows[2].inputColumns[0].valueName).toEqual('mean');
          expect(resultRows[2].inputColumns[0].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[2].inputColumns[0].value).toEqual(2);
          expect(resultRows[2].inputColumns[0].isInValidValue).toEqual(false);
          expect(resultRows[2].inputColumns[1].valueName).toEqual('standard deviation');
          expect(resultRows[2].inputColumns[1].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[2].inputColumns[0].isInValidValue).toEqual(false);
          expect(resultRows[2].inputColumns[2].valueName).toEqual('N');
          expect(resultRows[2].inputColumns[2].dataType).toEqual(INTEGER_TYPE);
          expect(resultRows[2].inputColumns[0].isInValidValue).toEqual(false);
        });
      });

      describe('for a dichotomous type', function() {
        beforeEach(function() {
          var results = [{
            armUri: arms[0].armURI,
            instance: 'http://instance/2',
            momentUri: measurementMoments[0].uri,
            result_property: 'count',
            value: '66'
          }];
          variable.measurementType = resultsTableService.DICHOTOMOUS_TYPE;
          variable.resultProperties = [
            'http://trials.drugis.org/ontology#count',
            'http://trials.drugis.org/ontology#sample_size'
          ];
          variable.armOrContrast = 'ontology:arm_level_data';
          resultRows = resultsTableService.createInputRows(variable, arms, [], measurementMoments, results);
        });

        it('should create input columns', function() {
          expect(resultRows[2].inputColumns).toBeDefined();
          expect(resultRows[2].inputColumns.length).toBe(2);
          expect(resultRows[2].inputColumns[0].valueName).toEqual('subjects with event');
          expect(resultRows[2].inputColumns[0].dataType).toEqual(INTEGER_TYPE);
          expect(resultRows[2].inputColumns[0].isInValidValue).toEqual(false);
          expect(resultRows[2].inputColumns[0].value).toEqual(66);
          expect(resultRows[2].inputColumns[1].valueName).toEqual('N');
          expect(resultRows[2].inputColumns[1].dataType).toEqual(INTEGER_TYPE);
          expect(resultRows[2].inputColumns[1].isInValidValue).toEqual(false);
        });
      });

      describe('for a categorical type', function() {
        beforeEach(function() {
          delete variable.resultProperties;
        });

        it('should return input columns for current style categories', function() {
          var valueObjects = [{
            momentUri: measurementMoments[0].uri,
            result_property: {
              category: 'id1'
            },
            value: 5
          }];
          variable.categoryList = [{
            '@id': 'id1',
            label: 'cat1'
          }];
          var result = resultsTableService.createInputRows(variable, arms, [], measurementMoments, valueObjects);
          expect(result.length).toBe(8);
          expect(result[3].group.label).toEqual('Overall population');
          expect(result[3].numberOfGroups).toBe(4);
          expect(result[3].inputColumns[0].value).toBe(5);
          expect(result[3].inputColumns[0].dataType).toEqual(INTEGER_TYPE);
          expect(result[3].inputColumns[0].isCategory).toBeTruthy();
          expect(result[3].inputColumns[0].isInValidValue).toBeFalsy();
        });

        it('should return input columns for legacy style categories', function() {
          var valueObjects = [{
            momentUri: measurementMoments[0].uri,
            result_property: 'cat1',
            value: 5
          }];
          variable.categoryList = ['cat1'];
          var result = resultsTableService.createInputRows(variable, arms, [], measurementMoments, valueObjects);
          expect(result.length).toBe(8);
          expect(result[3].group.label).toEqual('Overall population');
          expect(result[3].numberOfGroups).toBe(4);
          expect(result[3].inputColumns[0].value).toBe(5);
          expect(result[3].inputColumns[0].dataType).toEqual(INTEGER_TYPE);
          expect(result[3].inputColumns[0].isCategory).toBeTruthy();
          expect(result[3].inputColumns[0].isInValidValue).toBeFalsy();
        });
      });

      describe('for a survival type', function() {
        beforeEach(function() {
          var results = [{
            armUri: arms[0].armURI,
            instance: 'http://instance/2',
            momentUri: measurementMoments[0].uri,
            result_property: 'exposure',
            value: '66'
          }];
          variable.measurementType = resultsTableService.DICHOTOMOUS_TYPE;
          variable.resultProperties = [
            'http://trials.drugis.org/ontology#exposure'
          ];
          variable.armOrContrast = 'ontology:arm_level_data';
          resultRows = resultsTableService.createInputRows(variable, arms, [], measurementMoments, results);
        });

        it('should create input columns', function() {
          expect(resultRows[2].inputColumns).toBeDefined();
          expect(resultRows[2].inputColumns.length).toBe(1);
          expect(resultRows[2].inputColumns[0].valueName).toEqual('total observation time');
          expect(resultRows[2].inputColumns[0].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[2].inputColumns[0].isInValidValue).toEqual(false);
          expect(resultRows[2].inputColumns[0].value).toEqual(66);
        });
      });
    });

    describe('createInputRows for contrast data', function() {
      var resultRows, variable, arms, groups, measurementMoments;

      beforeEach(function() {
        variable = {
          measuredAtMoments: [{
            uri: 'uri 1'
          }, {
            uri: 'uri 3'
          }],
          measurementType: {},
          resultProperties: [
            'http://trials.drugis.org/ontology#continuous_mean_difference',
            'http://trials.drugis.org/ontology#standard_error',
            'http://trials.drugis.org/ontology#confidence_interval_width'
          ],
          armOrContrast: CONTRAST,
          confidenceIntervalWidth: 90,
          referenceArm: 'http://arms/arm1'
        };
        arms = [{
          label: 'reference arm 1',
          armURI: 'http://arms/arm1'
        }, {
          label: 'arm 2 20 mg',
          armURI: 'http://arms/arm2'
        }, {
          label: 'aab arm 3',
          armURI: 'http://arms/arm3'
        }];
        measurementMoments = [{
          uri: 'uri 1',
          label: 'moment 1'
        }, {
          uri: 'uri 2',
          label: 'moment 2'
        }, {
          uri: 'uri 3',
          label: 'moment 3'
        }];
        groups = [{
          label: 'xyz group 1',
          groupUri: 'http://groups/group'
        }];
        resultRows = resultsTableService.createInputRows(variable, arms, groups, measurementMoments);
      });

      it('should not add reference arm', function() {
        expect(_.some(resultRows, function(row) {
          return row.group.label === arms[0].label;
        })).toBeFalsy();
      });

      it('should set the number of arms, not including the reference arm and groups', function() {
        expect(resultRows[0].numberOfGroups).toEqual(2);
      });

      it('shoul, for each combination of non-reference arm and measurement moment at which the variable is measured return one row, except for the confidence interval which should return 2', function() {
        expect(resultRows.length).toEqual(4);
      });

      it('should apply some sort of sane sorting to the arms', function() {
        expect(resultRows[0].group.label).toEqual(arms[2].label);
        expect(resultRows[1].group.label).toEqual(arms[1].label);
        expect(resultRows[2].group.label).toEqual(arms[2].label);
        expect(resultRows[3].group.label).toEqual(arms[1].label);
      });

      it('should place the appropriate measurement moment and arm on each row', function() {
        expect(resultRows[0].measurementMoment).toEqual(measurementMoments[0]);
        expect(resultRows[0].group).toEqual(arms[2]);
        expect(resultRows[1].measurementMoment).toEqual(measurementMoments[0]);
        expect(resultRows[1].group).toEqual(arms[1]);
        expect(resultRows[2].measurementMoment).toEqual(measurementMoments[2]);
        expect(resultRows[2].group).toEqual(arms[2]);
        expect(resultRows[3].measurementMoment).toEqual(measurementMoments[2]);
        expect(resultRows[3].group).toEqual(arms[1]);
      });

      describe('for a continuous type', function() {
        beforeEach(function() {
          var results = [{
            armUri: arms[2].armURI,
            instance: 'http://instance/1',
            momentUri: measurementMoments[2].uri,
            result_property: 'continuous_mean_difference',
            value: '2'
          }];
          variable.measurementType = resultsTableService.CONTINUOUS_TYPE;
          resultRows = resultsTableService.createInputRows(variable, arms, groups, measurementMoments, results);
        });

        it('should create input columns', function() {
          expect(resultRows[2].inputColumns).toBeDefined();
          expect(resultRows[2].inputColumns.length).toBe(4);
          expect(resultRows[2].inputColumns[0].valueName).toEqual('mean difference');
          expect(resultRows[2].inputColumns[0].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[2].inputColumns[0].value).toEqual(2);
          expect(resultRows[2].inputColumns[0].isInValidValue).toEqual(false);
          expect(resultRows[2].inputColumns[1].valueName).toEqual('standard error');
          expect(resultRows[2].inputColumns[1].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[2].inputColumns[0].isInValidValue).toEqual(false);
          expect(resultRows[2].inputColumns[2].valueName).toEqual('confidence interval lower bound');
          expect(resultRows[2].inputColumns[2].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[2].inputColumns[3].valueName).toEqual('confidence interval upper bound');
          expect(resultRows[2].inputColumns[3].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[2].inputColumns[0].isInValidValue).toEqual(false);
        });
      });

      describe('for a dichotomous type', function() {
        beforeEach(function() {
          var results = [{
            armUri: arms[2].armURI,
            instance: 'http://instance/2',
            momentUri: measurementMoments[2].uri,
            result_property: 'odds_ratio',
            value: '66'
          }];
          variable.measurementType = resultsTableService.DICHOTOMOUS_TYPE;
          variable.resultProperties = [
            'http://trials.drugis.org/ontology#odds_ratio',
            'http://trials.drugis.org/ontology#standard_error'
          ];
          variable.armOrContrast = CONTRAST;
          resultRows = resultsTableService.createInputRows(variable, arms, groups, measurementMoments, results);
        });

        it('should create input columns', function() {
          expect(resultRows[2].inputColumns).toBeDefined();
          expect(resultRows[2].inputColumns.length).toBe(2);
          expect(resultRows[2].inputColumns[0].valueName).toEqual('odds ratio');
          expect(resultRows[2].inputColumns[0].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[2].inputColumns[0].isInValidValue).toEqual(false);
          expect(resultRows[2].inputColumns[0].value).toEqual(66);
          expect(resultRows[2].inputColumns[1].valueName).toEqual('standard error');
          expect(resultRows[2].inputColumns[1].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[2].inputColumns[1].isInValidValue).toEqual(false);
        });
      });

      describe('for a survival type', function() {
        beforeEach(function() {
          var results = [{
            armUri: arms[2].armURI,
            instance: 'http://instance/2',
            momentUri: measurementMoments[2].uri,
            result_property: 'hazard_ratio',
            value: '66'
          }];
          variable.measurementType = resultsTableService.DICHOTOMOUS_TYPE;
          variable.resultProperties = [
            'http://trials.drugis.org/ontology#hazard_ratio'
          ];
          variable.armOrContrast = CONTRAST;
          resultRows = resultsTableService.createInputRows(variable, arms, groups, measurementMoments, results);
          it('should create input columns', function() {
            expect(resultRows[2].inputColumns).toBeDefined();
            expect(resultRows[2].inputColumns.length).toBe(2);
            expect(resultRows[2].inputColumns[0].valueName).toEqual('odds ratio');
            expect(resultRows[2].inputColumns[0].dataType).toEqual(DOUBLE_TYPE);
            expect(resultRows[2].inputColumns[0].isInValidValue).toEqual(false);
            expect(resultRows[2].inputColumns[0].value).toEqual(66);
          });
        });
      });
    });

    describe('isValidValue', function() {
      it('should return true for null', function() {
        var column = {
          value: null
        };
        expect(resultsTableService.isValidValue(column)).toBe(true);
      });
      it('should return false for undefined', function() {
        var column = {
          value: undefined
        };
        expect(resultsTableService.isValidValue(column)).toBe(false);
      });
      it('should return true for a float value in a float column', function() {
        var column1 = {
          value: 3.2,
          dataType: DOUBLE_TYPE
        };
        var column2 = {
          value: 3,
          dataType: DOUBLE_TYPE
        };
        expect(resultsTableService.isValidValue(column1)).toBe(true);
        expect(resultsTableService.isValidValue(column2)).toBe(true);
      });
      it('should return false for a float value in an integer column', function() {
        var column = {
          value: 3.2,
          dataType: INTEGER_TYPE
        };
        expect(resultsTableService.isValidValue(column)).toBe(false);
      });
      it('should return false for a non-numeric value', function() {
        var column1 = {
          value: 'test',
          dataType: INTEGER_TYPE
        };
        var column2 = {
          value: 'test',
          dataType: DOUBLE_TYPE
        };
        var column3 = {
          value: '1.23abc',
          dataType: DOUBLE_TYPE
        };
        expect(resultsTableService.isValidValue(column1)).toBe(false);
        expect(resultsTableService.isValidValue(column2)).toBe(false);
        expect(resultsTableService.isValidValue(column3)).toBe(false);
      });
    });

    describe('buildMeasurementMomentOptions', function() {
      it('should make a mm -> options map with each mm except itself as values, plus always an "unassign" option', function() {
        var mm1 = {
          label: 'xyz measurement moment',
          uri: 'http://trials.org/instances/1'
        };
        var mm2 = {
          label: 'def measurement moment',
          uri: 'http://trials.org/instances/2'
        };
        var mm3 = {
          label: 'abc measurement moment 1',
          uri: 'http://trials.org/instances/3'
        };
        var measurementMoments = [mm1, mm2, mm3];
        var unassign = {
          label: 'Unassign'
        };
        var expectedResult = {};
        expectedResult[mm1.uri] = [mm3, mm2, unassign];
        expectedResult[mm2.uri] = [mm3, mm1, unassign];
        expectedResult[mm3.uri] = [mm2, mm1, unassign];

        var result = resultsTableService.buildMeasurementMomentOptions(measurementMoments);

        expect(result).toEqual(expectedResult);
      });
    });

    describe('findMeasurementOverlap', function() {
      var targetMMUri = 'targetMMUri';
      var measurementMoment1Uri = 'measurementMoment1Uri';
      var momentSelections = {
        targetMMUri: {
          uri: targetMMUri
        },
        measurementMoment1Uri: {
          uri: measurementMoment1Uri
        }
      };
      var mm1 = {
        uri: measurementMoment1Uri
      };
      var mm2 = {
        uri: targetMMUri
      };

      it('should find where there is already data at a certain measurement moment', function() {
        var inputRows = [{
          measurementMoment: mm1,
          inputColumns: [{
            value: 3
          }]
        }, {
          measurementMoment: mm2,
          inputColumns: [{
            value: 3
          }]
        }];
        var result = resultsTableService.findMeasurementOverlap(momentSelections, inputRows);
        var expectedResult = {
          targetMMUri: true,
          measurementMoment1Uri: true
        };
        expect(result).toEqual(expectedResult);
      });

      it('should return false if there is no data at a certain measurement moment', function() {
        var inputRows = [{
          measurementMoment: mm1,
          inputColumns: [{
            value: 3
          }]
        }, {
          measurementMoment: mm2,
          inputColumns: [{
            value: undefined
          }]
        }];
        var result = resultsTableService.findMeasurementOverlap(momentSelections, inputRows);
        var expectedResult = {
          targetMMUri: false,
          measurementMoment1Uri: true
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('findNotAnalysedProperty', function() {
      it('should find an input header which is not analysis ready if there is one', function() {
        var inputHeaders = [{
          lexiconKey: 'some-key',
          analysisReady: true
        }, {
          lexiconKey: 'another-key',
          analysisReady: false
        }, {
          lexiconKey: 'yet-another-key',
          analysisReady: true
        }];
        var result = resultsTableService.findNotAnalysedProperty(inputHeaders);
        expect(result).toBeTruthy();
      });

      it('should return falsy if all headers are analysis ready', function() {
        var inputHeaders = [{
          lexiconKey: 'some-key',
          analysisReady: true
        }, {
          lexiconKey: 'another-key',
          analysisReady: true
        }];
        var result = resultsTableService.findNotAnalysedProperty(inputHeaders);
        expect(result).toBeFalsy();
      });
    });

    describe('buildMeasurementMomentSelections', function() {
      it('should return the possible measurement moment selections', function() {
        var inputRows = [{
          measurementMoment: {
            uri: 'moment1uri'
          }
        }, {
          measurementMoment: {
            uri: 'moment2uri'
          }
        }];
        var measurementMomentOptions = {
          moment1uri: [{
            some: 'property'
          }],
          moment2uri: [{
            another: 'property'
          }]
        };
        var result = resultsTableService.buildMeasurementMomentSelections(inputRows, measurementMomentOptions);
        var expectedResult = {
          moment1uri: {
            some: 'property'
          },
          moment2uri: {
            another: 'property'
          }
        };
        expect(result).toEqual(expectedResult);
      });
    });
  });
});

'use strict';
define(['angular-mocks', './results'], function() {
  describe('the results table service', function() {

    var INTEGER_TYPE = '<http://www.w3.org/2001/XMLSchema#integer>';
    var DOUBLE_TYPE = '<http://www.w3.org/2001/XMLSchema#double>';

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
      it('should return a list containting objects which contain "Mean, ± sd and N " labels, their respective lexicon key and wether they are implemented for use during analysis when the type is continuous', function() {
        var testType = {
          resultProperties: [
            'http://trials.drugis.org/ontology#mean',
            'http://trials.drugis.org/ontology#standard_deviation',
            'http://trials.drugis.org/ontology#sample_size'
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
      it('should return a list containting objects which contain "subjects with event and N" labels, their respective lexicon key and wether they are implemented for use during analysiswhen the type is dichotomous', function() {
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
      it('should return a list of the categories for a categorical variable', function() {
        var testType = {
          categoryList: [
            'Male',
            'Female'
          ]
        };
        expect(resultsTableService.createHeaders(testType)).toEqual(['Male', 'Female']);
      });
    });

    describe('createInputRows', function() {
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
            'http://trials.drugis.org/ontology#standard_deviation',
            'http://trials.drugis.org/ontology#sample_size'
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

    describe('findOverlappingMeasurements', function() {
      it('should find where there is already data at a certain measurement moment', function() {
        var targetMMUri = 'targetMMUri';
        var mm1 = {
          uri: 'measurementMoment1Uri'
        };
        var mm2 = {
          uri: targetMMUri
        };
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
        var result = resultsTableService.findOverlappingMeasurements(targetMMUri, inputRows);
        expect(result).toBeTruthy();
      });
    });
    it('should return false if there is no data at a certain measurement moment', function() {
      var targetMMUri = 'targetMMUri';
      var mm1 = {
        uri: 'measurementMoment1Uri'
      };
      var targetMM = {
        uri: targetMMUri
      };
      var inputRows = [{
        measurementMoment: mm1,
        inputColumns: [{
          value: 3
        }]
      }, {
        measurementMoment: targetMM,
        inputColumns: [{
          value: undefined
        }]
      }];
      var result = resultsTableService.findOverlappingMeasurements(targetMMUri, inputRows);
      expect(result).toBeFalsy();
    });

  });
});

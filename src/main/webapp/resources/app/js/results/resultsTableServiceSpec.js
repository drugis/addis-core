'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the results table service', function() {

    var INTEGER_TYPE = '<http://www.w3.org/2001/XMLSchema#integer>';
    var DOUBLE_TYPE = '<http://www.w3.org/2001/XMLSchema#double>';

    var resultsTableService,
      armService = jasmine.createSpyObj('ArmService', ['queryItems']),
      measurementMomentService = jasmine.createSpyObj('MeasurementMomentService', ['queryItems']);
    beforeEach(module('trialverse.util'));

    beforeEach(function() {
      module('trialverse.arm', function($provide) {
        $provide.value('ArmService', armService);

      });
      module('trialverse.measurementMoment', function($provide) {
        $provide.value('MeasurementMomentService', measurementMomentService);
      });
    });

    beforeEach(inject(function(ResultsTableService) {
      resultsTableService = ResultsTableService;
    }));

    describe('buildHeaders', function() {
      it('should return a list containting "Mean, ± sd and N " when the type is continuous', function() {
        var testType = resultsTableService.CONTINUOUS_TYPE;
        expect(resultsTableService.createHeaders(testType)).toEqual(['Mean', '± sd', 'N']);
      });
      it('should return a list containting "Count and N" when the type is dichotomous', function() {
        var testType = resultsTableService.DICHOTOMOUS_TYPE;
        expect(resultsTableService.createHeaders(testType)).toEqual(['Count', 'N']);
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
          measurementType: {}
        };
        arms = [{
          label: 'arm 1',
          armURI: 'http://arms/arm1'
        }, {
          label: 'arm 2',
          armURI: 'http://arms/arm2'
        }, {
          label: 'arm 3',
          armURI: 'http://arms/arm3'
        }];
        measurementMoments = [{
          uri: 'uri 1',
        }, {
          uri: 'uri 2'
        }, {
          uri: 'uri 3'
        }];

        resultRows = resultsTableService.createInputRows(variable, arms, measurementMoments);
      });

      it('should set the number of arms', function() {
        expect(resultRows[0].numberOfArms).toEqual(3);
      });
      it('should return one row for each combination of arm and measurement moment at which the variable is measured', function() {
        expect(resultRows.length).toEqual(6);
      });
      it('should place the appropriate measurement moment and arm on each row', function() {
        expect(resultRows[0].measurementMoment).toEqual(measurementMoments[0]);
        expect(resultRows[0].arm).toEqual(arms[0]);
        expect(resultRows[1].measurementMoment).toEqual(measurementMoments[0]);
        expect(resultRows[1].arm).toEqual(arms[1]);
        expect(resultRows[2].measurementMoment).toEqual(measurementMoments[0]);
        expect(resultRows[2].arm).toEqual(arms[2]);
        expect(resultRows[3].measurementMoment).toEqual(measurementMoments[2]);
        expect(resultRows[3].arm).toEqual(arms[0]);
        expect(resultRows[4].measurementMoment).toEqual(measurementMoments[2]);
        expect(resultRows[4].arm).toEqual(arms[1]);
        expect(resultRows[5].measurementMoment).toEqual(measurementMoments[2]);
        expect(resultRows[5].arm).toEqual(arms[2]);
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
          resultRows = resultsTableService.createInputRows(variable, arms, measurementMoments, results);
        });

        it('should create input columns', function() {
          expect(resultRows[0].inputColumns).toBeDefined();
          expect(resultRows[0].inputColumns.length).toBe(3);
          expect(resultRows[0].inputColumns[0].valueName).toEqual('mean');
          expect(resultRows[0].inputColumns[0].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[0].inputColumns[0].value).toEqual(2);
          expect(resultRows[0].inputColumns[0].isInValidValue).toEqual(false);
          expect(resultRows[0].inputColumns[1].valueName).toEqual('standard_deviation');
          expect(resultRows[0].inputColumns[1].dataType).toEqual(DOUBLE_TYPE);
          expect(resultRows[0].inputColumns[0].isInValidValue).toEqual(false);
          expect(resultRows[0].inputColumns[2].valueName).toEqual('sample_size');
          expect(resultRows[0].inputColumns[2].dataType).toEqual(INTEGER_TYPE);
          expect(resultRows[0].inputColumns[0].isInValidValue).toEqual(false);
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
          resultRows = resultsTableService.createInputRows(variable, arms, measurementMoments, results);
        });

        it('should create input columns', function() {
          expect(resultRows[0].inputColumns).toBeDefined();
          expect(resultRows[0].inputColumns.length).toBe(2);
          expect(resultRows[0].inputColumns[0].valueName).toEqual('count');
          expect(resultRows[0].inputColumns[0].dataType).toEqual(INTEGER_TYPE);
          expect(resultRows[0].inputColumns[0].isInValidValue).toEqual(false);
          expect(resultRows[0].inputColumns[0].value).toEqual(66);
          expect(resultRows[0].inputColumns[1].valueName).toEqual('sample_size');
          expect(resultRows[0].inputColumns[1].dataType).toEqual(INTEGER_TYPE);
          expect(resultRows[0].inputColumns[1].isInValidValue).toEqual(false);
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
  });
});

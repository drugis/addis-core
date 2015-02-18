'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the results table service', function() {

    var resultsTableService,
      armService = jasmine.createSpyObj('ArmService', ['queryItems']),
      measurementMomentService = jasmine.createSpyObj('MeasurementMomentService', ['queryItems']);
    beforeEach(module('trialverse.util'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        $provide.value('ArmService', armService);
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
        arms = ['arm 1', 'arm 2', 'arm 3'];
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
          variable.measurementType = resultsTableService.CONTINUOUS_TYPE;
          resultRows = resultsTableService.createInputRows(variable, arms, measurementMoments);
        });

        it('should create input columns', function() {
          expect(resultRows[0].inputColumns).toBeDefined();
          expect(resultRows[0].inputColumns.length).toBe(3);
          expect(resultRows[0].inputColumns[0].valueName).toEqual('mean');
          expect(resultRows[0].inputColumns[1].valueName).toEqual('standard_deviation');
          expect(resultRows[0].inputColumns[2].valueName).toEqual('sample_size');
        });
      });

      describe('for a dichotomous type', function() {
        beforeEach(function() {
          variable.measurementType = resultsTableService.DICHOTOMOUS_TYPE;
          resultRows = resultsTableService.createInputRows(variable, arms, measurementMoments);
        });

        it('should create input columns', function() {
          expect(resultRows[0].inputColumns).toBeDefined();
          expect(resultRows[0].inputColumns.length).toBe(2);
          expect(resultRows[0].inputColumns[0].valueName).toEqual('count');
          expect(resultRows[0].inputColumns[1].valueName).toEqual('sample_size');
        });
      });

    });

  });
});

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

    describe('createInputRows', function() {
      var result, variable, arms, measurementMoments;

      beforeEach(function() {
        variable = {
          measuredAtMoments: ['uri 1', 'uri 3']
        };
        arms = ['arm 1', 'arm 2', 'arm 3'];
        measurementMoments = [{
          uri: 'uri 1',
        }, {
          uri: 'uri 2'
        }, {
          uri: 'uri 3'
        }];

        result = resultsTableService.createInputRows(variable, arms, measurementMoments);
      });
      it('should set the number of arms', function() {
        expect(result[0].nArms).toEqual(3);
      });
      it('should return one row for each combination of arm and measurement moment at which the variable is measured', function() {
        expect(result.length).toEqual(6);
      });
      it('should place the appropriate measurement moment and arm on each row', function() {
        expect(result[0].measurementMoment).toEqual(measurementMoments[0]);
        expect(result[0].arm).toEqual(arms[0]);
        expect(result[1].measurementMoment).toEqual(measurementMoments[0]);
        expect(result[1].arm).toEqual(arms[1]);
        expect(result[2].measurementMoment).toEqual(measurementMoments[0]);
        expect(result[2].arm).toEqual(arms[2]);
        expect(result[3].measurementMoment).toEqual(measurementMoments[2]);
        expect(result[3].arm).toEqual(arms[0]);
        expect(result[4].measurementMoment).toEqual(measurementMoments[2]);
        expect(result[4].arm).toEqual(arms[1]);
        expect(result[5].measurementMoment).toEqual(measurementMoments[2]);
        expect(result[5].arm).toEqual(arms[2]);
      });
      it('should create properties for the inputs to bind to', function() {
        expect(result[0].mean).toBeDefined();
        expect(result[0].sd).toBeDefined();
        expect(result[0].n).toBeDefined();
      });
    });

  });
});

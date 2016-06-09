'use strict';
define(['angular-mocks'], function() {
  describe('the repair service', function() {
    var repairService;
    var byGroupSourceResults = [{
      groupUri: 'groupUri1',
      instance: 'resultInstance1',
      momentUri: 'momentUri1',
      outcomeUri: 'outcomeUri1',
      result_property: 'sample_size',
      value: 12
    }, {
      groupUri: 'groupUri1',
      instance: 'resultInstance2',
      momentUri: 'momentUri1',
      outcomeUri: 'outcomeUri1',
      result_property: 'count',
      value: 20
    }, {
      groupUri: 'groupUri1',
      instance: 'resultInstance3',
      momentUri: 'momentUri2',
      outcomeUri: 'outcomeUri1',
      result_property: 'sample_size',
      value: 12
    }, {
      groupUri: 'groupUri1',
      instance: 'resultInstance4',
      momentUri: 'momentUri2',
      outcomeUri: 'outcomeUri1',
      result_property: 'count',
      value: 20
    }];

    var byGroupTargetResults = [{
      groupUri: 'groupUri1',
      instance: 'resultInstance1',
      momentUri: 'momentUri1',
      outcomeUri: 'outcomeUri1',
      result_property: 'sample_size',
      value: 12
    }, {
      groupUri: 'groupUri1',
      instance: 'resultInstance2',
      momentUri: 'momentUri1',
      outcomeUri: 'outcomeUri1',
      result_property: 'count',
      value: 20
    }];

    var isOverlap = function(a, b) {   // NB taken from OutcomeService
      return a.armUri === b.armUri &&
        a.momentUri === b.momentUri;
    };

    beforeEach(module('trialverse.outcome'));

    beforeEach(inject(function(RepairService) {
      repairService = RepairService;
    }));

    describe('findOverlappingResults', function() {

      it('should do thing to your data', function() {
        var expectedResults = byGroupSourceResults.slice(0, 2);
        expect(repairService.findOverlappingResults(byGroupSourceResults, byGroupTargetResults, isOverlap)).toEqual(expectedResults);
      });
    });

    describe('findNonOverlappingResults', function() {

      it('should do thing to your data', function() {
        var expectedResults = byGroupSourceResults.slice(2, 4);
        expect(repairService.findNonOverlappingResults(byGroupSourceResults, byGroupTargetResults, isOverlap)).toEqual(expectedResults);
      });
    });



  });
});

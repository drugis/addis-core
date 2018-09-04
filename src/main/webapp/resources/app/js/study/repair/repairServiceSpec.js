'use strict';
define(['angular-mocks', '../../outcome/outcome'], function() {
  describe('the repair service', function() {
    var repairService, q, rootScope;
    var studyService = jasmine.createSpyObj('StudyService', ['getJsonGraph', 'saveJsonGraph']);

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

    var isOverlap = function(a, b) { // NB taken from OutcomeService
      return a.armUri === b.armUri &&
        a.momentUri === b.momentUri;
    };

    beforeEach(function(){
      angular.mock.module('trialverse.outcome', function($provide){
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, RepairService) {
      q = $q;
      rootScope = $rootScope;
      repairService = RepairService;
    }));

    describe('findOverlappingResults', function() {

      it('should return the overlapping results', function() {
        var expectedResults = byGroupSourceResults.slice(0, 2);
        expect(repairService.findOverlappingResults(byGroupSourceResults, byGroupTargetResults, isOverlap)).toEqual(expectedResults);
      });
    });

    describe('findNonOverlappingResults', function() {

      it('should return the non-overlapping results', function() {
        var expectedResults = byGroupSourceResults.slice(2, 4);
        expect(repairService.findNonOverlappingResults(byGroupSourceResults, byGroupTargetResults, isOverlap)).toEqual(expectedResults);
      });
    });

    describe('mergeResults', function() {

      var studyGetJsonGraphDefer, studySaveJsonGraphDefer;

      beforeEach(function() {
        studyGetJsonGraphDefer = q.defer();
        var studyGetJsonGraphPromise = studyGetJsonGraphDefer.promise;
        studyService.getJsonGraph.and.returnValue(studyGetJsonGraphPromise);

        studySaveJsonGraphDefer = q.defer();
        var studySaveJsonGraphPromise = studySaveJsonGraphDefer.promise;
        studyService.saveJsonGraph.and.returnValue(studySaveJsonGraphPromise);
      });

      var studyJsonObject = [{
        '@id': 'resultInstance1',
        'of_group': 'oldGroup'
      }, {
        '@id': 'resultInstance2',
        'of_group': 'oldGroup'
      }, {
        '@id': 'resultInstance3',
        'of_group': 'oldGroup'
      }, {
        '@id': 'resultInstance4',
        'of_group': 'oldGroup'
      }];

      var expectedStudyObjectAfterMerge = [{
        '@id': 'resultInstance3',
        'of_group': 'groupTargetUri'
      }, {
        '@id': 'resultInstance4',
        'of_group': 'groupTargetUri'
      }];

      beforeEach(function(done) {
        var overlapFunction = function isOverlappingGroupMeasurement(a, b) {
          return a.momentUri === b.momentUri &&
            a.outcomeUri === b.outcomeUri;
        };

        studyGetJsonGraphDefer.resolve(studyJsonObject);
        studySaveJsonGraphDefer.resolve();

        repairService.mergeResults('groupTargetUri', byGroupSourceResults, byGroupTargetResults, overlapFunction, 'of_group').then(done);
        rootScope.$digest();
      });

      it('should merge the measurement results, nonOverlappingResults are moved from the source to the target, overlapping results are removed from the source', function() {
        expect(studyService.getJsonGraph).toHaveBeenCalled();
        expect(studyService.saveJsonGraph).toHaveBeenCalledWith(expectedStudyObjectAfterMerge);
      });
    });



  });
});

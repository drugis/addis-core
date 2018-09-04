'use strict';
define(['angular-mocks', './util'], function() {
  describe('the abstract group service', function() {

    var rootScope, q;
    var resultsService = jasmine.createSpyObj('ResultsService', ['queryResultsByGroup', 'queryNonConformantMeasurementsByGroupUri']);
    var studyService = jasmine.createSpyObj('StudyService', ['getStudy', 'save']);
    var repairService = jasmine.createSpyObj('RepairService', ['findOverlappingResults', 'findNonOverlappingResults', 'mergeResults']);
    var abstractGroupService;
    var byGroupSourceResults;
    var sourceResultsDefer, targetResultsDefer, sourceNonConformantResultsDefer,
      targetNonConformantResultsDefer, getStudyDefer, saveStudyDefer, mergeResultsDefer;

    beforeEach(function() {
      angular.mock.module('trialverse.util', function($provide) {
        $provide.value('ResultsService', resultsService);
        $provide.value('RepairService', repairService);
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, AbstractGroupService) {
      q = $q;
      rootScope = $rootScope;

      sourceResultsDefer = $q.defer();
      var sourceResultsPromise = sourceResultsDefer.promise;
      targetResultsDefer = $q.defer();
      var targetResultsPromise = targetResultsDefer.promise;
      resultsService.queryResultsByGroup.and.returnValues(sourceResultsPromise, targetResultsPromise);

      sourceNonConformantResultsDefer = $q.defer();
      var sourceNonConformantResultsPromise = sourceNonConformantResultsDefer.promise;
      targetNonConformantResultsDefer = $q.defer();
      var targetNonConformantResultsPromise = targetNonConformantResultsDefer.promise;
      resultsService.queryNonConformantMeasurementsByGroupUri.and.returnValues(sourceNonConformantResultsPromise, targetNonConformantResultsPromise);

      getStudyDefer = $q.defer();
      var getStudyPromise = getStudyDefer.promise;
      studyService.getStudy.and.returnValue(getStudyPromise);

      saveStudyDefer = $q.defer();
      var saveStudyPromise = saveStudyDefer.promise;
      studyService.save.and.returnValue(saveStudyPromise);

      mergeResultsDefer = $q.defer();
      var mergeResultsPromise = mergeResultsDefer.promise;
      repairService.mergeResults.and.returnValue(mergeResultsPromise);

      abstractGroupService = AbstractGroupService;
    }));


    describe('merge', function() {
      byGroupSourceResults = [{
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

      var groupSource = {
        groupUri: 'groupSourceUri'
      };
      var groupTarget = {
        groupUri: 'groupTargetUri'
      };

      var getStudyResult = {
        has_group: [{
          '@id': 'groupSourceUri',
          '@type': 'ontology:Group',
          'label': 'group label'
        }]
      };

      var expectedSaveAfterDelete = {
        has_group: []
      };

      beforeEach(function(done) {
        sourceResultsDefer.resolve(byGroupSourceResults);
        targetResultsDefer.resolve(byGroupTargetResults);
        sourceNonConformantResultsDefer.resolve([]);
        targetNonConformantResultsDefer.resolve([]);
        mergeResultsDefer.resolve();
        getStudyDefer.resolve(getStudyResult);
        saveStudyDefer.resolve();
        abstractGroupService.merge(groupSource, groupTarget).then(done);
        rootScope.$digest();
      });

      it('should have remove the double results, move the non double resutls and delete the merge source', function() {
        expect(resultsService.queryResultsByGroup).toHaveBeenCalledWith('groupSourceUri');
        expect(resultsService.queryResultsByGroup).toHaveBeenCalledWith('groupTargetUri');
        expect(repairService.mergeResults).toHaveBeenCalledWith('groupTargetUri', byGroupSourceResults, byGroupTargetResults, jasmine.any(Function), 'of_group');
        expect(studyService.getStudy).toHaveBeenCalled();
        expect(studyService.save).toHaveBeenCalledWith(expectedSaveAfterDelete);
      });
    });

    describe('hasOverlap', function() {
      byGroupSourceResults = [{
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

      var groupSource = {
        groupUri: 'groupSourceUri'
      };
      var groupTarget = {
        groupUri: 'groupTargetUri'
      };

      var hasOverlap;

      beforeEach(function(done) {
        sourceResultsDefer.resolve(byGroupSourceResults);
        targetResultsDefer.resolve(byGroupTargetResults);
        sourceNonConformantResultsDefer.resolve([]);
        targetNonConformantResultsDefer.resolve([]);
        repairService.findOverlappingResults.and.returnValue([{}]);

        abstractGroupService.hasOverlap(groupSource, groupTarget).then(function(res) {
          hasOverlap = res;
          done();
        });
        rootScope.$digest();
      });

      it('should return true when when there is overlap', function() {
        expect(resultsService.queryResultsByGroup).toHaveBeenCalledWith('groupSourceUri');
        expect(resultsService.queryResultsByGroup).toHaveBeenCalledWith('groupTargetUri');
        expect(hasOverlap).toBe(true);
      });
    });

  });
});

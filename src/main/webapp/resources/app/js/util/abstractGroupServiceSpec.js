'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the abstract group service', function() {

    var rootScope, q;
    var resultsService = jasmine.createSpyObj('ResultsService', ['queryResultsByGroup']);
    var studyService = jasmine.createSpyObj('StudyService', ['getJsonGraph', 'saveJsonGraph', 'getStudy', 'save']);
    var abstractGroupService;
    var byGroupSourceResults;
    var sourceResultsDefer, targetResultsDefer, studyGetJsonGraphDefer, studySaveJsonGraphDefer,
      getStudyDefer, saveStudyDefer;

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        $provide.value('ResultsService', resultsService);
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

      studyGetJsonGraphDefer = $q.defer();
      var studyGetJsonGraphPromise = studyGetJsonGraphDefer.promise;
      studyService.getJsonGraph.and.returnValue(studyGetJsonGraphPromise);

      studySaveJsonGraphDefer = $q.defer();
      var studySaveJsonGraphPromise = studySaveJsonGraphDefer.promise;
      studyService.saveJsonGraph.and.returnValue(studySaveJsonGraphPromise);

      getStudyDefer = $q.defer();
      var getStudyPromise = getStudyDefer.promise;
      studyService.getStudy.and.returnValue(getStudyPromise);

      saveStudyDefer = $q.defer();
      var saveStudyPromise = saveStudyDefer.promise;
      studyService.save.and.returnValue(saveStudyPromise);

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
      },{
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
      }
    ];

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

      var studyJsonObject = [{
        '@id': 'resultInstance1',
        'of_group' : 'oldGroup'
      }, {
        '@id': 'resultInstance2',
        'of_group' : 'oldGroup'
      },{
        '@id': 'resultInstance3',
        'of_group' : 'oldGroup'
      },{
        '@id': 'resultInstance4',
        'of_group' : 'oldGroup'
      }];

      var expectedStudyObjectAfterMerge = [
        {
          '@id': 'resultInstance3',
          'of_group' : 'groupTargetUri'
        },{
          '@id': 'resultInstance4',
          'of_group' : 'groupTargetUri'
        }
      ];

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
        studyGetJsonGraphDefer.resolve(studyJsonObject);
        studySaveJsonGraphDefer.resolve();
        getStudyDefer.resolve(getStudyResult);
        saveStudyDefer.resolve();
        abstractGroupService.merge(groupSource, groupTarget).then(done);
        rootScope.$digest();
      });

      it('should have remove the double results, move the non double resutls and delete the merge source', function() {
        expect(resultsService.queryResultsByGroup).toHaveBeenCalledWith('groupSourceUri');
        expect(resultsService.queryResultsByGroup).toHaveBeenCalledWith('groupTargetUri');
        expect(studyService.getJsonGraph).toHaveBeenCalled();
        expect(studyService.saveJsonGraph).toHaveBeenCalledWith(expectedStudyObjectAfterMerge);
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
      },{
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
      }
    ];

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

        abstractGroupService.hasOverlap(groupSource, groupTarget).then(function(res){
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

'use strict';
define(['angular', 'angular-mocks'], function() {
  xdescribe('the abstract group service', function() {

    var rootScope, q;
    var resultsService = jasmine.createSpyObj('ResultsService', ['queryResultsByGroup']);
    var studyService = jasmine.createSpyObj('StudyService', ['getJsonGraph', 'saveJsonGraph']);
    var abstractGroupService;
    var byGroupResults;
    var resultsDefer, studyGetJsonGraphDefer;

    beforeEach(function() {
      module('trialverse.util', function($provide) {
        $provide.value('ResultsService', resultsService);
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, AbstractGroupService) {
      q = $q;
      rootScope = $rootScope;

      resultsDefer = $q.defer();
      var resultsPromise = resultsDefer.promise;
      resultsService.queryResultsByGroup.and.returnValue(resultsPromise);

      studyGetJsonGraphDefer = $q.defer();
      var studyGetJsonGraphPromise = studyGetJsonGraphDefer.promise;
      studyService.getJsonGraph.and.returnValue(studyGetJsonGraphPromise);

      // studyGetJsonGraphDefer = $q.defer();
      // var studyGetJsonGraphPromise = studyGetJsonGraphDefer.promise;
      // studyService.getJsonGraph.and.returnValue(studyGetJsonGraphPromise);

      abstractGroupService = AbstractGroupService;
    }));


    describe('merge', function() {
      byGroupResults = [{
        '@id': 'http://trials.drugis.org/instances/result2',
        'standard_deviation': 2,
        'mean': 5,
        'of_group': 'http://trials.drugis.org/instances/arm2',
        'of_moment': 'http://trials.drugis.org/instances/moment1',
        'of_outcome': 'http://trials.drugis.org/instances/outcome1',
        'sample_size': 33
      }, {
        '@id': 'http://trials.drugis.org/instances/result3',
        'count': 3,
        'of_group': 'http://trials.drugis.org/instances/arm2',
        'of_moment': 'http://trials.drugis.org/instances/moment1',
        'of_outcome': 'http://trials.drugis.org/instances/outcome2',
        'sample_size': 33
      }];

      var studyJsonObject = {
        has_group: [{
          '@id': 'groupTargetUri',
          '@type': 'ontology:Group',
          'label': 'group label'
        }],
        has_arm: []
      };

      var groupSource = {
        groupUri: 'groupSourceUri'
      };
      var groupTarget = {
        groupUri: 'groupTargetUri'
      };

      beforeEach(function(done) {
        resultsDefer.resolve(byGroupResults);
        studyGetJsonGraphDefer.resolve(studyJsonObject);
        abstractGroupService.merge(groupSource, groupTarget).then(done);
        rootScope.$digest();
      });

      it('should have merged', function() {
        expect(resultsService.queryResults).toHaveBeenCalledWith('groupSourceUri');
        expect(resultsService.queryResults).toHaveBeenCalledWith('groupTargetUri');
      });
    });

    // describe('delete group', function() {
    //   beforeEach(function(done) {
    //     groupService.deleteItem(result[1]).then(done);
    //     rootScope.$digest();
    //   });
    //
    //   it('should delete the group', function(done) {
    //     done();
    //   });
    // });

  });
});

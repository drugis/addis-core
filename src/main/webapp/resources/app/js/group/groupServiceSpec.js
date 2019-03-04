'use strict';
define(['angular', 'angular-mocks', './group'], function(angular) {
  describe('the group service', function() {

    var rootScope, q;
    var studyService = jasmine.createSpyObj('StudyService', ['getStudy', 'save']);
    var groupService, armService;
    var studyDefer;

    beforeEach(function() {
      angular.mock.module('trialverse.group', function($provide) {
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, GroupService, ArmService) {
      q = $q;
      rootScope = $rootScope;

      studyDefer = $q.defer();
      var getStudyPromise = studyDefer.promise;
      studyService.getStudy.and.returnValue(getStudyPromise);
      groupService = GroupService;
      armService = ArmService;
    }));

    describe('for a study without included population', function() {
      beforeEach(function() {
        var studyJsonObject = {
          has_group: [{
            '@id': 'http://trials.drugis.org/instances/1c3c67ba-4c0c-46e3-846c-5e9d72c5ed80',
            '@type': 'ontology:Group',
            'label': 'group label'
          }],
          has_arm: []
        };
        studyDefer.resolve(studyJsonObject);

        rootScope.$digest();
      });

      describe('query groups', function() {

        it('should query the groups and add study population', function(done) {
          groupService.queryItems().then(function(result) {
            expect(result.length).toBe(2);
            expect(result[0].label).toEqual('group label');
            done();
          });
          rootScope.$digest();
        });

      });

      describe('addItem', function() {
        var newGroup = {
          label: 'new group label'
        };
        var groupsResult;
        beforeEach(function(done) {
          groupService.addItem(newGroup).then(function() {
            groupService.queryItems().then(function(result) {
              groupsResult = result;
              done();
            });
          });
          rootScope.$digest();
        });

        it('should add the group to the graph', function() {
          expect(groupsResult.length).toBe(3);
          expect(groupsResult[0].label).toEqual('group label');
          expect(groupsResult[1].label).toEqual(newGroup.label);
        });
      });

      describe('edit group', function() {
        var editedGroup = {
          '@id': 'http://trials.drugis.org/instances/1c3c67ba-4c0c-46e3-846c-5e9d72c5ed80',
          label: 'edited label'
        };

        var editResult;

        beforeEach(function(done) {
          groupService.queryItems().then(function(result) {
            result[0].label = editedGroup.label;
            groupService.editItem(result[0]).then(function() {
              groupService.queryItems().then(function(result) {
                editResult = result;
                done();
              });
            });
          });
          rootScope.$digest();
        });

        it('should edit the group', function() {
          expect(editResult.length).toBe(2);
          expect(editResult[0].label).toBe(editedGroup.label);
        });
      });

      describe('reclassifyAsArm', function() {
        var editResult, armsResult;

        beforeEach(function(done) {
          groupService.queryItems().then(function(result) {
            groupService.reclassifyAsArm(result[0]).then(function() {
              q.all([groupService.queryItems(), armService.queryItems()]).then(function(groupsAndArms) {
                editResult = groupsAndArms[0];
                armsResult = groupsAndArms[1];
                done();
              });
            });
          });
          rootScope.$digest();
        });

        it('should reclassify the group as a arm', function() {
          expect(editResult.length).toBe(1); // includes overall population
          expect(armsResult.length).toBe(1);
          expect(armsResult[0].label).toBe('group label');
        });
      });

      describe('delete group', function() {


        beforeEach(function(done) {
          var group2 = {
            label: 'group 2 label'
          };
          var group3 = {
            label: 'group 3 label'
          };
          groupService.addItem(group2).then(function() {
            groupService.addItem(group3).then(function() {
              groupService.queryItems().then(function(result) {
                groupService.deleteItem(result[0]).then(done);
              });
            });
          });
          rootScope.$digest();
        });

        it('should delete the group', function(done) {
          groupService.queryItems().then(function(result) {
            expect(result.length).toBe(3);
            expect(result[0].label).toBe('group 3 label');
            expect(result[1].label).toBe('group label');
            done();
          });
          rootScope.$digest();
        });
      });
    });

    describe('for a study with included population', function() {
      beforeEach(function() {
        var studyJsonObject = {
          has_group: [{
            '@id': 'http://trials.drugis.org/instances/1c3c67ba-4c0c-46e3-846c-5e9d72c5ed80',
            '@type': 'ontology:Group',
            'label': 'group label'
          }],
          has_included_population: [{
            '@id': 'http://trials.drugis.org/instances/uuuuuid',
            '@type': 'ontology:StudyPopulation'
          }]
        };
        studyDefer.resolve(studyJsonObject);

        rootScope.$digest();
      });
      it('queryitems should return the groups', function(done) {
        groupService.queryItems().then(function(result) {
          expect(result.length).toBe(2);
          expect(result[0].label).toEqual('group label');
          done();
        });
        rootScope.$digest();
      });
    });

  });
});
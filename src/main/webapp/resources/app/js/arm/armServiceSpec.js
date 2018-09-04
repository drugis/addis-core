'use strict';
define(['angular-mocks', './arm', '../group/group'], function() {
  describe('the arm service', function() {

    var rootScope, q;
    var studyService = jasmine.createSpyObj('StudyService', ['getStudy', 'save']);
    var armService, groupService;

    beforeEach(function() {
      angular.mock.module('trialverse.arm', function($provide) {
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(function() {
      angular.mock.module('trialverse.group', function($provide) {
        $provide.value('StudyService', studyService);
      });
    });


    beforeEach(inject(function($q, $rootScope, ArmService, GroupService) {
      q = $q;
      rootScope = $rootScope;

      var studyJsonObject = {
        has_arm: [{
          '@id': 'http://trials.drugis.org/instances/someInstance',
          '@type': 'ontology:Arm',
          'label': 'xyz arm label'
        }, {
          '@id': 'http://trials.drugis.org/instances/otherInstance',
          '@type': 'ontology:Arm',
          'label': 'abc arm label'
        }],
        has_group: []
      };
      var studyDefer = $q.defer();
      var getStudyPromise = studyDefer.promise;
      studyDefer.resolve(studyJsonObject);
      studyService.getStudy.and.returnValue(getStudyPromise);
      armService = ArmService;
      groupService = GroupService;
      rootScope.$digest();
    }));

    describe('query arms', function() {

      it('should query the arms', function(done) {
        armService.queryItems().then(function(result) {
          expect(result.length).toBe(2);
          expect(result[0].label).toEqual('abc arm label');
          done();
        });
        rootScope.$digest();
      });

    });

    describe('addItem', function() {
      var newArm = {
        label: 'new arm label'
      };
      var armsResult;
      beforeEach(function(done) {
        armService.addItem(newArm).then(function() {
          armService.queryItems().then(function(result) {
            armsResult = result;
            done();
          });
        });
        rootScope.$digest();
      });

      it('should add the arm to the graph', function() {
        expect(armsResult.length).toBe(3);
        expect(armsResult[0].label).toEqual('abc arm label');
        expect(armsResult[1].label).toEqual(newArm.label);
      });
    });

    describe('edit arm', function() {
      var editedArm = {
        '@id': 'http://trials.drugis.org/instances/1c3c67ba-4c0c-46e3-846c-5e9d72c5ed80',
        label: 'edited label'
      };

      var editResult;

      beforeEach(function(done) {
        armService.queryItems().then(function(result) {
          result[0].label = editedArm.label;
          armService.editItem(result[0]).then(function() {
            armService.queryItems().then(function(result) {
              editResult = result;
              done();
            });
          });
        });
        rootScope.$digest();
      });

      it('should edit the arm', function() {
        expect(editResult.length).toBe(2);
        expect(editResult[0].label).toBe(editedArm.label);
      });
    });

    describe('reclassifyAsGroup', function() {
      var editResult, groupsResult;

      beforeEach(function(done) {
        armService.queryItems().then(function(result) {
          armService.reclassifyAsGroup(result[0]).then(function() {
            q.all([armService.queryItems(), groupService.queryItems()]).then(function(armsAndGroups) {
              editResult = armsAndGroups[0];
              groupsResult = armsAndGroups[1];
              done();
            });
          });
        });
        rootScope.$digest();
      });

      it('should reclassify the arm as a group', function() {
        expect(editResult.length).toBe(1);
        expect(groupsResult.length).toBe(2); // includes overall population
        var newGroupIndex = groupsResult[0].label === 'Overall population' ? 1 : 0;
        expect(groupsResult[newGroupIndex].label).toBe('abc arm label');
      });
    });

    describe('delete arm', function() {

      beforeEach(function(done) {
        var arm2 = {
          label: 'arm 2 label'
        };
        var arm3 = {
          label: 'arm 3 label'
        };
        armService.addItem(arm2).then(function() {
          armService.addItem(arm3).then(function() {
            armService.queryItems().then(function(result) {
              armService.deleteItem(result[1]).then(done);
            });
          });
        });
        rootScope.$digest();
      });

      it('should delete the arm', function(done) {
        armService.queryItems().then(function(result) {
          expect(result.length).toBe(3);
          expect(result[0].label).toBe('abc arm label');
          expect(result[1].label).toBe('arm 3 label');
          done();
        });
        rootScope.$digest();
      });
    });
  });
});
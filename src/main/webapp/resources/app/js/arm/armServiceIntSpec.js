'use strict';
define(['angular', 'angular-mocks'], function(angular, angularMocks) {
  fdescribe('the arm service', function() {

    var graphUri = 'http://karma-test/';
    var scratchStudyUri = 'http://localhost:9876/scratch'; // NB proxied by karma to actual fuseki instance

    var rootScope, q, httpBackend;
    var remotestoreServiceStub;
    var studyService = jasmine.createSpyObj('StudyService', ['getStudy', 'save']);
    var armService;

    beforeEach(function() {
      module('trialverse.arm', function($provide) {
        $provide.value('StudyService', studyService)
      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, ArmService) {
      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;

      var studyJsonObject = {
        has_arm: [{
          "@id": "http://trials.drugis.org/instances/1c3c67ba-4c0c-46e3-846c-5e9d72c5ed80",
          "@type": "ontology:Arm",
          "label": "arm label"
        }]
      }
      var studyDefer = $q.defer();
      var getStudyPromise = studyDefer.promise;
      studyDefer.resolve(studyJsonObject);
      studyService.getStudy.and.returnValue(getStudyPromise);
      armService = ArmService;

      rootScope.$digest();
    }));

    fdescribe('query arms', function() {

      it('should query the arms', function(done) {
        armService.queryItems().then(function(result) {
          expect(result.length).toBe(1);
          expect(result[0].label).toEqual('arm label');
          done();
        });
        rootScope.$digest();
      });

    });

    fdescribe('addItem', function() {
      var studyUuid = 'studyUuid';
      var newArm = {
        label: 'new arm label'
      };
      var armsResult;
      beforeEach(function(done) {
        armService.addItem(newArm).then(function() {
          armService.queryItems().then(function(result) {
            armsResult = result;
            done();
          })
        });
        rootScope.$digest();
      });

      it('should add the arm to the graph', function() {
        expect(armsResult.length).toBe(2);
        expect(armsResult[0].label).toEqual('arm label');
        expect(armsResult[1].label).toEqual(newArm.label);
      });
    });

    fdescribe('edit arm', function() {
      var editedArm = {
        "@id": "http://trials.drugis.org/instances/1c3c67ba-4c0c-46e3-846c-5e9d72c5ed80",
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
        expect(editResult.length).toBe(1);
        expect(editResult[0].label).toBe(editedArm.label);
      });
    });

    fdescribe('delete arm', function() {


      beforeEach(function(done) {

        armService.queryItems().then(function(result) {
          armService.deleteItem(result[0]).then(done);
        });

        rootScope.$digest();
      });

      it('should delete the arm', function(done) {
        armService.queryItems().then(function(result) {
          expect(result.length).toBe(0);
          done();
        });
      });
    });


  });
});

'use strict';
define(['angular-mocks', './studyInformation'], function() {
  describe('the population information service', function() {

    var rootScope, q,
      uUIDServiceStub,
      studyServiceMock = jasmine.createSpyObj('StudyService', ['getStudy', 'save']),

      studyInformationService,
      mockGeneratedUuid = 'newUuid',
      studyDefer,
      jsonStudy
    ;

    beforeEach(angular.mock.module('trialverse.studyInformation'));
    beforeEach(function() {
      angular.mock.module('trialverse.util', function($provide) {
        uUIDServiceStub = jasmine.createSpyObj('UUIDService', [
          'generate'
        ]);
        uUIDServiceStub.generate.and.returnValue(mockGeneratedUuid);
        $provide.value('UUIDService', uUIDServiceStub);
        $provide.value('StudyService', studyServiceMock);
      });
    });


    beforeEach(inject(function($q, $rootScope, StudyInformationService) {
      q = $q;
      rootScope = $rootScope;

      studyDefer = q.defer();
      studyServiceMock.getStudy.and.returnValue(studyDefer.promise);

      studyInformationService = StudyInformationService;

    }));


    describe('query study information', function() {

      var result;
      jsonStudy = {
        has_blinding: 'ontology:SingleBlind',
        has_allocation: 'ontology:AllocationRandomized',
        status: 'ontology:StatusWithdrawn',
        has_number_of_centers: 37,
        has_objective: [{
          comment: 'objective'
        }]
      };

      beforeEach(function(done) {
        studyDefer.resolve(jsonStudy);
        studyInformationService.queryItems().then(function(info) {
          result = info;
          done();
        });
        rootScope.$digest();
      });

      it('should return study information', function() {
        expect(result.length).toBe(1);
        expect(result[0].blinding).toBe(jsonStudy.has_blinding);
        expect(result[0].allocation).toBe(jsonStudy.has_allocation);
        expect(result[0].status).toBe(jsonStudy.status);
        expect(result[0].numberOfCenters).toBe(jsonStudy.has_number_of_centers);
        expect(result[0].objective).toBe(jsonStudy.has_objective[0]);
      });
    });

    describe('edit study information when there is no previous information', function() {

      var newInformation = {
        allocation: {
          uri: 'ontology:AllocationRandomized'
        },
        blinding: {
          uri: 'ontology:SingleBlind'
        },
        status: {
          uri: 'ontology:Completed'
        },
        numberOfCenters: 29,
        objective: {
          comment: 'new study objective'
        }
      };
      var studyInformation;

      beforeEach(function(done) {
        jsonStudy = {
          has_objective: []
        };
        studyDefer.resolve(jsonStudy);

        studyInformationService.editItem(newInformation);
        studyInformationService.queryItems().then(function(resultInfo) {
          studyInformation = resultInfo;
          done();
        });
        rootScope.$digest();
      });

      it('should make the new study information accessible', function() {
        expect(studyInformation).toBeDefined();
        expect(studyInformation[0].blinding).toEqual(newInformation.blinding.uri);
        expect(studyInformation[0].allocation).toEqual(newInformation.allocation.uri);
        expect(studyInformation[0].status).toEqual(newInformation.status.uri);
        expect(studyInformation[0].numberOfCenters).toEqual(newInformation.numberOfCenters);
        expect(studyInformation[0].objective.comment).toBe(newInformation.objective.comment);
      });

    });

    describe('edit study information with "unknown" values in selects', function() {
      var result;
      var oldStudy = {
        has_blinding: 'ontology:DoubleBlind',
        has_allocation: 'ontology:AllocationRandomized',
        status: 'ontology:StatusWithdrawn',
        has_number_of_centers: 37,
        has_objective: []
      };
      var newInformation = {
        has_blinding: {
          uri: 'unknown'
        },
        has_allocation: {
          uri: 'unknown'
        },
        status: {
          uri: 'unknown'
        }
      };

      beforeEach(function(done) {
        jsonStudy = oldStudy;
        studyDefer.resolve(jsonStudy);

        oldStudy.has_blinding = newInformation.has_blinding;
        oldStudy.has_allocation = newInformation.has_allocation;
        oldStudy.status = newInformation.status;

        studyInformationService.editItem(newInformation);
        studyInformationService.queryItems().then(function(resultInfo) {
          result = resultInfo;
          done();
        });
        rootScope.$digest();
      });

      it('should delete previously selected values', function() {
        expect(result.length).toBe(1);
        expect(result[0].has_blinding).not.toBeDefined();
        expect(result[0].has_allocation).not.toBeDefined();
        expect(result[0].status).not.toBeDefined();
        expect(result[0].has_number_of_centers).toBe(oldStudy.has_number_of_centers);
      });
    });


  });
});

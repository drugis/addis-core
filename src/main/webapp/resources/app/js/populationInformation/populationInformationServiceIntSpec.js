'use strict';
define(['angular-mocks', './populationInformation'], function() {
  describe('the population information service', function() {

    var rootScope, q;
    var uUIDServiceStub;
    var studyService = jasmine.createSpyObj('StudyService', ['getStudy', 'save']);

    var populationInformationService;
    var mockGeneratedUuid = 'newUuid';
    var studyJsonObject = {
      has_indication: [{
        '@id': 'http://trials.drugis.org/instances/abc-123',
        '@type': 'ontology:Indication',
        'label': 'Severe depression',
        'sameAs': 'http://trials.drugis.org/concepts/xyz-456'
      }],
      has_eligibility_criteria: [{
        '@id': 'http://eligibility_criteria_id',
        'comment': 'over 22 years of age'
      }]
    };
    var studyWithoutIndicationJsonObject = {
      has_indication: [],
      has_eligibility_criteria: [{
        '@id': 'http://eligibility_criteria_id',
        'comment': 'over 22 years of age'
      }]
    };

    beforeEach(angular.mock.module('trialverse.populationInformation'));
    beforeEach(function() {
      angular.mock.module('trialverse.util', function($provide) {
        uUIDServiceStub = jasmine.createSpyObj('UUIDService', [
          'generate'
        ]);
        uUIDServiceStub.generate.and.returnValue(mockGeneratedUuid);
        $provide.value('UUIDService', uUIDServiceStub);
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, PopulationInformationService) {

      q = $q;
      rootScope = $rootScope;
      populationInformationService = PopulationInformationService;

      rootScope.$digest();
    }));

    afterEach(function() {
      studyService.save.calls.reset();
    });


    describe('query population information', function() {

      var result;

      beforeEach(inject(function($q, $rootScope) {
        q = $q;
        rootScope = $rootScope;
        var studyDefer = $q.defer();
        var getStudyPromise = studyDefer.promise;
        studyDefer.resolve(angular.copy(studyJsonObject));
        studyService.getStudy.and.returnValue(getStudyPromise);

        rootScope.$digest();
      }));

      beforeEach(function(done) {
        populationInformationService.queryItems().then(function(info) {
          result = info;
          done()
        });
        rootScope.$digest();
      });

      it('should return the population information contained in the study', function() {
        expect(result.length).toBe(1);
        expect(result[0].indication.label).toBe('Severe depression');
        expect(result[0].eligibilityCriteria.label).toBe('over 22 years of age');
      });

    });

    describe('query population information on study without a indication', function() {
      var result;
      var studyDefer;
      var getStudyPromise;

      beforeEach(inject(function($q, $rootScope) {
        q = $q;
        rootScope = $rootScope;
        studyDefer = $q.defer();
        getStudyPromise = studyDefer.promise;
        studyDefer.resolve(angular.copy(studyWithoutIndicationJsonObject));
        studyService.getStudy.and.returnValue(getStudyPromise);

        rootScope.$digest();
      }));

      beforeEach(function(done) {
        populationInformationService.queryItems().then(function(info) {
          result = info;
          done();
        });
        rootScope.$digest();
      });

      it('should return the population information contained in the study', function() {
        expect(result.length).toBe(1);
        expect(result[0].indication).toBe(undefined);
      });

    });

    describe('edit population information when indication was present on study', function() {

      var studyDefer, getStudyPromise;
      var newInformation;


      beforeEach(inject(function($q, $rootScope) {
        q = $q;
        rootScope = $rootScope;
        studyDefer = $q.defer();
        getStudyPromise = studyDefer.promise;
        studyDefer.resolve(angular.copy(studyJsonObject));
        studyService.getStudy.and.returnValue(getStudyPromise);

        rootScope.$digest();
      }));

      beforeEach(function(done) {
        newInformation = {
          indication: {
            label: 'new label'
          },
          eligibilityCriteria: {
            label: 'eligibility label'
          }
        };

        populationInformationService.editItem(newInformation).then(done);
        rootScope.$digest();
      });

      it('should make the new population information accessible', function() {

        var expedtedToBesaved = {
          has_indication: [{
            '@id': 'http://trials.drugis.org/instances/abc-123',
            '@type': 'ontology:Indication',
            'label': 'new label',
            'sameAs': 'http://trials.drugis.org/concepts/xyz-456'
          }],
          has_eligibility_criteria: [{
            '@id': 'http://eligibility_criteria_id',
            'comment': 'eligibility label'
          }]
        };

        expect(studyService.save).toHaveBeenCalledWith(expedtedToBesaved);

      });

    });

    describe('edit population information when NO indication was present on study', function() {

      var studyDefer, getStudyPromise;
      var newInformation;


      beforeEach(inject(function($q, $rootScope) {
        q = $q;
        rootScope = $rootScope;
        studyDefer = $q.defer();
        getStudyPromise = studyDefer.promise;
        studyDefer.resolve(angular.copy(studyWithoutIndicationJsonObject));
        studyService.getStudy.and.returnValue(getStudyPromise);

        rootScope.$digest();
      }));

      beforeEach(function(done) {

        newInformation = {
          indication: {
            label: 'new label'
          },
          eligibilityCriteria: {
            label: 'eligibility label'
          }
        };

        populationInformationService.editItem(newInformation).then(done);
        rootScope.$digest();
      });

      it('should persist the changes an add the id', function() {

        var expedtedToBesaved = {

          has_eligibility_criteria: [{
            '@id': studyJsonObject.has_eligibility_criteria[0]['@id'],
            'comment': newInformation.eligibilityCriteria.label
          }],
          has_indication: [{
            '@id': 'http://trials.drugis.org/instances/newUuid',
            'label': newInformation.indication.label,
          }],
        };

        expect(studyService.save).toHaveBeenCalledWith(expedtedToBesaved);

      });

    });


  });
});

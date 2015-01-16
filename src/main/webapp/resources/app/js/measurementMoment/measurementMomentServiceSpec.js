'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the measurement moment service', function() {
    var
      sparqlResource = jasmine.createSpyObj('SparqlResource', ['get']),
      studyService = jasmine.createSpyObj('StudyService', ['doModifyingQuery', 'doNonModifyingQuery']),
      q, rootScope,
      nonModifyingQueryPromise, queryResourcePromise,
      measurementMomentService;

    beforeEach(module('trialverse.measurementMoment'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        $provide.value('StudyService', studyService);
        $provide.value('SparqlResource', sparqlResource);
      });
    });

    beforeEach(inject(function($q, $rootScope) {
      q = $q;
      rootScope = $rootScope;
      queryResourcePromise = q.defer();
      nonModifyingQueryPromise = q.defer();
      studyService.doNonModifyingQuery.and.returnValue(nonModifyingQueryPromise);
      sparqlResource.get.and.returnValue({
        $promise: queryResourcePromise.promise
      });
    }));

    beforeEach(inject(function(MeasurementMomentService) {
      measurementMomentService = MeasurementMomentService;
    }));

    describe('queryItems', function() {
      it('should query the measurement moments', function() {
        var result = measurementMomentService.queryItems();
        var queryResult = 'any string 2'

        queryResourcePromise.resolve('any string 1');
        nonModifyingQueryPromise.resolve(queryResult);
        rootScope.$digest();

        expect(result.$$state.value.promise.$$state.value).toEqual(queryResult);
      });
    });

  });
});

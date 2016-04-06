'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('unit service', function() {
    var dosageService,
      sparqlResourceMock = jasmine.createSpyObj('SparqlResource', ['get']),
      sparqlDefer,
      response;

    beforeEach(module('addis.interventions', function($provide) {
      $provide.value('SparqlResource', sparqlResourceMock);
    }));

    beforeEach(inject(function($rootScope, $q, $httpBackend, DosageService) {
      sparqlDefer = $q.defer();
      sparqlDefer.resolve('');
      sparqlResourceMock.get.and.returnValue(sparqlDefer.promise);
      dosageService = DosageService;
      $httpBackend.expect('GET', '/users/user/datasets/data-s3t/versions/vers-i0n').respond(response);
      $rootScope.$digest();
    }));

    describe('get', function() {
      it('should query and transform the response', function(done) {
        var userUid = 'user',
          datasetUuid = 'data-s3t',
          datasetVersionUuid = 'vers-i0n';
        response = JSON.stringify({
          results: {
            bindings: [{
              'test': 'value'
            }]
          }
        });
        dosageService.get(userUid, datasetUuid, datasetVersionUuid).then(function(result) {
          expect(result).toEqual([{
            unitName: 'milligram'
          }, {
            unitName: 'milliliter'
          }]);
          done();
        });
      });
    });

  });
});

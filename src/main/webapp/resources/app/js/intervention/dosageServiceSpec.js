'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('unit service', function() {
    var
      scope,
      httpBackend,
      dosageService,
      response;
    beforeEach(module('addis.interventions'));

    beforeEach(inject(function($rootScope, $httpBackend, DosageService) {
      scope = $rootScope;
      httpBackend = $httpBackend;
      dosageService = DosageService;
      $httpBackend.expect('GET', 'app/sparql/queryUnits.sparql').respond('its sparql mom');
      $httpBackend.flush();
      scope.$apply();
    }));

    fdescribe('get', function() {
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
      httpBackend.expect('GET', '/users/user/datasets/data-s3t/versions/vers-i0n').respond(response);
      httpBackend.flush();
      scope.$apply();
    });

  });
});

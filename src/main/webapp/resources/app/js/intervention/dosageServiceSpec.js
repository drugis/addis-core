'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('unit service', function() {
    var
      scope,
      httpBackend,
      dosageService;
    beforeEach(module('addis.interventions'));

    beforeEach(inject(function($rootScope, $httpBackend, DosageService) {
      scope = $rootScope;
      httpBackend = $httpBackend;
      dosageService = DosageService;
      $httpBackend.expect('GET', 'app/sparql/queryUnits.sparql').respond('its sparql mom');
      $httpBackend.flush();
      scope.$apply();
    }));

    describe('get', function() {
      it('should query and transform the response', function(done) {
        var userUid = 'user',
          datasetUuid = 'data-s3t',
          datasetVersionUuid = 'vers-i0n',
          response = JSON.stringify({
            'head': {
              'vars': ['unitName', 'unitPeriod']
            },
            'results': {
              'bindings': [{
                'unitName': {
                  'type': 'literal',
                  'value': 'milligram'
                },
                'unitPeriod': {
                  'datatype': 'http://www.w3.org/2001/XMLSchema#duration',
                  'type': 'typed-literal',
                  'value': 'P1D'
                }
              }, {
                'unitName': {
                  'type': 'literal',
                  'value': 'milligram'
                },
                'unitPeriod': {
                  'datatype': 'http://www.w3.org/2001/XMLSchema#duration',
                  'type': 'typed-literal',
                  'value': 'PT1H'
                }
              }]
            }
          });
        httpBackend.expect('GET', '/users/user/datasets/data-s3t/versions/vers-i0n/query?query=its+sparql+mom').respond(response);
        dosageService.get(userUid, datasetUuid, datasetVersionUuid).then(function(result) {
          expect(result).toEqual([{
            unitName: 'milligram',
            unitPeriod: 'P1D',
            label: 'milligram/day'
          }, {
            unitName: 'milligram',
            unitPeriod: 'PT1H',
            label: 'milligram/hour'
          }]);
          done();
        });
        httpBackend.flush();
        scope.$apply();
      });
    });

  });
});

'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('unit dosage service', function() {
    var
      scope,
      httpBackend,
      dosageService;
    beforeEach(angular.mock.module('addis.interventions'));

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
                },
                'unitConcept': {
                  type: 'uri',
                  value: 'conceptUri'
                },
              }, {
                'unitName': {
                  'type': 'literal',
                  'value': 'milligram'
                },
                'unitPeriod': {
                  'datatype': 'http://www.w3.org/2001/XMLSchema#duration',
                  'type': 'typed-literal',
                  'value': 'PT1H'
                },
                'unitConcept': {
                  type: 'uri',
                  value: 'conceptUri'
                },
              }]
            }
          });
        httpBackend.expect('GET', '/users/user/datasets/data-s3t/versions/vers-i0n/query?query=its+sparql+mom').respond(response);
        dosageService.get(userUid, datasetUuid, datasetVersionUuid).then(function(result) {
          expect(result).toEqual([{
            unitName: 'milligram',
            unitPeriod: 'P1D',
            unitConcept: 'conceptUri'
          }, {
            unitName: 'milligram',
            unitPeriod: 'PT1H',
            unitConcept: 'conceptUri'
          }]);
          done();
        });
        httpBackend.flush();
        scope.$apply();
      });
    });

  });
});

'use strict';
define(['angular', 'angular-mocks'], function(angular, angularMocks) {
  describe('the unit service service', function() {

    var rootScope, q,
      studyServiceMock = jasmine.createSpyObj('StudyService', ['getStudy', 'getJsonGraph', 'save', 'saveJsonGraph']),
      graphDefer,
      unitService;


    beforeEach(function() {
      module('trialverse.activity', function($provide) {
        $provide.value('StudyService', studyServiceMock);
      });
    });

    beforeEach(module('trialverse.activity'));

    beforeEach(inject(function($q, $rootScope, UnitService) {
      q = $q;
      rootScope = $rootScope;

      unitService = UnitService;
      graphDefer = q.defer();
      studyServiceMock.getJsonGraph.and.returnValue(graphDefer.promise);

    }));


    describe('query units', function() {

      beforeEach(function() {
        graphDefer.resolve({
          '@graph': [{
            "@id": "http://trials.drugis.org/instances/unitUuid1",
            "@type": "ontology:Unit",
            "conversionMultiplier": "1.000000e-03",
            "label": "milligram"
          }, {
            "@id": "http://trials.drugis.org/instances/unitUuid2",
            "@type": "ontology:Unit",
            "conversionMultiplier": "1.000000e-00",
            "label": "liter"
          }]
        });
      });

      it('should return the units contained in the graph', function(done) {

        // call function under test
        unitService.queryItems().then(function(result) {
          var units = result;

          // verify query result
          expect(units.length).toBe(2);
          expect(units[0].label).toEqual('milligram');
          expect(units[1].label).toEqual('liter');

          done();
        });
        rootScope.$digest();
      });
    });



  });
});

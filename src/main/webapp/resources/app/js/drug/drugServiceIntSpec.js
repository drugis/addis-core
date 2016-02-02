'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('the drug service', function() {

    var rootScope, q,
      studyServiceMock = jasmine.createSpyObj('StudyService', ['getJsonGraph', 'saveJsonGraph']),
      drugService,
      graphDefer;

    beforeEach(function() {
      module('trialverse.activity', function($provide) {
        $provide.value('StudyService', studyServiceMock);
      });
    });

    beforeEach(angularMocks.module('trialverse.activity'));

    beforeEach(angularMocks.inject(function($q, $rootScope, DrugService) {
      q = $q;
      rootScope = $rootScope;

      graphDefer = q.defer();
      studyServiceMock.getJsonGraph.and.returnValue(graphDefer.promise);

      drugService = DrugService;
    }));


    describe('query drugs', function() {
      var jsonGraph = [{
        '@id': 'http://trials.drugis.org/instances/drugUuid1',
        '@type': 'ontology:Drug',
        label: 'Sertraline'
      }, {
        '@id': 'http://trials.drugis.org/instances/drugUuid2',
        '@type': 'ontology:Drug',
        label: 'Bupropion'
      }];

      beforeEach(function() {
        graphDefer.resolve(jsonGraph);
        rootScope.$apply();
      });

      it('should return the drugs contained in the graph', function(done) {
        drugService.queryItems().then(function(result) {
          var drugs = result;
          expect(drugs.length).toBe(2);
          expect(drugs[0].label).toEqual('Sertraline');
          expect(drugs[1].label).toEqual('Bupropion');
          done();
        });
        rootScope.$apply();
      });
    });

  });
});

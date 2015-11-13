'use strict';
define(['angular', 'angular-mocks'], function(angular, angularMocks) {
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

    beforeEach(module('trialverse.activity'));

    beforeEach(inject(function($q, $rootScope, DrugService) {
      q = $q;
      rootScope = $rootScope;

      graphDefer = q.defer();
      studyServiceMock.getJsonGraph.and.returnValue(graphDefer.promise);

      drugService = DrugService;
    }));


    describe('query drugs', function() {
      var jsonGraph = {
        '@graph': [{
          '@id': 'http://trials.drugis.org/instances/drugUuid1',
          '@type': 'ontology:Drug',
          label: 'Sertraline'
        }, {
          '@id': 'http://trials.drugis.org/instances/drugUuid2',
          '@type': 'ontology:Drug',
          label: 'Bupropion'
        }]
      }

      beforeEach(function() {
        graphDefer.resolve(jsonGraph);
        rootScope.$apply();
      });

      it('should return the drugs contained in the graph', function(done) {
        // call function under test
        drugService.queryItems().then(function(result) {
          var drugs = result;

          // verify query result
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

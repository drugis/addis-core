'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('study service', function() {

    var remoteRdfStoreService, conceptService, scope, httpBackend,
      loadDefer, createDefer, executeQueryDefer;

    beforeEach(module('trialverse', function($provide) {
      remoteRdfStoreService = jasmine.createSpyObj('RemoteRdfStoreService', ['create', 'load', 'executeUpdate', 'executeQuery']);
      $provide.value('RemoteRdfStoreService', remoteRdfStoreService);
    }));

    beforeEach(inject(function($rootScope, $q, $httpBackend, ConceptService) {
      scope = $rootScope;
      httpBackend = $httpBackend;
      loadDefer = $q.defer();
      createDefer = $q.defer();
      executeQueryDefer = $q.defer();

      testUtils.loadTemplate('queryConcepts.sparql', $httpBackend);
      testUtils.loadTemplate('addConcept.sparql', $httpBackend);

      remoteRdfStoreService.create.and.returnValue(createDefer.promise);
      remoteRdfStoreService.load.and.returnValue(loadDefer.promise);
      // remoteRdfStoreService.executeUpdate.and.returnValue(executeUpdateDefer.promise);
      remoteRdfStoreService.executeQuery.and.returnValue(executeQueryDefer.promise);
      conceptService = ConceptService;
    }));


    describe('after loading the store', function() {

      it('the load promise should be resolved', function() {
        var loadPromise = conceptService.loadStore('data');
        createDefer.resolve();
        loadDefer.resolve();

        scope.$digest();

        expect(loadPromise.$$state.status).toBe(1);
      });

      describe('queryItems', function() {
        xit('should request data from the remote store', function() {
          var data = ['foo'];

          var expected = {
            $$state: {
              status: 1,
              value: 'foo'
            }
          };
          var queryResult = conceptService.queryItems();
          executeQueryDefer.resolve(data);
          scope.$digest();

          expect(queryResult).toEqual(expected);

        });
      });
    });
  });
});

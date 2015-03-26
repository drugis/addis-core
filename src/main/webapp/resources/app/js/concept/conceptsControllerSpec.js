'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the concepts controller', function() {

    var
      scope, httpBackend,
      datasetUuid = 'datasetUuid',
      stateParamsMock = {
        datasetUUID: datasetUuid
      },
      loadConceptStoreDefer,
      queryItemsDefer,
      loadDatasetStoreDefer,
      modalMock = jasmine.createSpyObj('$modal', ['open']),
      conceptServiceMock = jasmine.createSpyObj('ConceptService', ['loadStore', 'queryItems']);

    beforeEach(module('trialverse.concept'));

    beforeEach(inject(function($rootScope, $q, $httpBackend, $controller, GraphResource) {
      scope = $rootScope;
      httpBackend = $httpBackend;

      httpBackend.expectGET('/datasets/' + datasetUuid + '/graphs/concepts').respond('concepts');

      loadConceptStoreDefer = $q.defer();
      queryItemsDefer = $q.defer();

      conceptServiceMock.loadStore.and.returnValue(loadConceptStoreDefer.promise);
      conceptServiceMock.queryItems.and.returnValue(queryItemsDefer.promise);

      $controller('ConceptController', {
        $scope: scope,
        $modal: modalMock,
        $stateParams: stateParamsMock,
        ConceptService: conceptServiceMock,
        GraphResource: GraphResource,
        CONCEPT_GRAPH_UUID: 'CONCEPT_GRAPH_UUID'
      });
    }));

    describe('on load', function() {
      it('should place the concepts on the scope', function() {
        var conceptResult ='anyValue2';

        expect(scope.concepts).toBeDefined();
        httpBackend.flush();
        expect(conceptServiceMock.loadStore).toHaveBeenCalled();
        loadConceptStoreDefer.resolve('anyValue');
        scope.$digest();
        expect(conceptServiceMock.queryItems).toHaveBeenCalled();
        queryItemsDefer.resolve(conceptResult);
        scope.$digest();
        expect(scope.concepts).toBe(conceptResult);
      });
    });

  });
});

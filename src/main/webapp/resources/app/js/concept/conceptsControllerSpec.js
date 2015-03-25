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
      datasetQueryDefer,
      modalMock = jasmine.createSpyObj('$modal', ['open']),
      datasetServiceMock = jasmine.createSpyObj('DatasetService', ['loadStore', 'queryDataset', 'reset']),
      datasetResourceMock = jasmine.createSpyObj('DatasetResource', ['get']),
      conceptServiceMock = jasmine.createSpyObj('ConceptService', ['loadStore', 'queryItems']);

    beforeEach(module('trialverse.concept'));

    beforeEach(inject(function($rootScope, $q, $httpBackend, $controller, DatasetResource, GraphResource) {
      scope = $rootScope;
      httpBackend = $httpBackend;

      httpBackend.expectGET('/datasets/' + datasetUuid ).respond('dataset');
      httpBackend.expectGET('/datasets/' + datasetUuid + '/graphs/concepts').respond('concepts');

      loadConceptStoreDefer = $q.defer();
      queryItemsDefer = $q.defer();
      loadDatasetStoreDefer = $q.defer();
      datasetQueryDefer = $q.defer();

      conceptServiceMock.loadStore.and.returnValue(loadConceptStoreDefer.promise);
      conceptServiceMock.queryItems.and.returnValue(queryItemsDefer.promise);

      datasetServiceMock.loadStore.and.returnValue(loadDatasetStoreDefer.promise);
      datasetServiceMock.queryDataset.and.returnValue(datasetQueryDefer.promise);

      $controller('ConceptController', {
        $scope: scope,
        $modal: modalMock,
        $stateParams: stateParamsMock,
        DatasetService: datasetServiceMock,
        DatasetResource: DatasetResource,
        ConceptService: conceptServiceMock,
        GraphResource: GraphResource,
        CONCEPT_GRAPH_UUID: 'CONCEPT_GRAPH_UUID'
      });
    }));

    describe('on load', function() {
      it('should place the concepts and dataset on the scope', function() {
        var conceptResult ='anyValue2';
        var datasetResult = [{label: 'dataset label'}];

        expect(scope.concepts).toBeDefined();
        httpBackend.flush();
        expect(conceptServiceMock.loadStore).toHaveBeenCalled();
        expect(datasetServiceMock.loadStore).toHaveBeenCalled();
        loadConceptStoreDefer.resolve('anyValue');
        loadDatasetStoreDefer.resolve('anyValue');
        scope.$digest();
        expect(conceptServiceMock.queryItems).toHaveBeenCalled();
        expect(datasetServiceMock.queryDataset).toHaveBeenCalled();
        queryItemsDefer.resolve(conceptResult);
        datasetQueryDefer.resolve(datasetResult);
        scope.$digest();
        expect(scope.concepts).toBe(conceptResult);
        expect(scope.dataset).toBe(datasetResult[0]);
      });
    });

  });
});

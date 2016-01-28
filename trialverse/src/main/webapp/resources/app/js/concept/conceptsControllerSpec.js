'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the concepts controller', function() {

    var
      scope, httpBackend,
      datasetUuid = 'datasetUuid',
      versionUuid = 'versionUuid',
      stateParamsMock = {
        datasetUUID: datasetUuid,
        versionUuid: versionUuid
      },
      loadConceptStoreDefer,
      queryItemsDefer,
      loadDatasetStoreDefer,
      modalMock = jasmine.createSpyObj('$modal', ['open']),
      conceptServiceMock = jasmine.createSpyObj('ConceptService', ['loadStore', 'queryItems']),
      datasetConceptDefer;

    beforeEach(module('trialverse.concept'));

    beforeEach(inject(function($rootScope, $q, $httpBackend, $controller, VersionedGraphResource) {
      scope = $rootScope;

      // mock parent scope
      datasetConceptDefer = $q.defer();
      scope.datasetConcepts = datasetConceptDefer.promise; 
      datasetConceptDefer.resolve('anyValue2');
      httpBackend = $httpBackend;

      loadConceptStoreDefer = $q.defer();
      queryItemsDefer = $q.defer();

      conceptServiceMock.loadStore.and.returnValue(loadConceptStoreDefer.promise);
      conceptServiceMock.queryItems.and.returnValue(queryItemsDefer.promise);

      $controller('ConceptController', {
        $scope: scope,
        $modal: modalMock,
        $stateParams: stateParamsMock,
        ConceptService: conceptServiceMock,
        VersionedGraphResource: VersionedGraphResource,
        CONCEPT_GRAPH_UUID: 'CONCEPT_GRAPH_UUID'
      });

      scope.$apply();
    }));

    describe('on load', function() {
      it('should place the concepts on the scope', function() {
        var conceptResult ='anyValue2';

        expect(scope.concepts).toBeDefined();
        expect(scope.concepts).toBe(conceptResult);
      });
    });

  });
});

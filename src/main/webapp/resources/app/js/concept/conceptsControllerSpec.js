'use strict';
define(['angular-mocks', './concept'], function() {
  describe('the concepts controller', function() {

    var
      scope,
      datasetUuid = 'datasetUuid',
      versionUuid = 'versionUuid',
      stateParamsMock = {
        datasetUuid: datasetUuid,
        versionUuid: versionUuid
      },
      loadConceptStoreDefer,
      queryItemsDefer,
      modalMock = jasmine.createSpyObj('$modal', ['open']),
      conceptsServiceMock = jasmine.createSpyObj('ConceptsService', ['loadStore', 'queryItems']),
      pageTitleServiceMock = jasmine.createSpyObj('PageTitleService', ['setPageTitle']),
      datasetConceptDefer;

    beforeEach(angular.mock.module('trialverse.concept'));

    beforeEach(inject(function($rootScope, $q, $controller, VersionedGraphResource) {
      scope = $rootScope;

      // mock parent scope
      datasetConceptDefer = $q.defer();
      scope.datasetConcepts = datasetConceptDefer.promise;
      datasetConceptDefer.resolve('anyValue2');

      loadConceptStoreDefer = $q.defer();
      queryItemsDefer = $q.defer();

      conceptsServiceMock.loadStore.and.returnValue(loadConceptStoreDefer.promise);
      conceptsServiceMock.queryItems.and.returnValue(queryItemsDefer.promise);

      $controller('ConceptsController', {
        $scope: scope,
        $modal: modalMock,
        $stateParams: stateParamsMock,
        ConceptsService: conceptsServiceMock,
        VersionedGraphResource: VersionedGraphResource,
        CONCEPT_GRAPH_UUID: 'CONCEPT_GRAPH_UUID',
        PageTitleService: pageTitleServiceMock
      });

      scope.$apply();
    }));

    describe('on load', function() {
      it('should place the concepts on the scope', function(done) {
        var conceptResult = 'anyValue2';
        queryItemsDefer.resolve(conceptResult);
        queryItemsDefer.promise.then(function() {

          expect(scope.concepts).toBeDefined();
          expect(scope.concepts).toBe(conceptResult);
          done();
        });
        scope.$apply();
      });
    });

  });
});

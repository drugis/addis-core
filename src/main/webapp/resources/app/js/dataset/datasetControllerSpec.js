'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('the dataset controller', function() {

    var scope, httpBackend,
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      studiesWithDetailsService = jasmine.createSpyObj('StudiesWithDetailsService', ['get']),
      historyResource = jasmine.createSpyObj('HistoryResource', ['query']),
      conceptService = jasmine.createSpyObj('ConceptService', ['loadJson', 'queryItems']),
      versionedGraphResource = jasmine.createSpyObj('VersionedGraphResource', ['get', 'getConceptJson']),
      datasetResource = jasmine.createSpyObj('DatasetResource', ['getForJson']),
      datasetVersionedResource = jasmine.createSpyObj('DatasetVersionedResource', ['getForJson']),
      datasetDeferred,
      queryHistoryDeferred,
      studiesWithDetailsGetDeferred,
      mockQueryDatasetDeferred,
      conceptsJsonDefer,
      mockStudiesWithDetail = {
        '@graph': {}
      },
      userUid = 'userUid',
      datasetUUID = 'uuid-1',
      versionUuid = 'version-1',
      stateParams = {
        userUid: userUid,
        datasetUUID: datasetUUID,
        versionUuid: versionUuid
      };

    beforeEach(angularMocks.module('trialverse.dataset'));

    beforeEach(angularMocks.inject(function($rootScope, $q, $controller, $httpBackend) {
      scope = $rootScope;
      httpBackend = $httpBackend;

      mockQueryDatasetDeferred = $q.defer();
      studiesWithDetailsGetDeferred = $q.defer();
      queryHistoryDeferred = $q.defer();
      conceptsJsonDefer = $q.defer();
      datasetDeferred = $q.defer();

      studiesWithDetailsService.get.and.returnValue(studiesWithDetailsGetDeferred.promise);
      conceptService.loadJson.and.returnValue(conceptsJsonDefer.promise);
      versionedGraphResource.getConceptJson.and.returnValue({
        $promise: conceptsJsonDefer.promise
      });
      historyResource.query.and.returnValue({
        $promise: queryHistoryDeferred.promise
      });
      datasetVersionedResource.getForJson.and.returnValue({
        $promise: datasetDeferred.promise
      });
      datasetResource.getForJson.and.returnValue({
        $promise: datasetDeferred.promise
      });

      mockModal.open.calls.reset();

      var windowMock = {
        config: {
          user: {
            userEmail: 'foo@bar.google'
          }
        }
      };

      $controller('DatasetController', {
        $scope: scope,
        $window: windowMock,
        $stateParams: stateParams,
        $modal: mockModal,
        DatasetVersionedResource: datasetVersionedResource,
        DatasetResource: datasetResource,
        StudiesWithDetailsService: studiesWithDetailsService,
        HistoryResource: historyResource,
        ConceptService: conceptService,
        VersionedGraphResource: versionedGraphResource
      });

    }));

    describe('on load', function() {

      beforeEach(function() {
        datasetDeferred.resolve({
          'http://purl.org/dc/terms/title': 'title',
          'http://purl.org/dc/terms/description': 'description',
          'http://purl.org/dc/terms/creator': 'creator'
        });
      });

      it('should get the dataset and place its properties on the scope', function() {
        scope.$digest();
        expect(datasetVersionedResource.getForJson).toHaveBeenCalled();
        expect(scope.dataset).toEqual({
          datasetUri: 'uuid-1',
          label: 'title',
          comment: 'description',
          creator: 'creator'
        });

      });

      it('should get the studies with detail and place them on the scope', function() {
        studiesWithDetailsGetDeferred.resolve(mockStudiesWithDetail);
        scope.$digest();
        expect(scope.studiesWithDetail).toBe(mockStudiesWithDetail);
      });

      it('should place the table options on the scope', function() {
        expect(scope.tableOptions.columns[0].label).toEqual('Title');
      });

      it('should place the current revision on the scope', function() {
        var historyItems = [{
          'uri': 'http://uri/version-1',
          i: 0
        }];
        queryHistoryDeferred.resolve(historyItems);
        scope.$digest();
        expect(scope.currentRevision).toBeDefined();
      });

      it('should place the concepts on the scope', function() {
        var datasetConcepts = [{
          label: 'concept 1'
        }];
        conceptsJsonDefer.resolve(datasetConcepts);
        scope.$digest();
        expect(scope.datasetConcepts).toBeDefined(); // promise resolved
        expect(versionedGraphResource.getConceptJson).toHaveBeenCalled();
        expect(conceptService.loadJson).toHaveBeenCalled();
      });

    });

    describe('on load for a head view', function() {
      beforeEach(angularMocks.inject(function($controller) {
        $controller('DatasetController', {
          $scope: scope,
          $window: {},
          $stateParams: {},
          $modal: mockModal,
          DatasetVersionedResource: datasetVersionedResource,
          DatasetResource: datasetResource,
          StudiesWithDetailsService: studiesWithDetailsService,
          HistoryResource: historyResource,
          ConceptService: conceptService,
          VersionedGraphResource: versionedGraphResource
        });
      }));
      it('should get the datasetusing the non versioned resource ', function() {
        expect(datasetResource.getForJson).toHaveBeenCalled();
      });
    });

    describe('showTableOptions', function() {
      it('should open a modal', function() {
        scope.showTableOptions();

        expect(mockModal.open).toHaveBeenCalled();
      });
    });

    describe('showStudyDialog', function() {
      it('should open a modal', function() {
        scope.showStudyDialog();
        expect(mockModal.open).toHaveBeenCalled();
      });
    });

  });

});

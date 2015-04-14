'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the dataset controller', function() {

    var scope, httpBackend,
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      mockDatasetService = jasmine.createSpyObj('DatasetService', ['loadStore', 'queryDataset', 'reset']),
      mockJsonLDService = jasmine.createSpyObj('JsonLdService', ['rewriteAtIds']),
      mockRemoteRdfStoreService = jasmine.createSpyObj('RemoteRdfStoreService', ['deFusekify']),
      studiesWithDetailsService = jasmine.createSpyObj('StudiesWithDetailsService', ['get']),
      historyResource = jasmine.createSpyObj('HistoryResource', ['query']),
      historyService = jasmine.createSpyObj('HistoryService', ['addOrderIndex']),
      mockLoadStoreDeferred,
      queryHistoryDeferred,
      studiesWithDetailsGetDeferred,
      mockQueryDatasetDeferred,
      mockStudiesWithDetail = {
        '@graph': {}
      },
      datasetUUID = 'uuid-1',
      versionUuid = 'version-1',
      stateParams = {
        datasetUUID: datasetUUID,
        versionUuid: versionUuid
      };


    // encode query like angualr does for test use, http://tools.ietf.org/html/rfc3986
    function encodeUriQuery(val) {
      return encodeURIComponent(val).
      replace(/%40/gi, '@').
      replace(/%3A/gi, ':').
      replace(/%3B/gi, ';').
      replace(/%24/g, '$').
      replace(/%2C/gi, ',').
      replace(/%20/g, '+');
    }

    beforeEach(module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, DatasetVersionedResource) {
      scope = $rootScope;
      httpBackend = $httpBackend;

      mockLoadStoreDeferred = $q.defer();
      mockQueryDatasetDeferred = $q.defer();
      studiesWithDetailsGetDeferred = $q.defer();
      queryHistoryDeferred = $q.defer();

      mockDatasetService.loadStore.and.returnValue(mockLoadStoreDeferred.promise);
      mockDatasetService.queryDataset.and.returnValue(mockQueryDatasetDeferred.promise);
      studiesWithDetailsService.get.and.returnValue(studiesWithDetailsGetDeferred.promise);
      mockRemoteRdfStoreService.deFusekify.and.returnValue(mockStudiesWithDetail);
      historyResource.query.and.returnValue({
        $promise: queryHistoryDeferred.promise
      });
      historyService.addOrderIndex.and.returnValue([{
        '@id': 'http://host/versions/' + versionUuid,
        idx: 0
      }]);

      mockModal.open.calls.reset();
      httpBackend.expectGET('/datasets/' + datasetUUID + '/versions/' + versionUuid).respond('dataset');

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
        DatasetService: mockDatasetService,
        DatasetVersionedResource: DatasetVersionedResource,
        StudiesWithDetailsService: studiesWithDetailsService,
        JsonLdService: mockJsonLDService,
        RemoteRdfStoreService: mockRemoteRdfStoreService,
        HistoryResource: historyResource,
        HistoryService: historyService
      });

    }));

    describe('on load', function() {
      it('should get the dataset and place its properties on the scope', function() {
        var mockDataset = [{
          mock: 'object'
        }];
        httpBackend.flush();
        expect(mockDatasetService.reset).toHaveBeenCalled();
        expect(mockDatasetService.loadStore).toHaveBeenCalled();
        mockLoadStoreDeferred.resolve();
        scope.$digest();
        expect(mockDatasetService.queryDataset).toHaveBeenCalled();
        mockQueryDatasetDeferred.resolve(mockDataset);
        scope.$digest();
        expect(scope.dataset).toEqual({
          mock: 'object',
          uuid: 'uuid-1'
        });
        expect(scope.dataset.uuid).toBe(stateParams.datasetUUID);
        expect(studiesWithDetailsService.get).toHaveBeenCalled();
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
          '@id': 'http://host/versions/' + versionUuid
        }];
        queryHistoryDeferred.resolve(historyItems);
        scope.$digest();
        expect(scope.currentRevision).toBeDefined();
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
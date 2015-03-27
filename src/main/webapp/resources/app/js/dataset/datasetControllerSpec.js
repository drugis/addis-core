'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('the dataset controller', function() {

    var scope, httpBackend,
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      mockDatasetService = jasmine.createSpyObj('DatasetService', ['loadStore', 'queryDataset', 'reset']),
      mockJsonLDService = jasmine.createSpyObj('JsonLdService', ['rewriteAtIds']),
      mockRemoteRdfStoreService = jasmine.createSpyObj('RemoteRdfStoreService', ['deFusekify']),
      mockLoadStoreDeferred,
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
    function encodeUriQuery(val, pctEncodeSpaces) {
      return encodeURIComponent(val).
      replace(/%40/gi, '@').
      replace(/%3A/gi, ':').
      replace(/%3B/gi, ';').
      replace(/%24/g, '$').
      replace(/%2C/gi, ',').
      replace(/%20/g, '+'));
    }

    beforeEach(module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, DatasetVersionedResource, StudiesWithDetailsService) {
      scope = $rootScope;
      httpBackend = $httpBackend;

      mockLoadStoreDeferred = $q.defer();
      mockQueryDatasetDeferred = $q.defer();

      mockDatasetService.loadStore.and.returnValue(mockLoadStoreDeferred.promise);
      mockDatasetService.queryDataset.and.returnValue(mockQueryDatasetDeferred.promise);

      mockRemoteRdfStoreService.deFusekify.and.returnValue(mockStudiesWithDetail);

      mockModal.open.calls.reset();


      var query = testUtils.loadTemplate('queryStudiesWithDetails.sparql', httpBackend);
      httpBackend.expectGET('/datasets/' + datasetUUID + '/versions/' + versionUuid).respond('dataset');
      httpBackend.expectGET('/datasets/' + datasetUUID + '/versions/' + versionUuid + '/query?query=' + encodeUriQuery(query)).respond(mockStudiesWithDetail);

      $controller('DatasetController', {
        $scope: scope,
        $stateParams: stateParams,
        $modal: mockModal,
        DatasetService: mockDatasetService,
        DatasetVersionedResource: DatasetVersionedResource,
        StudiesWithDetailsService: StudiesWithDetailsService,
        JsonLdService: mockJsonLDService,
        RemoteRdfStoreService: mockRemoteRdfStoreService
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
      });

      it('should get the studies with detail and place them on the scope', function() {
        httpBackend.flush();
        expect(scope.studiesWithDetail).toBe(mockStudiesWithDetail);
      });

      it('should place the table options on the scope', function() {
        expect(scope.tableOptions.columns[0].label).toEqual('Title');
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
      })
    });

  });

});
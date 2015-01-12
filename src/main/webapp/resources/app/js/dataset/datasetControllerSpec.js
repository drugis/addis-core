'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the dataset controller', function() {

    var scope, httpBackend,
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      mockDatasetService = jasmine.createSpyObj('DatasetService', ['loadStore', 'queryDataset']),
      mockJsonLDService = jasmine.createSpyObj('JsonLdService', ['rewriteAtIds']),
      mockLoadStoreDeferred,
      mockQueryDatasetDeferred,
      mockStudiesWithDetail = {
        '@graph' : {}
      },
      stateParams = {
        datasetUUID: 'uuid-1'
      };

    beforeEach(module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, DatasetResource, StudiesWithDetailResource) {
      scope = $rootScope;
      httpBackend = $httpBackend;

      mockLoadStoreDeferred = $q.defer();
      mockQueryDatasetDeferred = $q.defer();

      mockDatasetService.loadStore.and.returnValue(mockLoadStoreDeferred.promise);
      mockDatasetService.queryDataset.and.returnValue(mockQueryDatasetDeferred.promise);

      mockJsonLDService.rewriteAtIds.and.returnValue(mockStudiesWithDetail);

      mockModal.open.calls.reset();

      httpBackend.expectGET('/datasets/' + stateParams.datasetUUID).respond('dataset');
      httpBackend.expectGET('/datasets/' + stateParams.datasetUUID + '/studiesWithDetail').respond(mockStudiesWithDetail);

      $controller('DatasetController', {
        $scope: scope,
        $stateParams: stateParams,
        $modal: mockModal,
        DatasetService: mockDatasetService,
        DatasetResource: DatasetResource,
        StudiesWithDetailResource: StudiesWithDetailResource,
        JsonLdService: mockJsonLDService
      });

    }));

    describe('on load', function() {
      it('should get the dataset and place its properties on the scope', function() {
        var mockDataset = {
          data: {
            results: {
              bindings: [{mock: 'object'}]
            }
          }
        };
        httpBackend.flush();
        expect(mockDatasetService.loadStore).toHaveBeenCalled();
        mockLoadStoreDeferred.resolve();
        scope.$digest();
        expect(mockDatasetService.queryDataset).toHaveBeenCalled();
        mockQueryDatasetDeferred.resolve(mockDataset);
        scope.$digest();
        expect(scope.dataset).toEqual({mock: 'object', uuid: 'uuid-1'});
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

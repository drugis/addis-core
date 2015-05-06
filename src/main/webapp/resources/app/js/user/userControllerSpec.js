'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('user controller', function() {

    var scope, httpBackend,
      userHash = 'userHash',
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      mockDatasetService = jasmine.createSpyObj('DatasetService', ['loadStore', 'queryDatasetsOverview', 'reset']),
      md5Mock = jasmine.createSpyObj('md5', ['createHash']),
      mockLoadStoreDeferred,
      mockQueryDatasetsDeferred,
      users = [{user:'user1'}, {user:'user2'}],
      datasets = [{title: 'my results', headVersion: 'http://host/versions/foo'}];

    beforeEach(module('trialverse.user'));


    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, DatasetResource) {
      scope = $rootScope;
      httpBackend = $httpBackend;

      mockLoadStoreDeferred = $q.defer();
      mockQueryDatasetsDeferred = $q.defer();

      mockDatasetService.loadStore.and.returnValue(mockLoadStoreDeferred.promise);
      mockDatasetService.queryDatasetsOverview.and.returnValue(mockQueryDatasetsDeferred.promise);
      md5Mock.createHash.and.returnValue(userHash);

      httpBackend.expectGET('/users').respond(users);
      httpBackend.expectGET('/users/' + userHash + '/datasets').respond(datasets);

      $controller('UserController', {
        $scope: scope,
        $modal: mockModal,
        $stateParams: {userUid: userHash},
        $window: {config: {user: {userNameHash: userHash}}},
        DatasetResource: DatasetResource,
        DatasetService: mockDatasetService,
        md5: md5Mock
      });

    }));

    it('should place createDatasetDialog on the scope', function() {
      expect(scope.createDatasetDialog).not.toBe(null);
    });

    it('should query the datasetVersionedResource, process the results and place them on the scope', function() {

      httpBackend.flush();

      expect(mockDatasetService.loadStore).toHaveBeenCalled();
      mockLoadStoreDeferred.resolve(101);
      scope.$digest();
      expect(mockDatasetService.queryDatasetsOverview).toHaveBeenCalled();
      mockQueryDatasetsDeferred.resolve(datasets);
      scope.$digest();
      expect(scope.datasets).toBe(datasets);
    });

    describe('createDatasetDialog', function() {
      it('should open a modal', function() {
        scope.createDatasetDialog();
        expect(mockModal.open).toHaveBeenCalled();
      });
    });

  });
});

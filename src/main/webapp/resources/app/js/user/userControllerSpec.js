'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('user controller', function() {

    var scope, httpBackend,
      userHash = 'userHash',
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      md5Mock = jasmine.createSpyObj('md5', ['createHash']),
      mockLoadStoreDeferred,
      mockQueryDatasetsDeferred,
      users = [{user:'user1'}, {user:'user2'}],
      datasets = [{title: 'my results', headVersion: 'http://host/versions/foo'}];

    beforeEach(module('trialverse.user'));


    beforeEach(angularMocks.inject(function($rootScope, $q, $controller, $httpBackend, DatasetResource) {
      scope = $rootScope;
      httpBackend = $httpBackend;

      mockLoadStoreDeferred = $q.defer();
      mockQueryDatasetsDeferred = $q.defer();

      md5Mock.createHash.and.returnValue(userHash);

      httpBackend.expectGET('/users').respond(users);
      httpBackend.expectGET('/users/' + userHash + '/datasets').respond(datasets);

      $controller('UserController', {
        $scope: scope,
        $modal: mockModal,
        $stateParams: {userUid: userHash},
        $window: {config: {user: {userNameHash: userHash}}},
        DatasetResource: DatasetResource,
        md5: md5Mock
      });

    }));

    it('should place createDatasetDialog on the scope', function() {
      expect(scope.createDatasetDialog).not.toBe(null);
    });


    describe('createDatasetDialog', function() {
      it('should open a modal', function() {
        scope.createDatasetDialog();
        expect(mockModal.open).toHaveBeenCalled();
      });
    });

  });
});

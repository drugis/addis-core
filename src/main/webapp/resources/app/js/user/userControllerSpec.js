'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('user controller', function() {

    var scope, httpBackend,
      userId = 3,
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      md5Mock = jasmine.createSpyObj('md5', ['createHash']),
      mockLoadStoreDeferred,
      mockQueryDatasetsDeferred,
      users = [{user:'user1'}, {user:'user2'}],
      datasets = [{title: 'my results', headVersion: 'http://host/versions/foo'}];

    beforeEach(angular.mock.module('trialverse.user'));


    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, DatasetResource) {
      scope = $rootScope;
      httpBackend = $httpBackend;

      mockLoadStoreDeferred = $q.defer();
      mockQueryDatasetsDeferred = $q.defer();

      md5Mock.createHash.and.returnValue(userId);

      httpBackend.expectGET('/users').respond(users);
      httpBackend.expectGET('/users/' + userId + '/datasets').respond(datasets);

      $controller('UserController', {
        $scope: scope,
        $modal: mockModal,
        $stateParams: {userUid: userId},
        $window: {config: {user: {id: userId}}},
        DatasetResource: DatasetResource,
        md5: md5Mock
      });

    }));

  });
});

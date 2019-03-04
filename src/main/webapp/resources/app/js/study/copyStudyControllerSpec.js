'use strict';
define(['angular', 'angular-mocks'], function(angular, angularMocks) {
  describe('copy study controller', function() {

    var scope,
      modalInstanceMock = jasmine.createSpyObj('$mockInstance', ['close', 'dismiss']),
      datasetsMock = ['dataset 1'],
      userUidMock = 'userUidMock',
      datasetUuidMock = 'datasetUuid',
      graphUuidMock = 'graphUuid',
      versionUuidMock = 'versionUuid',
      copyDefer,
      copyPromise,
      copyStudyResourceMock = jasmine.createSpyObj('CopyStudyResource', ['copy']),
      uuidServiceMock = jasmine.createSpyObj('UUIDService', ['generate', 'buildGraphUri'])
      ;
    beforeEach(angular.mock.module('trialverse.study'));

    beforeEach(inject(function($rootScope, $q, $controller) {
      scope = $rootScope;

      copyDefer = $q.defer();
      copyPromise = {
        '$promise': copyDefer.promise
      };
      copyStudyResourceMock.copy.and.returnValue(copyPromise);

      $controller('CopyStudyController', {
        $scope: scope,
        $modalInstance: modalInstanceMock,
        datasets: datasetsMock,
        userUid: userUidMock,
        datasetUuid: datasetUuidMock,
        graphUuid: graphUuidMock,
        versionUuid: versionUuidMock,
        CopyStudyResource: copyStudyResourceMock,
        UUIDService: uuidServiceMock
      });

    }));

    describe('copyStudy', function() {
      it('should be on the scope', function() {
        expect(scope.copyStudy).toBeDefined();
      });
      it('should create a copy message and pass it to the copystudyresource', function() {
        var targetDataset = {
          uri: 'http://testhost/datasets/datasetUuid'
        };
        var sourceGraphUri = 'http://testhost/datasets/datasetUuid/versions/versionUuid/graphs/graphUuid';
        var targetGraphUuid = 'targetGraphUuid';

        uuidServiceMock.generate.and.returnValue(targetGraphUuid);
        uuidServiceMock.buildGraphUri.and.returnValue(sourceGraphUri);

        scope.copyStudy(targetDataset);

        expect(copyStudyResourceMock.copy).toHaveBeenCalled();
        copyDefer.resolve();
        scope.$apply();
      });
    });
  });
});

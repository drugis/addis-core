'use strict';
define(['angular', 'angular-mocks'], function(angular) {
  describe('CopyStudyController', function() {

    var scope;
    var  modalInstanceMock = jasmine.createSpyObj('$mockInstance', ['close', 'dismiss']);
    var  datasetsMock = ['dataset 1'];
    var  loggedInUserIdMock = 'loggedInUserIdMock';
    var  datasetUuidMock = 'datasetUuid';
    var  graphUuidMock = 'graphUuid';
    var  versionUuidMock = 'versionUuid';
    var  copyDefer;
    var  copyPromise;
    var  copyStudyResourceMock = jasmine.createSpyObj('CopyStudyResource', ['copy']);
    var  uuidServiceMock = jasmine.createSpyObj('UUIDService', ['generate', 'buildGraphUri']);
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
        loggedInUserId: loggedInUserIdMock,
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

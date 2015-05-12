'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the study controller', function() {

    var scope, httpBackend,
      userUid = 'userUid',
      datasetUUID = 'datasetUUID',
      studyGraphUuid = 'studyGraphUuid',
      versionUuid = 'versionUuid',
      mockStateParams = {
        userUid: userUid,
        datasetUUID: datasetUUID,
        studyGraphUuid: studyGraphUuid,
        versionUuid: versionUuid
      },
      anchorScrollMock = jasmine.createSpy('anchorScroll'),
      locationMock = jasmine.createSpyObj('location', ['hash']),
      modalMock = jasmine.createSpyObj('modal', ['open']),
      studyServiceMock = jasmine.createSpyObj('StudyService', ['reset', 'queryArmData', 'loadStore', 'queryStudyData', 'getGraph', 'studySaved']),
      resultsServiceMock = jasmine.createSpyObj('ResultsService', ['cleanUpMeasurements']),
      studyDesignServiceMock = jasmine.createSpyObj('StudyDesignService', ['cleanupCoordinates']),
      loadStoreDeferred,
      queryStudyDataDeferred,
      queryArmDataDeferred,
      getGraphDeferred;


    beforeEach(module('trialverse.study'));

    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, VersionedGraphResource) {

      scope = $rootScope;
      httpBackend = $httpBackend;

      httpBackend.expectGET('/users/' + userUid + '/datasets/' + datasetUUID + '/versions/' + versionUuid + '/graphs/' + studyGraphUuid).respond('study');

      loadStoreDeferred = $q.defer();
      queryStudyDataDeferred = $q.defer();
      queryArmDataDeferred = $q.defer();
      getGraphDeferred = $q.defer();

      studyServiceMock.loadStore.and.returnValue(loadStoreDeferred.promise);
      studyServiceMock.queryStudyData.and.returnValue(queryStudyDataDeferred.promise);
      studyServiceMock.queryArmData.and.returnValue(queryArmDataDeferred.promise);
      studyServiceMock.getGraph.and.returnValue(getGraphDeferred.promise);

      $controller('StudyController', {
        $scope: scope,
        $stateParams: mockStateParams,
        VersionedGraphResource: VersionedGraphResource,
        $location: locationMock,
        $anchorScroll: anchorScrollMock,
        $modal: modalMock,
        $window: {
          bind: 'mockBind',
          innerHeight: 'innerHeightMock'
        },
        StudyService: studyServiceMock,
        ResultsService: resultsServiceMock,
        StudyDesignService: studyDesignServiceMock
      });
    }));

    describe('on load', function() {

      it('should place study on the scope', function() {
        var
          studyQueryResult = 'studyQueryResult',
          armQueryResult = 'armQueryResult';

        expect(scope.study).toBeDefined();
        httpBackend.flush();
        expect(studyServiceMock.loadStore).toHaveBeenCalled();
        loadStoreDeferred.resolve(3);
        scope.$digest();
        expect(studyServiceMock.queryStudyData).toHaveBeenCalled();
        queryStudyDataDeferred.resolve(studyQueryResult);
        scope.$digest();
        expect(scope.study).toBe(studyQueryResult);
      });

    });

  });
});
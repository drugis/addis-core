'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the study controller', function() {

    var scope, httpBackend,
      datasetUUID = 'datasetUUID',
      studyUUID = 'studyUUID',
      mockStateParams = {
        datasetUUID: datasetUUID,
        studyUUID: studyUUID
      },
      anchorScrollMock = jasmine.createSpy('anchorScroll'),
      locationMock = jasmine.createSpyObj('location', ['hash']),
      modalMock = jasmine.createSpyObj('modal', ['open']),
      studyServiceMock = jasmine.createSpyObj('StudyService', ['reset','queryArmData', 'loadStore', 'queryStudyData', 'getGraph', 'studySaved']),
      datasetServiceMock = jasmine.createSpyObj('DatasetService', ['reset', 'loadStore', 'queryDataset']),
      resultsServiceMock = jasmine.createSpyObj('ResultsService', ['cleanUpMeasurements']),
      studyDesignServiceMock = jasmine.createSpyObj('StudyDesignService', ['cleanupCoordinates']),
      loadStoreDeferred,
      loadDatasetStoreDeferred,
      queryStudyDataDeferred,
      queryArmDataDeferred,
      getGraphDeferred;


    beforeEach(module('trialverse.study'));

    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, GraphResource, DatasetResource) {

      scope = $rootScope;
      httpBackend = $httpBackend;

      httpBackend.expectGET('/datasets/' + datasetUUID + '/graphs/' + studyUUID).respond('study');
      httpBackend.expectGET('/datasets/datasetUUID?studyUUID=studyUUID').respond('dataset');

      loadStoreDeferred = $q.defer();
      queryStudyDataDeferred = $q.defer();
      queryArmDataDeferred = $q.defer();
      getGraphDeferred = $q.defer();
      loadDatasetStoreDeferred = $q.defer();

      studyServiceMock.loadStore.and.returnValue(loadStoreDeferred.promise);
      studyServiceMock.queryStudyData.and.returnValue(queryStudyDataDeferred.promise);
      studyServiceMock.queryArmData.and.returnValue(queryArmDataDeferred.promise);
      studyServiceMock.getGraph.and.returnValue(getGraphDeferred.promise);

      datasetServiceMock.loadStore.and.returnValue(loadDatasetStoreDeferred.promise);

      $controller('StudyController', {
        $scope: scope,
        $stateParams: mockStateParams,
        GraphResource: GraphResource,
        $location: locationMock,
        $anchorScroll: anchorScrollMock,
        $modal: modalMock,
        $window: {bind: 'mockBind'},
        StudyService: studyServiceMock,
        DatasetResource: DatasetResource,
        DatasetService: datasetServiceMock,
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

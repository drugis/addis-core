'use strict';
define(['angular-mocks', './dataset'], function() {
  describe('the dataset controller', function() {

    var scope,
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      studiesWithDetailsService = jasmine.createSpyObj('StudiesWithDetailsService', ['get', 'getTreatmentActivities', 'addActivitiesToStudies']),
      historyResource = jasmine.createSpyObj('HistoryResource', ['query']),
      conceptsService = jasmine.createSpyObj('ConceptsService', ['loadJson', 'queryItems']),
      graphResource = jasmine.createSpyObj('GraphResource', ['getConceptJson']),
      versionedGraphResource = jasmine.createSpyObj('VersionedGraphResource', ['getConceptJson']),
      datasetResource = jasmine.createSpyObj('DatasetResource', ['getForJson']),
      datasetVersionedResource = jasmine.createSpyObj('DatasetVersionedResource', ['getForJson']),
      userService = jasmine.createSpyObj('UserService', ['isLoginUserEmail', 'getLoginUser']),
      dataModelServiceMock = jasmine.createSpyObj('DataModelService', ['correctUnitConceptType']),
      excelExportServiceMock = jasmine.createSpyObj('ExcelExportService', ['exportDataset']),
      pageTitleServiceMock = jasmine.createSpyObj('PageTitleService', ['setPageTitle']),
      datasetDeferred,
      queryHistoryDeferred,
      studiesWithDetailsGetDeferred,
      conceptsJsonDefer,
      userDefer,
      mockStudiesWithDetail = [{
        id: 'study 1'
      }],
      userUid = 'userUid',
      datasetUuid = 'uuid-1',
      versionUuid = 'version-1',
      state = jasmine.createSpyObj('state', ['go']),
      stateParams = {
        userUid: userUid,
        datasetUuid: datasetUuid,
        versionUuid: versionUuid
      };

    beforeEach(angular.mock.module('trialverse.dataset', function($provide) {
      $provide.value('ExcelExportService', excelExportServiceMock);
    }));

    beforeEach(angular.mock.module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $q) {
      scope = $rootScope;

      studiesWithDetailsGetDeferred = $q.defer();
      queryHistoryDeferred = $q.defer();
      conceptsJsonDefer = $q.defer();
      datasetDeferred = $q.defer();
      userDefer = $q.defer();

      var historyItems = [{
        'uri': 'http://uri/' + versionUuid,
        historyOrder: 1
      }];
      queryHistoryDeferred.resolve(historyItems);
      datasetDeferred.resolve({
        'http://purl.org/dc/terms/title': 'title',
        'http://purl.org/dc/terms/description': 'description',
        'http://purl.org/dc/terms/creator': 'creator'
      });

      studiesWithDetailsService.get.and.returnValue(studiesWithDetailsGetDeferred.promise);
      conceptsService.loadJson.and.returnValue(conceptsJsonDefer.promise);
      userService.getLoginUser.and.returnValue(userDefer.promise);
      userService.isLoginUserEmail.and.returnValue($q.resolve(true));
      versionedGraphResource.getConceptJson.and.returnValue({
        $promise: conceptsJsonDefer.promise
      });
      graphResource.getConceptJson.and.returnValue({
        $promise: conceptsJsonDefer.promise
      });
      historyResource.query.and.returnValue({
        $promise: queryHistoryDeferred.promise
      });
      datasetVersionedResource.getForJson.and.returnValue({
        $promise: datasetDeferred.promise
      });
      datasetResource.getForJson.and.returnValue({
        $promise: datasetDeferred.promise
      });

      mockModal.open.calls.reset();
    }));

    describe('on load for a versioned view', function() {

      beforeEach(inject(function($controller) {
        $controller('DatasetController', {
          $scope: scope,
          $stateParams: stateParams,
          $state: state,
          $modal: mockModal,
          DatasetVersionedResource: datasetVersionedResource,
          DatasetResource: datasetResource,
          StudiesWithDetailsService: studiesWithDetailsService,
          HistoryResource: historyResource,
          ConceptsService: conceptsService,
          GraphResource: graphResource,
          VersionedGraphResource: versionedGraphResource,
          UserService: userService,
          DataModelService: dataModelServiceMock,
          PageTitleService: pageTitleServiceMock
        });
        scope.$apply();
      }));

      it('should get the dataset and place its properties on the scope', function() {
        expect(datasetVersionedResource.getForJson).toHaveBeenCalled();
        expect(scope.dataset).toEqual({
          datasetUuid: 'uuid-1',
          title: 'title',
          comment: 'description',
          creator: 'creator'
        });
      });

      it('should get the studies with detail and place them on the scope', function() {
        studiesWithDetailsGetDeferred.resolve(mockStudiesWithDetail);
        scope.$digest();
        expect(scope.studiesWithDetail).toEqual(mockStudiesWithDetail);
      });

      it('should place the table options on the scope', function() {
        expect(scope.tableOptions.columns[0].label).toEqual('Title');
      });

      it('should place the current revision on the scope', function() {
        expect(scope.currentRevision).toEqual({
          'uri': 'http://uri/' + versionUuid,
          historyOrder: 1,
          isHead: false
        });
      });

      it('should place the concepts on the scope', function() {
        var datasetConcepts = [{
          label: 'concept 1'
        }];
        conceptsJsonDefer.resolve(datasetConcepts);
        scope.$digest();
        expect(scope.datasetConcepts).toBeDefined(); // promise resolved
        expect(versionedGraphResource.getConceptJson).toHaveBeenCalled();
        expect(conceptsService.loadJson).toHaveBeenCalled();
      });
      it('should not allow editing', function() {
        expect(scope.isEditingAllowed).toBe(false);
      });
    });

    describe('on load for a head view', function() {
      beforeEach(inject(function($controller) {
        $controller('DatasetController', {
          $scope: scope,
          $window: {},
          $stateParams: {},
          $state: state,
          $modal: mockModal,
          DatasetVersionedResource: datasetVersionedResource,
          DatasetResource: datasetResource,
          StudiesWithDetailsService: studiesWithDetailsService,
          HistoryResource: historyResource,
          ConceptsService: conceptsService,
          GraphResource: graphResource,
          VersionedGraphResource: versionedGraphResource,
          UserService: userService,
          PageTitleService: pageTitleServiceMock
        });
        scope.$apply();
      }));

      afterEach(datasetResource.getForJson.calls.reset);

      it('should get the dataset using the non versioned resource ', function() {
        expect(datasetResource.getForJson).toHaveBeenCalled();
      });
      it('should allow editing', function() {
        expect(scope.isEditingAllowed).toBe(true);
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

      describe('showCreateProjectDialog', function() {
        it('should open a modal', function() {
          scope.createProjectDialog();
          expect(mockModal.open).toHaveBeenCalled();
        });
      });
    });

  });

});

'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('create study controller', function() {

    var scope,
      mockModal = jasmine.createSpyObj('$mock', ['dismiss', 'close']),
      datasetService = jasmine.createSpyObj('DatasetService', ['addStudyToDatasetGraph']),
      datasetResource = jasmine.createSpyObj('DatasetResource', ['save']),
      uUIDService = jasmine.createSpyObj('UUIDService', ['generate']),
      studyService = jasmine.createSpyObj('StudyService', ['createEmptyStudyJsonLD']),
      studyResource = jasmine.createSpyObj('StudyResource', ['put']),
      mockDatasetsResult,
      mockDatasetsDeferred;

    beforeEach(module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $q, $controller) {
      scope = $rootScope;
      mockDatasetsDeferred = $q.defer();
      mockDatasetsResult = {
        promise: mockDatasetsDeferred.promise,
      };
      //datasetService.addStudyToDatasetGraph.and.returnValue(mockDatasetsResult);

      $controller('CreateStudyController', {
        $scope: scope,
        $stateParams: {},
        $modalInstance: mockModal,
        DatasetService: datasetService,
        DatasetResource: datasetResource,
        UUIDService: uUIDService,
        StudyService: studyService,
        StudyResource: studyResource
      });
    }));


    describe('isUniqueShortName', function() {
      it('should be true if the shortname is not already in the list', function() {
        scope.studiesWithDetail = {
          $resolved: true
        };
        expect(scope.isUniqueShortName('connor')).toBe(true);
        scope.studiesWithDetail = [{
          label: 'daan'
        }];
        expect(scope.isUniqueShortName('connor')).toBe(true);
      });
      it('should be false if the shortname is already in the list', function() {
        scope.studiesWithDetail = [{
          label: 'daan'
        }, {
          label: 'connor'
        }];
        expect(scope.isUniqueShortName('connor')).toBe(false);
      });
    });

  });
});
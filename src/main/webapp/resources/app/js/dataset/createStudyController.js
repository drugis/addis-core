'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance',
    'successCallback', 'StudyService', 'ImportStudyResource',
    'ImportStudyInfoResource', 'UUIDService'
  ];

  var CreateStudyController = function($scope, $stateParams, $modalInstance,
    successCallback, StudyService, ImportStudyResource, ImportStudyInfoResource, UUIDService) {

    $scope.isCreatingStudy = false;
    $scope.studyImport = {};

    $scope.isUniqueShortName = function(shortName) {
      var anyduplicateName = _.find($scope.studiesWithDetail, function(existingStudy) {
        return existingStudy.label === shortName;
      });
      return !anyduplicateName;
    };

    $scope.createStudy = function(study) {
      $scope.isCreatingStudy = true;
      var newStudyVersionPromise = StudyService.createEmptyStudy(study, $stateParams.userUid, $stateParams.datasetUUID);
      newStudyVersionPromise.then(function(newVersion) {
        successCallback(newVersion);
        $scope.isCreatingStudy = false;
        $modalInstance.close();
      });
    };

    $scope.isValidNct = function(nctId) {
      var inLowerCase = nctId.toLowerCase();
      return new RegExp('^' + 'nct+').test(inLowerCase);
    };

    $scope.getInfo = function(studyImport) {
      studyImport.error = undefined;
      if ($scope.isValidNct(studyImport.nctId)) {
        $scope.studyImport.basicInfo = {};
        $scope.studyImport.loading = true;
        ImportStudyInfoResource.get({
          nctId: studyImport.nctId
        }).$promise.then(function(basicInfo) {
          $scope.studyImport.basicInfo = basicInfo;
          $scope.studyImport.loading = false;
        }, function(reason) {
          $scope.studyImport.basicInfo = [];
          studyImport.error = reason;
          studyImport.loading = false;
        }, function() {
          $scope.studyImport.loading = false;
        });
      }
    };

    $scope.import = function(studyImport) {
      var uuid = UUIDService.generate();
      var importStudyRef = studyImport.basicInfo.id;
      ImportStudyResource.import({
        userUid: $stateParams.userUid,
        datasetUUID: $stateParams.datasetUUID,
        graphUuid: uuid,
        importStudyRef: importStudyRef,
        commitTitle: 'Create study though import'
      }, function(value, responseHeaders) {
        var newVersionUri = responseHeaders('X-EventSource-Version');
        var newVersionUuid = UUIDService.getUuidFromNamespaceUrl(newVersionUri);
        successCallback(newVersionUuid);
        $scope.isCreatingStudy = false;
        $modalInstance.close();
      }, function(error) {
        console.error('error' + error);
        $scope.isCreatingStudy = false;
        $modalInstance.close();
      });
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };


  };
  return dependencies.concat(CreateStudyController);
});

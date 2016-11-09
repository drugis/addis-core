'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance',
    'successCallback', 'StudyService', 'ImportStudyResource',
    'ImportStudyInfoResource', 'UUIDService'
  ];

  var CreateStudyController = function($scope, $stateParams, $modalInstance,
    successCallback, StudyService, ImportStudyResource, ImportStudyInfoResource, UUIDService) {

    $scope.isCreatingStudy = false;
    $scope.importing = false;
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
      return new RegExp('^' + 'NCT+').test(nctId);
    };

    $scope.getInfo = function(studyImport) {
      studyImport.error = undefined;
      studyImport.notFound = false;
      studyImport.nctId = studyImport.nctId.toUpperCase();
      if ($scope.isValidNct(studyImport.nctId)) {
        $scope.studyImport.basicInfo = {};
        $scope.studyImport.loading = true;
        ImportStudyInfoResource.get({
          nctId: studyImport.nctId
        }).$promise.then(function(basicInfo) {
          $scope.studyImport.loading = false;
          if (!basicInfo.id) {
            studyImport.notFound = true;
          } else {
            $scope.studyImport.basicInfo = basicInfo;
          }
        }, function(reason) {
          console.error('error', reason);
          $modalInstance.close();
        }, function() {
          $scope.studyImport.loading = false;
        });
      }
    };

    $scope.import = function(studyImport) {
      $scope.isCreatingStudy = true;
      var uuid = UUIDService.generate();
      var importStudyRef = studyImport.basicInfo.id;
      ImportStudyResource.import({
        userUid: $stateParams.userUid,
        datasetUUID: $stateParams.datasetUUID,
        graphUuid: uuid,
        importStudyRef: importStudyRef,
        commitTitle: 'Import ' + importStudyRef + ' from ClinicalTrials.gov',
        commitDescription: studyImport.basicInfo.title
      }, function(value, responseHeaders) {
        var newVersionUri = responseHeaders('X-EventSource-Version');
        var newVersionUuid = UUIDService.getUuidFromNamespaceUrl(newVersionUri);
        successCallback(newVersionUuid);
        $scope.isCreatingStudy = false;
        $modalInstance.close();
      }, function(error) {
        console.error('error', error);
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

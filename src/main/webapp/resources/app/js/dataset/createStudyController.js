'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance',
    'successCallback',
    'UUIDService', 'StudyService'
  ];

  var CreateStudyController = function($scope, $stateParams, $modalInstance,
    successCallback, StudyService, ImportStudyResource) {

    $scope.isCreatingStudy = false;

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

    $scope.fetchImportInfo = function(studyImport) {
      ImportStudyResource.get({ntcId: studyImport.ntcId}).$promise.then(function(basicInfo){
        $scope.studyImport.basicInfo = basicInfo;
      });
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };


  };
  return dependencies.concat(CreateStudyController);
});

'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance',
    'successCallback', 'StudyService', 'ImportStudyResource'
  ];

  var CreateStudyController = function($scope, $stateParams, $modalInstance,
    successCallback, StudyService, ImportStudyResource) {

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
      delete studyImport.error;
      if ($scope.isValidNct(studyImport.nctId)) {
        delete $scope.studyImport.basicInfo;
        $scope.studyImport.loading = true;
        ImportStudyResource.get({
          nctId: studyImport.nctId
        }).$promise.then(function(basicInfo) {
          $scope.studyImport.basicInfo = basicInfo;
        }, function(reason) {
          delete $scope.studyImport.basicInfo;
          studyImport.error = reason;
        }, function(){
          $scope.studyImport.loading = false;
        });
      }
    };

    $scope.import = function(studyImport) {
      alert("todo do import");
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };


  };
  return dependencies.concat(CreateStudyController);
});

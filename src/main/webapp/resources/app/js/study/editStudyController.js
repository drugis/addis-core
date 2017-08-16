'use strict';
define(['angular'],
  function(angular) {
    var dependencies = ['$scope', '$modalInstance', 'StudyService', 'successCallback', 'study'];
    var StudyController = function($scope, $modalInstance, StudyService, successCallback, study) {
      // functions
      $scope.saveStudy = saveStudy;
      $scope.cancel = cancel;

      // init
      $scope.study = angular.copy(study);

      function saveStudy() {
        $scope.isSaving = true;
        StudyService.getStudy().then(function(studyNode) {
          studyNode.label = $scope.study.label;
          studyNode.comment = $scope.study.comment;
          StudyService.save(studyNode).then(function() {
            $modalInstance.close();
            successCallback($scope.study.label, $scope.study.comment);
          });
        });
      }

      function cancel() {
        $modalInstance.dismiss('cancel');
      }
    };
    return dependencies.concat(StudyController);
  });
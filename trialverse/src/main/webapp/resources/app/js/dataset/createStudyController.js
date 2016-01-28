'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$modalInstance',
    'successCallback',
    'UUIDService', 'StudyService', 'GraphResource'
  ];
  var CreateStudyController = function($scope, $stateParams, $modalInstance,
    successCallback, UUIDService, StudyService, GraphResource) {

    $scope.isCreatingStudy = false;

    $scope.isUniqueShortName = function(shortName) {
      var anyduplicateName = _.find($scope.studiesWithDetail, function(existingStudy) {
        return existingStudy.label === shortName;
      });
      return !anyduplicateName;
    };

    $scope.createStudy = function(study) {
      $scope.isCreatingStudy = true;
      StudyService.createEmptyStudy(study).then(function() {
        StudyService.getGraph().then(function(queryResult) {
          var uuid = StudyService.getStudyUUID();
          GraphResource.put({
            userUid: $stateParams.userUid,
            datasetUUID: $stateParams.datasetUUID,
            graphUuid: uuid,
            commitTitle: 'Initial study creation: ' + study.label
          }, queryResult.data, function(value, responseHeaders) {
            var newVersion = responseHeaders('X-EventSource-Version');
            newVersion = newVersion.split('/')[4];
            successCallback(newVersion);
            $scope.isCreatingStudy = false;
            $modalInstance.close();
          }, function(error) {
            console.log('error' + error);
          });
        });
      });

    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(CreateStudyController);
});

'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance',
    'GraphResource', 'successCallback', 'study'
  ];
  var DeleteStudyController = function($scope, $stateParams, $modalInstance,
    GraphResource, successCallback, study) {

    $scope.deleteStudy = deleteStudy;
    $scope.study = study;
    $scope.cancel = cancel;

    function deleteStudy() {
      GraphResource.delete(
        _.extend($stateParams, {
          graphUuid: study.graphUri.split('/graphs/')[1]
        }),
        function(response, responseHeaders) {
          var newVersion = responseHeaders('X-EventSource-Version');
          newVersion = newVersion.split('/versions/')[1];
          successCallback(newVersion);
          $modalInstance.close();
        });
    }

    function cancel() {
      $modalInstance.close();
    }

  };

  return dependencies.concat(DeleteStudyController);

});

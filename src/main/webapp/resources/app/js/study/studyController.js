'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'StudyResource'];
    var StudyController = function($scope, $stateParams, StudyResource) {

      StudyResource.get($stateParams).$promise.then(function(study) {
        $scope.study = study;
      });
    };

    return dependencies.concat(StudyController);
  });
'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'StudyResource'];
    var StudyController = function($scope, $stateParams, StudyResource) {

      StudyResource.get($stateParams).$promise.then(function(study) {
        $scope.study = study;
        console.log('get succes');
      });
    };

    return dependencies.concat(StudyController);
  });
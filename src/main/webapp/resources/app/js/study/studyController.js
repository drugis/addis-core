'use strict';
define([],
  function() {
    var dependencies = ['$scope'];
    var StudyController = function($scope) {
        $scope.study = {name: "test study"}
    };

    return dependencies.concat(StudyController);
  });
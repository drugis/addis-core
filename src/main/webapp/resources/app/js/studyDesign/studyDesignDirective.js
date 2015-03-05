'use strict';
define([], function() {
  var dependencies = ['$stateParams', 'ArmService', 'EpochService', 'ActivityService'];

  var StudyDesignDirective = function($stateParams, ArmService, EpochService, ActivityService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/studyDesign/studyDesignDirective.html',
      scope: {},
      link: function(scope) {

        scope.studyDesign = {};

        var armsPromise = ArmService.queryItems($stateParams.studyUUID).then(function(result){
          scope.arms = result;
        });

        var epochsPromise = EpochService.queryItems($stateParams.studyUUID).then(function(result){
          scope.epochs = result;
        });

        var activitiesPromise = ActivityService.queryItems($stateParams.studyUUID).then(function(result){
          scope.activities = result;
        });

      }

    };
  };

  return dependencies.concat(StudyDesignDirective);
});

'use strict';
define([], function() {
  var dependencies = ['$stateParams', 'ArmService', 'EpochService', 'ActivityService', 'StudyDesignService'];

  var StudyDesignDirective = function($stateParams, ArmService, EpochService, ActivityService, StudyDesignService) {
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

        scope.onActivitySelected = function (epochUri, armUri, activity) {
          var coordinate = {
            epochUri: epochUri,
            armUri: armUri,
            activityUri: activity.uri
          };
          StudyDesignService.setActivityCoordinates($stateParams.studyUUID, coordinate);
        };

      }

    };
  };

  return dependencies.concat(StudyDesignDirective);
});

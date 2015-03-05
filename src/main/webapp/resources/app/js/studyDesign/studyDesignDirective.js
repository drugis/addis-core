'use strict';
define([], function() {
  var dependencies = ['$stateParams', '$q', 'ArmService', 'EpochService', 'ActivityService', 'StudyDesignService'];

  var StudyDesignDirective = function($stateParams, $q, ArmService, EpochService, ActivityService, StudyDesignService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/studyDesign/studyDesignDirective.html',
      scope: {},
      link: function(scope) {

        // scope.studyDesign = {};

        var reloadData = function() {
          var armsPromise = ArmService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.arms = result;
          });

          var epochsPromise = EpochService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.epochs = result;
          });

          var activitiesPromise = ActivityService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.activities = result;
          });

          $q.all([armsPromise, epochsPromise, activitiesPromise]).then(function() {
            StudyDesignService.queryItems($stateParams.studyUUID).then(function(coordinates) {
              var activityMap = _.indexBy(scope.activities, 'activityUri');
              var studyDesign = _.object(_.pluck(scope.epochs, 'uri'));

              _.each(scope.epochs, function(epoch) {
                studyDesign[epoch.uri] = _.object(_.pluck(scope.arms, 'armURI'));
              });

              _.each(coordinates, function(coordinate) {
                studyDesign[coordinate.epochUri][coordinate.armUri] = activityMap[coordinate.activityUri];
              });
              scope.studyDesign = studyDesign;
            });
          });
        }

        reloadData();

        scope.$on('refreshStudyDesign', function() {
          reloadData();
        });

        scope.onActivitySelected = function(epochUri, armUri, activity) {
          var coordinate = {
            epochUri: epochUri,
            armUri: armUri,
            activityUri: activity.activityUri
          };
          StudyDesignService.setActivityCoordinates($stateParams.studyUUID, coordinate);
        };

      }

    };
  };

  return dependencies.concat(StudyDesignDirective);
});
'use strict';
define(['lodash'], function(_) {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService', 'EpochService'];
    var StudyDesignService = function($q, StudyService, SparqlResource, UUIDService, EpochService) {

      function createCoordinate(activityUri, activityApplication) {
        return {
          activityUri: activityUri,
          epochUri: activityApplication.applied_in_epoch,
          armUri: activityApplication.applied_to_arm
        };
      }

      function flattenActivity(acc, activity) {
        if (activity.has_activity_application) {
          var applications = _.map(activity.has_activity_application, _.partial(createCoordinate, activity['@id']));
          return acc.concat(applications);
        } else {
          return acc;
        }
      }

      function queryItems() {
        return StudyService.getStudy().then(function(study) {
          return study.has_activity.reduce(flattenActivity, []);
        });
      }

      function setActivityCoordinates(coordinates) {
        return StudyService.getStudy().then(function(study) {

          // if coordinate was set clear it out
          _.each(study.has_activity, function(activity) {
            _.remove(activity.has_activity_application, function(application) {
              return coordinates.epochUri === application.applied_in_epoch &&
                coordinates.armUri === application.applied_to_arm;
            });
          });

          // find the activity to update
          var activityToAddTo = _.find(study.has_activity, function(activity) {
            return coordinates.activityUri === activity['@id'];
          });

          if(!activityToAddTo.has_activity_application) {
            activityToAddTo.has_activity_application = [];
          }

          activityToAddTo.has_activity_application = activityToAddTo.has_activity_application.concat([{
            '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
            applied_in_epoch: coordinates.epochUri,
            applied_to_arm: coordinates.armUri
          }]);

          return StudyService.save(study);
        });
      }

      function cleanupCoordinates() {
        var StudyPromise = StudyService.getStudy();
        var epochsPromise = EpochService.queryItems();
        return $q.all([StudyPromise, epochsPromise]).then(function(result) {
          var study = result[0];
          var epochUris =_.map(result[1], 'uri');
          var arms = _.map(study.has_arm, '@id');

          _.each(study.has_activity, function(activity) {
            _.remove(activity.has_activity_application, function(application) {
              return !_.includes(arms, application.applied_to_arm) ||
                !_.includes(epochUris, application.applied_in_epoch);
            });
          });

          return StudyService.save(study);
        });
      }

      return {
        queryItems: queryItems,
        setActivityCoordinates: setActivityCoordinates,
        cleanupCoordinates: cleanupCoordinates
      };
    };
    return dependencies.concat(StudyDesignService);
  });

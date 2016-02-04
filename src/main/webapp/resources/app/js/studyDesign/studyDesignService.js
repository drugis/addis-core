'use strict';
define(['lodash'], function(_) {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService'];
    var StudyDesignService = function($q, StudyService, SparqlResource, UUIDService) {

      function createCoordinate(activityUri, activityApplication) {
        return {
          activityUri: activityUri,
          epochUri: activityApplication.applied_in_epoch,
          armUri: activityApplication.applied_to_arm
        };
      }

      function flattenActivity(acumulator, activity) {
        if (activity.has_activity_application) {
          return acumulator
            .concat(
              activity
              .has_activity_application
              .map(createCoordinate.bind(this, activity['@id']))
            );
        } else {
          return acumulator;
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
        return StudyService.getStudy().then(function(study) {
          var arms = _.map(study.has_arm, '@id');
          var epochs = _.map(study.has_epochs, '@id');

          _.each(study.has_activity, function(activity) {
            _.remove(activity.has_activity_application, function(application) {
              return !_.includes(arms, application.applied_to_arm) ||
                !_.includes(epochs, application.applied_in_epoch);
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

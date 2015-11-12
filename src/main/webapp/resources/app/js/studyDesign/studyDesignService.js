'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService'];
    var StudyDesignService = function($q, StudyService, SparqlResource, UUIDService) {

      //var queryActivityCoordinatesTemplate = SparqlResource.get('queryActivityCoordinates.sparql');
      //var setActivityCoordinatesTemplate = SparqlResource.get('setActivityCoordinates.sparql');
      //var cleanupCoordinatesTemplate = SparqlResource.get('cleanupCoordinates.sparql');

      function createCoordinate(activityUri, activityApplication) {
        return {
          activityUri: activityUri,
          epochUri: activityApplication.applied_in_epoch,
          armUri: activityApplication.applied_to_arm
        };
      }

      function flattenActivity(acumulator, activity) {
        return acumulator
          .concat(
            activity
            .has_activity_application
            .map(createCoordinate.bind(this, activity['@id']))
          );
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

          // find the activity update
          var activityToAddTo = _.find(study.has_activity, function(activity) {
            return coordinates.activityUri === activity['@id'];
          });

          activityToAddTo.has_activity_application = activityToAddTo.has_activity_application.concat([{
            '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
            applied_in_epoch: coordinates.epochUri,
            applied_to_arm: coordinates.armUri
          }]);

          return StudyService.save(study);
        });
      }

      function cleanupCoordinates() {
        // return cleanupCoordinatesTemplate.then(function(template) {
        //   var query = fillInTemplate(template, {});
        //   return StudyService.doModifyingQuery(query);
        // });
      }

      // function fillInTemplate(template, coordinates) {
      //   return template
      //       .replace(/\$epochUri/g, coordinates.epochUri)
      //       .replace(/\$armUri/g, coordinates.armUri)
      //       .replace(/\$activityUri/g, coordinates.activityUri);
      // }

      return {
        queryItems: queryItems,
        setActivityCoordinates: setActivityCoordinates,
        cleanupCoordinates: cleanupCoordinates
      };
    };
    return dependencies.concat(StudyDesignService);
  });
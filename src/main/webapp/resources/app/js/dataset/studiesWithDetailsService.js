'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$http', '$filter', 'SparqlResource'];
  var StudiesWithDetailsService = function($http, $filter, SparqlResource) {

    var queryStudiesWithDetails = SparqlResource.get('queryStudiesWithDetails.sparql');
    var queryTreatmentActivities = SparqlResource.get('queryTreatmentActivities.sparql');
    var getStudyTitleQuery = SparqlResource.get('getStudyTitleQuery.sparql');

    function deFusekify(data) {
      var json = JSON.parse(data);
      var bindings = json.results.bindings;
      return _.map(bindings, function(binding) {
        return _.fromPairs(_.map(_.toPairs(binding), function(obj) {
          return [obj[0], obj[1].value];
        }));
      });
    }

    function executeQuery(queryPromise, userUid, datasetUuid, datasetVersionUuid) {
      return queryPromise.then(function(query) {
        var restPath = '/users/' + userUid + '/datasets/' + datasetUuid;
        if (datasetVersionUuid) {
          restPath = restPath + '/versions/' + datasetVersionUuid;
        }
        return $http.get(
          restPath + '/query', {
            params: {
              query: query
            },
            headers: {
              Accept: 'application/sparql-results+json'
            },
            transformResponse: function(data) {
              return deFusekify(data);
            }
          });
      }).then(function(response) {
        return response.data;
      });
    }

    function get(userUid, datasetUuid, datasetVersionUuid) {
      return executeQuery(queryStudiesWithDetails, userUid, datasetUuid, datasetVersionUuid);
    }

    function getWithoutDetails(userUid, datasetUuid, datasetVersionUuid, studyUuid) {
      var filledInQuery = getStudyTitleQuery.then(function(query) {
        return query.replace('$studyUuid$', studyUuid);
      });
      return executeQuery(filledInQuery, userUid, datasetUuid, datasetVersionUuid);
    }

    function getTreatmentActivities(userUid, datasetUuid, datasetVersionUuid) {
      return executeQuery(queryTreatmentActivities, userUid, datasetUuid, datasetVersionUuid);
    }

    function addActivitiesToStudies(studies, activities) {
      return _.map(studies, function(study) {
        study.treatments = _.chain(activities)
          .filter(['study', study.studyUri])
          .groupBy('activity')
          .map(function(activityGroup) {
            return _.map(activityGroup, activityToString).join(' + ');
          })
          .value()
          .sort()
          .join(', ');
        return study;
      });

    }

    function activityToString(activity) {
      if (activity.treatmentType === 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment') {
        return activity.drugName + ' ' + $filter('exponentialFilter')(activity.fixedDoseValue) + ' ' + activity.fixedDoseUnitLabel +
          ' per ' + $filter('durationFilter')(activity.fixedDoseDosingPeriodicity);

      } else if (activity.treatmentType === 'http://trials.drugis.org/ontology#TitratedDoseDrugTreatment') {
        return activity.drugName + ' ' + $filter('exponentialFilter')(activity.minDoseValue) + '-' +
          $filter('exponentialFilter')(activity.maxDoseValue) + ' ' + activity.minDoseUnitLabel + ' per ' + $filter('durationFilter')(activity.minDoseDosingPeriodicity);
      }
    }

    return {
      get: get,
      getWithoutDetails: getWithoutDetails,
      getTreatmentActivities: getTreatmentActivities,
      addActivitiesToStudies: addActivitiesToStudies
    };
  };
  return dependencies.concat(StudiesWithDetailsService);
});

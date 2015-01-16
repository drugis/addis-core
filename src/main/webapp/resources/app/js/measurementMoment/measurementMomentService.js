'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource'];
    var MeasurementMomentService = function($q, StudyService, SparqlResource) {

      var epochQuery = SparqlResource.get({
        name: 'queryMeasurementMoment.sparql'
      });

      function queryItems() {
        return epochQuery.$promise.then(function(query) {
          return StudyService.doNonModifyingQuery(query.data);
        });
      }

      return {
        queryItems: queryItems
      };
    };
    return dependencies.concat(MeasurementMomentService);
  });

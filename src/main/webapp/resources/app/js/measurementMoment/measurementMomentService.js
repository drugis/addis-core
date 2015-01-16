'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource'];
    var MeasurementMomentService = function($q, StudyService, SparqlResource) {

      var measurementMomentQuery = SparqlResource.get('queryMeasurementMoment.sparql');
      var addItem = SparqlResource.get('addMeasurementMoment.sparql');

      function queryItems() {
        return measurementMomentQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query);
        });
      }

      function addItem(item) {
        return addMeasurementMomentQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query);
        });
      }

      return {
        queryItems: queryItems
      };
    };
    return dependencies.concat(MeasurementMomentService);
  });

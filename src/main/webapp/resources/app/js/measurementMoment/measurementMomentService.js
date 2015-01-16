'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource'];
    var MeasurementMomentService = function($q, StudyService, SparqlResource) {

      var measurementMomentQuery = SparqlResource.get({
        name: 'queryMeasurementMoment.sparql'
      });

      var addItem = SparqlResource.get({
        name: 'addMeasurementMoment.sparql'
      });

      function queryItems() {
        return measurementMomentQuery.$promise.then(function(query) {
          return StudyService.doNonModifyingQuery(query.data);
        });
      }

      function addItem(item) {
        return addMeasurementMomentQuery.$promise.then(function(query) {
          return StudyService.doNonModifyingQuery(query.data);
        });
      }

      return {
        queryItems: queryItems
      };
    };
    return dependencies.concat(MeasurementMomentService);
  });

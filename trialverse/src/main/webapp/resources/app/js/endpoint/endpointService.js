'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'MeasurementMomentService', 'SparqlResource', 'OutcomeService'];
    var EndpointService = function($q, StudyService, UUIDService, MeasurementMomentService, SparqlResource, OutcomeService) {

      var addTemplateRaw = SparqlResource.get('addTemplate.sparql');

      var addEndpointQueryRaw = SparqlResource.get('addEndpoint.sparql');
      var endpointQuery = SparqlResource.get('queryEndpoint.sparql');
      var deleteEndpointRaw = SparqlResource.get('deleteVariable.sparql');
      var editEndpointRaw = SparqlResource.get('editVariable.sparql');
      var queryEndpointMeasuredAtRaw = SparqlResource.get('queryMeasuredAt.sparql');

      function queryItems() {
        var endpoints, measuredAtMoments, measurementMoments;

        var endpointsQueryPromise = endpointQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            endpoints = result;
          });
        });

        var measuredAtQueryPromise = queryEndpointMeasuredAtRaw.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            measuredAtMoments = result;
          });
        });

        var measurementMomentsPromise = MeasurementMomentService.queryItems().then(function(result) {
          measurementMoments = result;
        });

        return $q.all([endpointsQueryPromise, measuredAtQueryPromise, measurementMomentsPromise]).then(function() {
          return _.map(endpoints, function(endpoint) {
            var filtered = _.filter(measuredAtMoments, function(measuredAtMoment) {
              return endpoint.uri === measuredAtMoment.itemUri;
            });

            endpoint.measuredAtMoments = _.map(_.pluck(filtered, 'measurementMoment'), function(measurementMomentUri) {
              return _.find(measurementMoments, function(moment) {
                return measurementMomentUri === moment.uri;
              });
            });
            return endpoint;
          });
        });
      }

      function addItem(endpoint) {
        var newItem = angular.copy(endpoint);
        newItem.uuid = UUIDService.generate();
        newItem.uri = 'http://trials.drugis.org/instances/' + newItem.uuid;
        var stringToInsert = buildInsertMeasuredAtBlock(newItem);

        var addEndpointPromise = addEndpointQueryRaw.then(function(query) {
          var addEndpointQuery = fillInTemplate(query, newItem);
          return StudyService.doModifyingQuery(addEndpointQuery).then(function(){
            return OutcomeService.setOutcomeProperty(newItem);
          });
        });

        var addMeasuredAtPromise = addTemplateRaw.then(function(query) {
          var addMeasuredAtQuery = fillInTemplate(query, stringToInsert);
          return StudyService.doModifyingQuery(addMeasuredAtQuery);
        });

        return $q.all([addEndpointPromise, addMeasuredAtPromise]);
      }

      function deleteItem(item) {
        return deleteEndpointRaw.then(function(deleteQueryRaw) {
          return StudyService.doModifyingQuery(fillInTemplate(deleteQueryRaw, item));
        });
      }

      function editItem(item) {
        var newItem = angular.copy(item);
        newItem.measurementMomentBlock = buildInsertMeasuredAtBlock(item);
        return editEndpointRaw.then(function(editQueryRaw) {
          var editQuery = fillInTemplate(editQueryRaw, newItem);
          return StudyService.doModifyingQuery(editQuery).then(function(){
            return OutcomeService.setOutcomeProperty(item);
          });
        });
      }

      function buildInsertMeasuredAtBlock(endpoint) {
        return _.reduce(endpoint.measuredAtMoments, function(accumulator, measuredAtMoment) {
          return accumulator + ' <' + endpoint.uri + '> ontology:is_measured_at <' + measuredAtMoment.uri + '> .';
        }, '');
      }

      function fillInTemplate(template, item) {
        return template
               .replace(/\$UUID/g, item.uuid)
               .replace('$label', item.label)
               .replace('$measurementType', item.measurementType)
               .replace('$insertBlock', item)
               .replace('$insertMeasurementMomentBlock', item.measurementMomentBlock)
               .replace(/\$URI/g, item.uri)
              ;
      }


      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(EndpointService);
  });

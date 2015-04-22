'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'MeasurementMomentService', 'SparqlResource', 'OutcomeService'];
    var EndpointService = function($q, StudyService, UUIDService, MeasurementMomentService, SparqlResource, OutcomeService) {

      var addTemplateRaw = SparqlResource.get('addTemplate.sparql');

      var addEndpointQueryRaw = SparqlResource.get('addEndpoint.sparql');
      var endpointQuery = SparqlResource.get('queryEndpoint.sparql');
      var deleteEndpointRaw = SparqlResource.get('deleteEndpoint.sparql');
      var editEndpointRaw = SparqlResource.get('editEndpoint.sparql');
      var queryEndpointMeasuredAtRaw = SparqlResource.get('queryMeasuredAt.sparql');

      function queryItems(studyUuid) {
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

        var measurementMomentsPromise = MeasurementMomentService.queryItems(studyUuid).then(function(result) {
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

      function addItem(studyUuid, endpoint) {
        var newUUid = UUIDService.generate();
        endpoint.uri = 'http://trials.drugis.org/instances/' + newUUid;
        var stringToInsert = buildInsertMeasuredAtBlock(endpoint);

        var addEndpointPromise = addEndpointQueryRaw.then(function(query) {
          var addEndpointQuery = query
            .replace(/\$studyUuid/g, studyUuid)
            .replace(/\$UUID/g, newUUid)
            .replace('$label', endpoint.label)
            .replace('$measurementType', endpoint.measurementType);
          return StudyService.doModifyingQuery(addEndpointQuery).then(function(){
            return OutcomeService.setOutcomeProperty(endpoint);
          });
        });

        var addMeasuredAtPromise = addTemplateRaw.then(function(query) {
          var addMeasuredAtQuery = query.replace('$insertBlock', stringToInsert);
          return StudyService.doModifyingQuery(addMeasuredAtQuery);
        });

        return $q.all([addEndpointPromise, addMeasuredAtPromise]);
      }

      function deleteItem(item) {
        return deleteEndpointRaw.then(function(deleteQueryRaw) {
          return StudyService.doModifyingQuery(deleteQueryRaw.replace(/\$URI/g, item.uri));
        });
      }

      function editItem(item) {
        var stringToInsert = buildInsertMeasuredAtBlock(item);
        return editEndpointRaw.then(function(editQueryRaw) {
          var editQuery = editQueryRaw.replace(/\$URI/g, item.uri)
            .replace('$newLabel', item.label)
            .replace('$newMeasurementType', item.measurementType)
            .replace('$insertMeasurementMomentBlock', stringToInsert);
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


      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(EndpointService);
  });

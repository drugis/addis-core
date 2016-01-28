'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource', 'MeasurementMomentService', 'OutcomeService'];
    var AdverseEventService = function($q, StudyService, UUIDService, SparqlResource, MeasurementMomentService, OutcomeService) {

      var addTemplateRaw = SparqlResource.get('addTemplate.sparql');

      var addAdverseEventQueryRaw = SparqlResource.get('addAdverseEvent.sparql');
      var adverseEventsQuery = SparqlResource.get('queryAdverseEvent.sparql');
      var deleteAdverseEventRaw = SparqlResource.get('deleteVariable.sparql');
      var editAdverseEventRaw = SparqlResource.get('editVariable.sparql');
      var queryAdverseEventMeasuredAtRaw = SparqlResource.get('queryMeasuredAt.sparql');

      function queryItems() {
        var adverseEvents, measuredAtMoments, measurementMoments;

        var adverseEventsQueryPromise = adverseEventsQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            adverseEvents = result;
          });
        });

        var measuredAtQueryPromise = queryAdverseEventMeasuredAtRaw.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            measuredAtMoments = result;
          });
        });

        var measurementMomentsPromise = MeasurementMomentService.queryItems().then(function(result){
          measurementMoments = result;
        });

        return $q.all([adverseEventsQueryPromise, measuredAtQueryPromise, measurementMomentsPromise]).then(function() {
          return _.map(adverseEvents, function(adverseEvent) {
            var filtered = _.filter(measuredAtMoments, function(measuredAtMoment) {
              return adverseEvent.uri === measuredAtMoment.itemUri;
            });

            adverseEvent.measuredAtMoments = _.map(_.pluck(filtered, 'measurementMoment'), function(measurementMomentUri) {
              return _.find(measurementMoments, function(moment){
                return measurementMomentUri === moment.uri;
              });
            });
            return adverseEvent;
          });
        });
      }

      function addItem(adverseEvent) {
        var newItem = angular.copy(adverseEvent);
        newItem.uuid = UUIDService.generate();
        newItem.uri = 'http://trials.drugis.org/instances/' + newItem.uuid;
        var stringToInsert = buildInsertMeasuredAtBlock(newItem);

        var addAdverseEventPromise = addAdverseEventQueryRaw.then(function(query) {
          var addAdverseEventQuery = fillInTemplate(query, newItem);
          return StudyService.doModifyingQuery(addAdverseEventQuery).then(function(){
            return OutcomeService.setOutcomeProperty(newItem);
          });
        });

        var addMeasuredAtPromise = addTemplateRaw.then(function(query) {
          var addMeasuredAtQuery = query.replace('$insertBlock', stringToInsert);
          return StudyService.doModifyingQuery(addMeasuredAtQuery);
        });

        return $q.all([addAdverseEventPromise, addMeasuredAtPromise]);
      }

      function deleteItem(item) {
        return deleteAdverseEventRaw.then(function(deleteQueryRaw) {
          return StudyService.doModifyingQuery(fillInTemplate(deleteQueryRaw, item));
        });
      }

      function editItem(item) {
        var newItem = angular.copy(item);
        newItem.measurementMomentBlock = buildInsertMeasuredAtBlock(item);
        return editAdverseEventRaw.then(function(editQueryRaw) {
          var editQuery = fillInTemplate(editQueryRaw, newItem);
          return StudyService.doModifyingQuery(editQuery).then(function(){
            return OutcomeService.setOutcomeProperty(item);
          });
        });
      }

      function buildInsertMeasuredAtBlock(adverseEvent) {
        return _.reduce(adverseEvent.measuredAtMoments, function(accumulator, measuredAtMoment){
          return accumulator + ' <' + adverseEvent.uri + '> ontology:is_measured_at <' + measuredAtMoment.uri + '> .';
        }, '');
      }

      function fillInTemplate(template, item) {
        return template
               .replace(/\$UUID/g, item.uuid)
               .replace('$label', item.label)
               .replace('$measurementType', item.measurementType)
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
    return dependencies.concat(AdverseEventService);
  });

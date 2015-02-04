'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource', 'MeasurementMomentService'];
    var AdverseEventService = function($q, StudyService, UUIDService, SparqlResource, MeasurementMomentService) {

      var addTemplateRaw = SparqlResource.get('addTemplate.sparql');

      var addAdverseEventQueryRaw = SparqlResource.get('addAdverseEvent.sparql');
      var adverseEventsQuery = SparqlResource.get('queryAdverseEvent.sparql');
      var deleteAdverseEventRaw = SparqlResource.get('deleteAdverseEvent.sparql');
      var editAdverseEventRaw = SparqlResource.get('editAdverseEvent.sparql');
      var queryAdverseEventMeasuredAtRaw = SparqlResource.get('queryMeasuredAt.sparql');
      // var addMeasuredAtRaw = SparqlResource.get('addMeasuredAt.sparql');
      // var editMeasuredAtRaw = SparqlResource.get('addMeasuredAt.sparql');

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
        var newUUid = UUIDService.generate();
        var stringToInsert = _.reduce(adverseEvent.measuredAtMoments, function(accumulator, measuredAtMoment){
          return accumulator + ' instance:' + newUUid + ' ontology:is_measured_at <' + measuredAtMoment.uri + '> .';
        }, '');

        var addAdverseEventPromise = addAdverseEventQueryRaw.then(function(query) {
          var addAdverseEventQuery = query
            .replace(/\$UUID/g, newUUid)
            .replace('$label', adverseEvent.label)
            .replace('$measurementType', adverseEvent.measurementType);
          return StudyService.doModifyingQuery(addAdverseEventQuery);
        });

        var addMeasuredAtPromise = addTemplateRaw.then(function(query) {
          var addMeasuredAtQuery = query.replace('$insertBlock', stringToInsert);
          return StudyService.doModifyingQuery(addMeasuredAtQuery);
        });

        return $q.all([addAdverseEventPromise, addMeasuredAtPromise]);
      }

      function deleteItem(item) {
        var defer = $q.defer();

        deleteAdverseEventRaw.then(function(deleteQueryRaw) {
          var deleteQuery = deleteQueryRaw.replace(/\$URI/g, item.uri);
          defer.resolve(StudyService.doModifyingQuery(deleteQuery));
        });
        return defer.promise;
      }

      function editItem(item) {
        var defer = $q.defer();

        editAdverseEventRaw.then(function(editQueryRaw) {
          var editQuery = editQueryRaw.replace(/\$URI/g, item.uri)
            .replace('$newLabel', item.label)
            .replace('$newMeasurementType', item.measurementType);
          defer.resolve(StudyService.doModifyingQuery(editQuery));
        });
        return defer.promise;
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
'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource'];
    var AdverseEventService = function($q, StudyService, UUIDService, SparqlResource) {

      var addAdverseEventQueryRaw = SparqlResource.get('addAdverseEvent.sparql');
      var adverseEventsQuery = SparqlResource.get('queryAdverseEvent.sparql');
      var deleteAdverseEventRaw = SparqlResource.get('deleteAdverseEvent.sparql');
      var editAdverseEventRaw = SparqlResource.get('editAdverseEvent.sparql');

      function queryItems() {
        var defer = $q.defer();

        adverseEventsQuery.then(function(query) {
          defer.resolve(StudyService.doNonModifyingQuery(query));
        });
        return defer.promise;
      }

      function addItem(adverseEvent) {
        var defer = $q.defer();

        addAdverseEventQueryRaw.then(function(query) {
          var addAdverseEventQuery = query
            .replace(/\$UUID/g, UUIDService.generate())
            .replace('$label', adverseEvent.label)
            .replace('$measurementType', adverseEvent.measurementType);
          defer.resolve(StudyService.doModifyingQuery(addAdverseEventQuery));
        });
        return defer.promise;
      }

      function deleteItem(item) {
        var defer = $q.defer();

        deleteAdverseEventRaw.then(function(deleteQueryRaw) {
          var deleteQuery = deleteQueryRaw.replace(/\$URI/g, item.uri.value);
          defer.resolve(StudyService.doModifyingQuery(deleteQuery));
        });
        return defer.promise;
      }

      function editItem(item) {
        var defer = $q.defer();

        editAdverseEventRaw.then(function(editQueryRaw) {
          var editQuery = editQueryRaw.replace(/\$URI/g, item.uri.value)
            .replace('$newLabel', item.label.value)
            .replace('$newMeasurementType', item.measurementType.value);
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

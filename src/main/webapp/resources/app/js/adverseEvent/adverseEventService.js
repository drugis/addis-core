'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource'];
    var AdverseEventService = function($q, StudyService, UUIDService, SparqlResource) {

      var addAdverseEventQueryRaw = SparqlResource.get({
        name: 'addAdverseEvent.sparql'
      });

      var adverseEventsQuery = SparqlResource.get({
        name: 'queryAdverseEvent.sparql'
      });

      var deleteAdverseEventRaw = SparqlResource.get({
        name: 'deleteAdverseEvent.sparql'
      });

      var editAdverseEventRaw = SparqlResource.get({
        name: 'editAdverseEvent.sparql'
      });

      function queryItems() {
        var defer = $q.defer();

        adverseEventsQuery.$promise.then(function(query) {
          defer.resolve(StudyService.doNonModifyingQuery(query.data));
        });
        return defer.promise;
      }

      function addItem(adverseEvent) {
        var defer = $q.defer();

        addAdverseEventQueryRaw.$promise.then(function(query) {
          var addAdverseEventQuery = query.data
            .replace(/\$UUID/g, UUIDService.generate())
            .replace('$label', adverseEvent.label)
            .replace('$measurementType', adverseEvent.measurementType);
          defer.resolve(StudyService.doModifyingQuery(addAdverseEventQuery));
        });
        return defer.promise;
      }

      function deleteItem(item) {
        var defer = $q.defer();

        deleteAdverseEventRaw.$promise.then(function(deleteQueryRaw) {
          var deleteQuery = deleteQueryRaw.data.replace(/\$URI/g, item.uri.value);
          defer.resolve(StudyService.doModifyingQuery(deleteQuery));
        });
        return defer.promise;
      }

      function editItem(item) {
        var defer = $q.defer();

        editAdverseEventRaw.$promise.then(function(editQueryRaw) {
          var editQuery = editQueryRaw.data.replace(/\$URI/g, item.uri.value)
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

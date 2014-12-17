'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource'];
    var EndpointService = function($q, StudyService, UUIDService, SparqlResource) {

      var addEndpointQueryRaw = SparqlResource.get({
        name: 'addEndpoint.sparql'
      });

      var endpointQuery = SparqlResource.get({
        name: 'queryEndpoint.sparql'
      });

      var deleteEndpointRaw = SparqlResource.get({
        name: 'deleteEndpoint.sparql'
      });

      var editEndpointRaw = SparqlResource.get({
        name: 'editEndpoint.sparql'
      });

      function queryItems() {
        var defer = $q.defer();

        endpointQuery.$promise.then(function(query) {
          defer.resolve(StudyService.doNonModifyingQuery(query.data));
        });
        return defer.promise;
      }

      function addItem(endpoint) {
        var defer = $q.defer();

        addEndpointQueryRaw.$promise.then(function(query) {
          var addEndpointQuery = query.data
            .replace(/\$UUID/g, UUIDService.generate())
            .replace('$label', endpoint.label)
            .replace('$measurementType', endpoint.measurementType);
          defer.resolve(StudyService.doModifyingQuery(addEndpointQuery));
        });
        return defer.promise;
      }

      function deleteItem(item) {
        var defer = $q.defer();

        deleteEndpointRaw.$promise.then(function(deleteQueryRaw) {
          var deleteQuery = deleteQueryRaw.data.replace(/\$URI/g, item.uri.value);
          defer.resolve(StudyService.doModifyingQuery(deleteQuery));
        });
        return defer.promise;
      }

      function editItem(item) {
        var defer = $q.defer();

        editEndpointRaw.$promise.then(function(editQueryRaw) {
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
    return dependencies.concat(EndpointService);
  });

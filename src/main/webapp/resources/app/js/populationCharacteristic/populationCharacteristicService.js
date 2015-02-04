'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource'];
    var PopulationCharacteristicService = function($q, StudyService, UUIDService, SparqlResource) {

      var addPopulationCharacteristicQueryRaw = SparqlResource.get('addPopulationCharacteristic.sparql');
      var populationCharacteristicsQuery = SparqlResource.get('queryPopulationCharacteristic.sparql');
      var deletePopulationCharacteristicRaw = SparqlResource.get('deletePopulationCharacteristic.sparql');
      var editPopulationCharacteristicRaw = SparqlResource.get('editPopulationCharacteristic.sparql');

      function queryItems() {
        var defer = $q.defer();

        populationCharacteristicsQuery.then(function(query) {
          defer.resolve(StudyService.doNonModifyingQuery(query));
        });
        return defer.promise;
      }

      function addItem(populationCharacteristic) {
        var defer = $q.defer();

        addPopulationCharacteristicQueryRaw.then(function(query) {
          var addPopulationCharacteristicQuery = query
            .replace(/\$UUID/g, UUIDService.generate())
            .replace('$label', populationCharacteristic.label)
            .replace('$measurementType', populationCharacteristic.measurementType);
          defer.resolve(StudyService.doModifyingQuery(addPopulationCharacteristicQuery));
        });
        return defer.promise;
      }

      function deleteItem(item) {
        var defer = $q.defer();

        deletePopulationCharacteristicRaw.then(function(deleteQueryRaw) {
          var deleteQuery = deleteQueryRaw.replace(/\$URI/g, item.uri);
          defer.resolve(StudyService.doModifyingQuery(deleteQuery));
        });
        return defer.promise;
      }

      function editItem(item) {
        var defer = $q.defer();

        editPopulationCharacteristicRaw.then(function(editQueryRaw) {
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
    return dependencies.concat(PopulationCharacteristicService);
  });

'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource'];
    var PopulationCharacteristicService = function($q, StudyService, UUIDService, SparqlResource) {

      var addPopulationCharacteristicQueryRaw = SparqlResource.get({
        name: 'addPopulationCharacteristic.sparql'
      });

      var populationCharacteristicsQuery = SparqlResource.get({
        name: 'queryPopulationCharacteristic.sparql'
      });

      var deletePopulationCharacteristicRaw = SparqlResource.get({
        name: 'deletePopulationCharacteristic.sparql'
      });

      var editPopulationCharacteristicRaw = SparqlResource.get({
        name: 'editPopulationCharacteristic.sparql'
      });

      function queryItems() {
        var defer = $q.defer();

        populationCharacteristicsQuery.$promise.then(function(query) {
          defer.resolve(StudyService.doNonModifyingQuery(query.data));
        });
        return defer.promise;
      }

      function addItem(populationCharacteristic) {
        var defer = $q.defer();

        addPopulationCharacteristicQueryRaw.$promise.then(function(query) {
          var addPopulationCharacteristicQuery = query.data
            .replace(/\$UUID/g, UUIDService.generate())
            .replace('$label', populationCharacteristic.label)
            .replace('$measurementType', populationCharacteristic.measurementType);
          defer.resolve(StudyService.doModifyingQuery(addPopulationCharacteristicQuery));
        });
        return defer.promise;
      }

      function deleteItem(item) {
        var defer = $q.defer();

        deletePopulationCharacteristicRaw.$promise.then(function(deleteQueryRaw) {
          var deleteQuery = deleteQueryRaw.data.replace(/\$URI/g, item.uri.value);
          defer.resolve(StudyService.doModifyingQuery(deleteQuery));
        });
        return defer.promise;
      }

      function editItem(item) {
        var defer = $q.defer();

        editPopulationCharacteristicRaw.$promise.then(function(editQueryRaw) {
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
    return dependencies.concat(PopulationCharacteristicService);
  });

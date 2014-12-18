'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource'];
    var EpochService = function($q, StudyService, UUIDService, SparqlResource) {

      var epochQuery = SparqlResource.get({
        name: 'queryEpoch.sparql'
      });

      var addEpochQueryRaw = SparqlResource.get({
        name: 'addEpoch.sparql'
      });

      // var deleteEpochRaw = SparqlResource.get({
      //   name: 'deleteEpoch.sparql'
      // });

      // var editEpochRaw = SparqlResource.get({
      //   name: 'editEpoch.sparql'
      // });

      function simpleDurationBuilder(durationObject) {
        var duration = "P";

        if(durationObject.periodType.type === 'time') {
          duration = duration + "T";
        }

        duration = duration + durationObject.numberOfPeriods + durationObject.periodType.value;
        return duration;
      } 

      function queryItems() {
        var defer = $q.defer();

        epochQuery.$promise.then(function(query) {
          defer.resolve(StudyService.doNonModifyingQuery(query.data));
        });
        return defer.promise;
      }

      function addItem(item) {
        var defer = $q.defer();

        addEpochQueryRaw.$promise.then(function(query) {
          var addEpochQuery = query.data
            .replace(/\$UUID/g, UUIDService.generate())
            .replace('$label', item.label)
            .replace('$measurementType', item.measurementType);
          defer.resolve(StudyService.doModifyingQuery(addEpochQuery));
        });
        return defer.promise;
      }

      // function deleteItem(item) {
      //   var defer = $q.defer();

      //   deleteEpochRaw.$promise.then(function(deleteQueryRaw) {
      //     var deleteQuery = deleteQueryRaw.data.replace(/\$URI/g, item.uri.value);
      //     defer.resolve(StudyService.doModifyingQuery(deleteQuery));
      //   });
      //   return defer.promise;
      // }

      // function editItem(item) {
      //   var defer = $q.defer();

      //   editEpochRaw.$promise.then(function(editQueryRaw) {
      //     var editQuery = editQueryRaw.data.replace(/\$URI/g, item.uri.value)
      //       .replace('$newLabel', item.label.value)
      //       .replace('$newMeasurementType', item.measurementType.value);
      //     defer.resolve(StudyService.doModifyingQuery(editQuery));
      //   });
      //   return defer.promise;
      // }

      return {
        queryItems: queryItems,
        addItem: addItem,
 //       deleteItem: deleteItem,
 //       editItem: editItem
      };
    };
    return dependencies.concat(EpochService);
  });

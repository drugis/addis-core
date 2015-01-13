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

      var addEpochCommentQueryRaw = SparqlResource.get({
        name: 'addEpochComment.sparql'
      });

      var addEpochToEndOfListQueryRaw = SparqlResource.get({
        name: 'addEpochToEndOfList.sparql'
      });

      var setEpochPrimaryQueryRaw = SparqlResource.get({
        name: 'setEpochPrimary.sparql'
      });

      // var deleteTail = SparqlResource.get({
      //   name: 'deleteTail.sparql'
      // });

      // var deleteEpochRaw = SparqlResource.get({
      //   name: 'deleteEpoch.sparql'
      // });

      var editEpochRaw = SparqlResource.get({
        name: 'editEpoch.sparql'
      });

      function simpleDurationBuilder(durationObject) {
        if (durationObject.durationType === 'instantaneous') {
          return 'PT0S';
        }

        var duration = 'P';

        if (durationObject.periodType.type === 'time') {
          duration = duration + 'T';
        }

        duration = duration + durationObject.numberOfPeriods + durationObject.periodType.value;
        return duration;
      }

      function queryItems() {
        return epochQuery.$promise.then(function(query) {
          return StudyService.doNonModifyingQuery(query.data);
        });
      }

      function addItem(item) {
        var uuid = UUIDService.generate();
        var promises = [];
        var durationString = simpleDurationBuilder(item.duration);

        // add epoch
        addEpochQueryRaw.$promise.then(function(query) {
          var addEpochQuery = query.data
            .replace(/\$newUUID/g, uuid)
            .replace('$label', item.label)
            .replace('$duration', durationString);
          promises.push(StudyService.doModifyingQuery(addEpochQuery));
        });
        // optional add comment
        if (item.comment) {
          addEpochCommentQueryRaw.$promise.then(function(query) {
            var addEpochCommentQuery = query.data
              .replace(/\$newUUID/g, uuid)
              .replace('$comment', item.comment);
            promises.push(StudyService.doModifyingQuery(addEpochCommentQuery));
          });
        }
        // optional is_primary
        if(item.isPrimary) {
          setEpochPrimaryQueryRaw.$promise.then(function(query) {
            var setEpochPrimaryQuery = query.data
              .replace(/\$newUUID/g, uuid);
            promises.push(StudyService.doModifyingQuery(setEpochPrimaryQuery));
          });
        }

        // add epoch to list of has_epochs in study
        addEpochToEndOfListQueryRaw.$promise.then(function(query) {
          var addEpochToEndOfListQuery = query.data
            .replace(/\$elementToInsert/g, uuid);
          promises.push(StudyService.doModifyingQuery(addEpochToEndOfListQuery));
        });


        // todo add optional main epoch

        return $q.all(promises);
      }

      // function deleteItem(item) {
      //   var defer = $q.defer();

      //   deleteEpochRaw.$promise.then(function(deleteQueryRaw) {
      //     var deleteQuery = deleteQueryRaw.data.replace(/\$URI/g, item.uri.value);
      //     defer.resolve(StudyService.doModifyingQuery(deleteQuery));
      //   });
      //   return defer.promise;
      // }

      function editItem(item) {
        return editEpochRaw.$promise.then(function(editQueryRaw) {
          var editQuery = editQueryRaw.data.replace(/\$URI/g, item.uri.value)
            .replace('$newLabel', item.label.value)
            .replace('$newMeasurementType', item.measurementType.value);
          return StudyService.doModifyingQuery(editQuery);
        });
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        //       deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(EpochService);
  });

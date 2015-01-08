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
        name: 'addEpochToStudyEpochList.sparql'
      });

      var getLastItemInEpochList = SparqlResource.get({
        name: 'getLastItemInEpochList.sparql'
      });

      var deleteTail = SparqlResource.get({
        name: 'deleteTail.sparql'
      });

      // var deleteEpochRaw = SparqlResource.get({
      //   name: 'deleteEpoch.sparql'
      // });

      // var editEpochRaw = SparqlResource.get({
      //   name: 'editEpoch.sparql'
      // });

      function simpleDurationBuilder(durationObject) {
        var duration = "P";

        if (durationObject.periodType.type === 'time') {
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
        var uuid = UUIDService.generate();
        var promises = [];
        var durationString = simpleDurationBuilder(item.duration);

        var lastEpochUri;

        getLastItemInEpochList.$promise.then(function(query) {
          StudyService.doNonModifyingQuery(query.data).then(function(result) {
            console.log(result);
            lastEpochUri = result[0].lastEpoch.value;
             deleteTail.$promise.then(function(query){
                StudyService.doModifyingQuery(query.data).then(function(success){
                  console.log("after delete" + success);
                  addEpochToEndOfListQueryRaw.$promise.then(function(query) {
                    var addEpochToEndOfListQuery = query.data
                    .replace(/\$elementToInsert/g, uuid)
                    .replace(/\$lastItem/g, lastEpochUri);
                    StudyService.doModifyingQuery(addEpochToEndOfListQuery).then(function(succes){
                      console.log('after insert' + succes);
                      defer.resolve(true);
                    });
                  });
                })
             });
          });
        });

        // // add epoch
        // addEpochQueryRaw.$promise.then(function(query) {
        //   var addEpochQuery = query.data
        //     .replace(/\$newUUID/g, uuid)
        //     .replace('$label', item.label)
        //     .replace('$duration', durationString);
        //   promises.push(StudyService.doModifyingQuery(addEpochQuery));
        // });

        // add epoch to list of has_epochs in study
        

        // optional add comment
        // if (item.comment) {
        //   addEpochCommentQueryRaw.$promise.then(function(query) {
        //     var addEpochCommentQuery = query.data
        //       .replace(/\$newUUID/g, uuid)
        //       .replace('$comment', item.comment);
        //     promises.push(StudyService.doModifyingQuery(addEpochCommentQuery));
        //   });
        // }

        // todo add optional main epoch

        // $q.all(promises).then(function() {
        //   defer.resolve();
        // });
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
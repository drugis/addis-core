'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource'];
    var EpochService = function($q, StudyService, UUIDService, SparqlResource) {

      var epochQuery = SparqlResource.get('queryEpoch.sparql');
      var addEpochQueryRaw = SparqlResource.get('addEpoch.sparql');
      var addEpochCommentQueryRaw = SparqlResource.get('addEpochComment.sparql');
      var addEpochToEndOfListQueryRaw = SparqlResource.get('addEpochToEndOfList.sparql');
      var setEpochPrimaryQueryRaw = SparqlResource.get('setEpochPrimary.sparql');
      var deleteEpochRaw = SparqlResource.get('deleteEpoch.sparql');
      var editEpochRaw = SparqlResource.get('editEpoch.sparql');
      var removeEpochPrimaryRaw = SparqlResource.get('removeEpochPrimary.sparql');
      var setEpochToPrimaryRaw = SparqlResource.get('setEpochToPrimary.sparql');

      function queryItems(studyUuid) {
        return epochQuery.then(function(queryRaw) {
          var query = queryRaw.replace(/\$studyUuid/g, studyUuid);
          return StudyService.doNonModifyingQuery(query);
        });
      }

      function addItem(item, studyUUID) {
        var uuid = UUIDService.generate();
        var addEpochPromise, addCommentPromise, setPrimaryPromise, addToListPromise;

        // add epoch
        addEpochPromise = addEpochQueryRaw.then(function(query) {
          var addEpochQuery = query
            .replace(/\$newUUID/g, uuid)
            .replace('$label', item.label)
            .replace('$duration', item.duration);
          return StudyService.doModifyingQuery(addEpochQuery);
        });
        // optional add comment
        if (item.comment) {
          addCommentPromise = addEpochCommentQueryRaw.then(function(query) {
            var addEpochCommentQuery = query
              .replace(/\$newUUID/g, uuid)
              .replace('$comment', item.comment);
            return StudyService.doModifyingQuery(addEpochCommentQuery);
          });
        }
        // optional is_primary
        if (item.isPrimaryEpoch) {
          setPrimaryPromise = setEpochPrimaryQueryRaw.then(function(query) {
            var setEpochPrimaryQuery = query
              .replace(/\$studyUUID/g, studyUUID)
              .replace(/\$newUUID/g, uuid);
            return StudyService.doModifyingQuery(setEpochPrimaryQuery);
          });
        }

        // add epoch to list of has_epochs in study
        addToListPromise = addEpochToEndOfListQueryRaw.then(function(query) {
          var addEpochToEndOfListQuery = query
            .replace(/\$elementToInsert/g, uuid);
          return StudyService.doModifyingQuery(addEpochToEndOfListQuery);
        });

        return $q.all([addEpochPromise,
                       addCommentPromise,
                       setPrimaryPromise,
                       addToListPromise]);
      }

      function deleteItem(item) {
        return deleteEpochRaw.then(function(deleteQueryRaw) {
          var deleteQuery = deleteQueryRaw.replace(/\$URI/g, item.uri.value);
          return StudyService.doModifyingQuery(deleteQuery);
        });
      }

      function editItem(oldItem, newItem, studyUuid) {

        var isPrimaryDefer, epochDefer;

        var newDuration = newItem.duration.value;
        var newCommentValue = newItem.comment ? newItem.comment.value : '';

        if (oldItem.isPrimary.value === 'true' && !newItem.isPrimary.value) {
          isPrimaryDefer = removeEpochPrimaryRaw.then(function(queryRaw) {
            var query = queryRaw.replace(/\$URI/g, newItem.uri.value)
              .replace(/\$studyUuid/g, studyUuid);
            return StudyService.doModifyingQuery(query);
          });
        } else if (newItem.isPrimary.value) {
          isPrimaryDefer = setEpochToPrimaryRaw.then(function(queryRaw) {
            var query = queryRaw.replace(/\$URI/g, newItem.uri.value)
              .replace(/\$studyUuid/g, studyUuid);
            return StudyService.doModifyingQuery(query);
          });
        }

        epochDefer = editEpochRaw.then(function(editQueryRaw) {
          var editQuery = editQueryRaw.replace(/\$URI/g, newItem.uri.value)
            .replace(/\$studyUuid/g, studyUuid)
            .replace(/\$newDuration/g, newDuration)
            .replace(/\$newLabel/g, newItem.label.value);
            editQuery = editQuery.replace(/\$newComment/g, newCommentValue);
          return StudyService.doModifyingQuery(editQuery);
        });

        return $q.all([isPrimaryDefer, epochDefer]);
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(EpochService);
  });

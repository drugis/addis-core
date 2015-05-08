'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource', 'SanitizeService'];
    var EpochService = function($q, StudyService, UUIDService, SparqlResource, SanitizeService) {

      var epochQuery = SparqlResource.get('queryEpoch.sparql');
      var addEpochQueryRaw = SparqlResource.get('addEpoch.sparql');
      var addEpochCommentQueryRaw = SparqlResource.get('addEpochComment.sparql');
      var addEpochToEndOfListQueryRaw = SparqlResource.get('addEpochToEndOfList.sparql');
      var setEpochPrimaryQueryRaw = SparqlResource.get('setEpochPrimary.sparql');
      var deleteEpochRaw = SparqlResource.get('deleteEpoch.sparql');
      var editEpochRaw = SparqlResource.get('editEpoch.sparql');
      var removeEpochPrimaryRaw = SparqlResource.get('removeEpochPrimary.sparql');
      var setEpochToPrimaryRaw = SparqlResource.get('setEpochToPrimary.sparql');

      function queryItems() {
        return epochQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query);
        });
      }

      function addItem(item) {
        var newItem = angular.copy(item);
        item.uuid = UUIDService.generate();

        var addEpochPromise, addCommentPromise, setPrimaryPromise, addToListPromise;

        // add epoch
        addEpochPromise = addEpochQueryRaw.then(function(query) {
          var addEpochQuery = fillInTemplate(query, newItem);
          return StudyService.doModifyingQuery(addEpochQuery);
        });
        // optional add comment
        if (item.comment) {
          addCommentPromise = addEpochCommentQueryRaw.then(function(query) {
            var addEpochCommentQuery = fillInTemplate(query, newItem);
            return StudyService.doModifyingQuery(addEpochCommentQuery);
          });
        }
        // optional is_primary
        if (item.isPrimaryEpoch) {
          setPrimaryPromise = setEpochPrimaryQueryRaw.then(function(query) {
            var setEpochPrimaryQuery = fillInTemplate(query, newItem);
            return StudyService.doModifyingQuery(setEpochPrimaryQuery);
          });
        }

        // add epoch to list of has_epochs in study
        addToListPromise = addEpochToEndOfListQueryRaw.then(function(query) {
          var addEpochToEndOfListQuery = fillInTemplate(query, newItem);
          return StudyService.doModifyingQuery(addEpochToEndOfListQuery);
        });

        return $q.all([addEpochPromise,
                        addCommentPromise,
                        setPrimaryPromise,
                        addToListPromise]);
      }

      function deleteItem(item) {
        return deleteEpochRaw.then(function(deleteQueryRaw) {
          var deleteQuery = deleteQueryRaw.replace(/\$URI/g, item.uri);
          return StudyService.doModifyingQuery(deleteQuery);
        });
      }

      function editItem(oldItem, newItem) {
        var newItemCopy = angular.copy(newItem);
        newItemCopy.comment = newItemCopy.comment ? newItemCopy.comment : '';

        var isPrimaryDefer, epochDefer;

        if (oldItem.isPrimary === 'true' && !newItemCopy.isPrimary) {
          isPrimaryDefer = removeEpochPrimaryRaw.then(function(queryRaw) {
            var query = fillInTemplate(queryRaw, newItemCopy);
            return StudyService.doModifyingQuery(query);
          });
        } else if (newItemCopy.isPrimary) {
          isPrimaryDefer = setEpochToPrimaryRaw.then(function(queryRaw) {
            var query = fillInTemplate(queryRaw, newItemCopy);
            return StudyService.doModifyingQuery(query);
          });
        }

        epochDefer = editEpochRaw.then(function(editQueryRaw) {
          var editQuery = fillInTemplate(editQueryRaw, newItemCopy);
          return StudyService.doModifyingQuery(editQuery);
        });

        return $q.all([isPrimaryDefer, epochDefer]);
      }

      function fillInTemplate(template, item) {
        return template
                .replace(/\$newUUID/g, item.uuid)
                .replace('$label', item.label)
                .replace('$duration', item.duration)
                .replace('$comment', SanitizeService.sanitizeStringLiteral(item.comment))
                .replace(/\$elementToInsert/g, item.uuid)
                .replace(/\$URI/g, item.uri)
                .replace(/\$newDuration/g, item.duration)
                .replace(/\$newLabel/g, item.label)
                .replace(/\$newComment/g, SanitizeService.sanitizeStringLiteral(item.comment))
                ;
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

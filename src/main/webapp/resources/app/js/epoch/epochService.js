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

      //http://stackoverflow.com/questions/10834796/validate-that-a-string-is-a-positive-integer
      function isNormalInteger(str) {
        return /^\+?(0|[1-9]\d*)$/.test(str);
      }

      function isValidDuration(duration, periodTypeOptions) {
        if (!duration.durationType) {
          return false;
        } else if (duration.durationType === 'instantaneous') {
          return true;
        } else if (duration.durationType === 'period') {
          var isValidType = _.find(periodTypeOptions, function(option) {
            return option.value === duration.periodType.value;
          });
          var isValidNumberOfPeriods = isNormalInteger(duration.numberOfPeriods);
          return isValidType && isValidNumberOfPeriods;
        } else {
          throw 'invalid duration type';
        }
      }

      function transformDuration(duration) {
        var transformedDuration;

        if (duration.value === 'PT0S') {
          return {
            durationType: 'instantaneous'
          };
        }

        transformedDuration = {
          durationType: 'period',
        };
        var tempDurationStr = duration.value;

        if (duration.value[1] === 'T') {
          tempDurationStr = tempDurationStr.slice(2); // remove 'PT'
        } else {
          tempDurationStr = tempDurationStr.slice(1); // remove 'P'
        }

        transformedDuration.periodType = {
          value: tempDurationStr[tempDurationStr.length - 1]
        };
        var numberOfPeriodsAsString = tempDurationStr.substr(0, tempDurationStr.length - 1);
        transformedDuration.numberOfPeriods = parseInt(numberOfPeriodsAsString, 10);

        return transformedDuration;
      }

      function queryItems(studyUuid) {
        return epochQuery.then(function(queryRaw) {
          var query = queryRaw.replace(/\$studyUuid/g, studyUuid);
          return StudyService.doNonModifyingQuery(query);
        });
      }

      function addItem(item, studyUUID) {
        var uuid = UUIDService.generate();
        var addEpochPromise, addCommentPromise, setPrimaryPromise, addToListPromise;
        var durationString = simpleDurationBuilder(item.duration);

        // add epoch
        addEpochPromise = addEpochQueryRaw.then(function(query) {
          var addEpochQuery = query
            .replace(/\$newUUID/g, uuid)
            .replace('$label', item.label)
            .replace('$duration', durationString);
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

        var newDuration = simpleDurationBuilder(newItem.duration);
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
        editItem: editItem,
        transformDuration: transformDuration,
        isValidDuration: isValidDuration
      };
    };
    return dependencies.concat(EpochService);
  });

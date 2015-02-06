'use strict';
define([],
  function() {
    var dependencies = ['$q', '$filter', 'StudyService', 'SparqlResource', 'UUIDService', 'EpochService', 'DurationService'];
    var MeasurementMomentService = function($q, $filter, StudyService, SparqlResource, UUIDService, EpochService, DurationService) {

      var measurementMomentQuery = SparqlResource.get('queryMeasurementMoment.sparql');
      var addItemQuery = SparqlResource.get('addMeasurementMoment.sparql');
      var editItemQuery = SparqlResource.get('editMeasurementMoment.sparql');
      var deleteItemQuery = SparqlResource.get('deleteMeasurementMoment.sparql');

      function queryItems(studyUuid) {
        var measurementMoments, epochs;
        var epochsPromise = EpochService.queryItems(studyUuid).then(function(result){
          epochs = result;
        });

        var measurementsMomentsPromise = measurementMomentQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            measurementMoments = result;
          });
        });

        return $q.all([epochsPromise, measurementsMomentsPromise]).then(function() {

          var unsorted =  _.map(measurementMoments, function(measurementMoment) {
            measurementMoment.epoch = _.find(epochs, function(epoch) {
              return measurementMoment.epochUri === epoch.uri;
            });
            return measurementMoment;
          });

          return sortByEpochAnchorAndDuration(unsorted);

        });
      }

      function sortByEpochAnchorAndDuration(unsorted) {
        return unsorted.sort(function(a, b) {
          if(a.epoch.pos === b.epoch.pos) {
            if(a.relativeToAnchor === b.relativeToAnchor) {
              if(a.offset === b.offset) {
                // its all the same
                return 0;
              } else {
                // sort by duration
                return DurationService.durationStringToMills(a.offset) - DurationService.durationStringToMills(b.offset);
              }
            }
            // sort by anchor
            return a.relativeToAnchor === 'http://trials.drugis.org/ontology#anchorEpochStart' ? -1 : 1;
          }
          // sort by epoch
          return a.epoch.pos - b.epoch.pos;
        });
      }


      function addItem(item) {
        return addItemQuery.then(function(rawQuery) {

          var uuid = UUIDService.generate();
          var query = rawQuery
            .replace('$newItemUuid', uuid)
            .replace('$newLabel', item.label)
            .replace('$epochUri', item.epoch.uri)
            .replace('$anchorMoment', item.relativeToAnchor)
            .replace('$timeOffset', item.offset);
          return StudyService.doModifyingQuery(query);
        });
      }

      function editItem(item) {
        return editItemQuery.then(function(rawQuery) {
          var query = rawQuery
            .replace(/\$itemUri/g, item.uri)
            .replace('$newLabel', item.label)
            .replace('$epochUri', item.epoch.uri)
            .replace('$anchorMoment', item.relativeToAnchor)
            .replace('$timeOffset', item.offset);
          return StudyService.doModifyingQuery(query);
        });
      }

      function deleteItem(item) {
        return deleteItemQuery.then(function(rawQuery) {
          var query = rawQuery
            .replace(/\$itemUri/g, item.uri);
          return StudyService.doModifyingQuery(query);
        });

      }

      function generateLabel(measurementMoment) {
        if (!measurementMoment.epoch || !measurementMoment.offset || !measurementMoment.relativeToAnchor) {
          return '';
        }
        var offsetStr = (measurementMoment.offset === 'PT0S') ? 'At' : $filter('durationFilter')(measurementMoment.offset) + ' from';
        var anchorStr = measurementMoment.relativeToAnchor === 'http://trials.drugis.org/ontology#anchorEpochStart' ? 'start' : 'end';
        return offsetStr + ' ' + anchorStr + ' of ' + measurementMoment.epoch.label;
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        editItem: editItem,
        deleteItem: deleteItem,
        generateLabel: generateLabel
      };
    };
    return dependencies.concat(MeasurementMomentService);
  });

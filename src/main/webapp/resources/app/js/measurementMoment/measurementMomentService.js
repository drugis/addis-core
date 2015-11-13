'use strict';
define([],
  function() {
    var dependencies = ['$q', '$filter', 'StudyService', 'UUIDService', 'EpochService', 'DurationService'];
    var MeasurementMomentService = function($q, $filter, StudyService, UUIDService, EpochService, DurationService) {

      // var measurementMomentQuery = SparqlResource.get('queryMeasurementMoment.sparql');
      // var addItemQuery = SparqlResource.get('addMeasurementMoment.sparql');
      // var editItemQuery = SparqlResource.get('editMeasurementMoment.sparql');
      // var deleteItemQuery = SparqlResource.get('deleteMeasurementMoment.sparql');

      function toFrontend(backendItem) {
        var frontendItem = {
          uri: backendItem['@id'],
          label: backendItem.label
        };

        if (backendItem.relative_to_epoch) {
          frontendItem.epochUri = backendItem.relative_to_epoch;
        }

        if (backendItem.relative_to_anchor) {
          frontendItem.relativeToAnchor = backendItem.relative_to_anchor
        }

        if (backendItem.time_offset) {
          frontendItem.offset = backendItem.time_offset;
        }

        return frontendItem;
      }

      function isMeasurementMoment(node) {
        return 'ontology:MeasurementMoment' === node['@type'];
      }

      function queryItems() {

        var queryEpochs = EpochService.queryItems().then(function(result) {
          return result;
        });

        var queryMeasurementMoments = StudyService.getJsonGraph().then(function(graph) {
          return _.filter(graph, isMeasurementMoment).map(toFrontend);
        });

        return $q.all([queryEpochs, queryMeasurementMoments]).then(function(results) {

          var epochs = results[0];
          var measurementMoments = results[1];

          var unsorted = _.map(measurementMoments, function(measurementMoment) {
            measurementMoment.epoch = _.find(epochs, function(epoch) {
              return measurementMoment.epochUri === epoch.uri;
            });
            measurementMoment.epochLabel = measurementMoment.epoch.label;
            return measurementMoment;
          });

          return unsorted.length > 1 ? sortByEpochAnchorAndDuration(unsorted): unsorted;
        });
      }

      function sortByEpochAnchorAndDuration(unsorted) {
        return unsorted.sort(function(a, b) {

          // item contains a properties to be sorted
          function isSortable(item) {
            return item.epoch && item.relativeToAnchor && item.offset
          }

          // move un-sortable items to the back
          if (!isSortable(a) && !isSortable(b)) {
            return 0;
          } else if (!isSortable(a)) {
            return 1;
          } else if (!isSortable(b)) {
            return -1;
          }

          // sort only the sortable items
          if (a.epoch.pos === b.epoch.pos) {
            if (a.relativeToAnchor === b.relativeToAnchor) {
              if (a.offset === b.offset) {
                // its all the same
                return 0;
              } else {
                // sort by duration
                return DurationService.durationStringToMills(a.offset) - DurationService.durationStringToMills(b.offset);
              }
            }
            // sort by anchor
            return a.relativeToAnchor === 'ontology:anchorEpochStart' ? -1 : 1;
          }
          // sort by epoch
          return a.epoch.pos - b.epoch.pos;
        });
      }


      function addItem(item) {
        // var newItem = angular.copy(item);
        // newItem.uuid = UUIDService.generate();
        // return addItemQuery.then(function(template) {
        //   return StudyService.doModifyingQuery(fillTemplate(template, newItem));
        // });
      }

      function editItem(item) {
        // return editItemQuery.then(function(template) {
        //   return StudyService.doModifyingQuery(fillTemplate(template, item));
        // });
      }

      function deleteItem(item) {
        // return deleteItemQuery.then(function(template) {
        //   return StudyService.doModifyingQuery(fillTemplate(template, item));
        // });

      }

      function generateLabel(measurementMoment) {
        if (!measurementMoment.epoch || !measurementMoment.offset || !measurementMoment.relativeToAnchor) {
          return '';
        }
        var offsetStr = (measurementMoment.offset === 'PT0S') ? 'At' : $filter('durationFilter')(measurementMoment.offset) + ' from';
        var anchorStr = measurementMoment.relativeToAnchor === 'http://trials.drugis.org/ontology#anchorEpochStart' ? 'start' : 'end';
        return offsetStr + ' ' + anchorStr + ' of ' + measurementMoment.epoch.label;
      }

      // function fillTemplate(template, item) {
      //   var query = template
      //     .replace(/\$newItemUuid/g, item.uuid)
      //     .replace(/\$itemUri/g, item.uri)
      //     .replace(/\$newLabel/g, item.label)
      //     .replace(/\$anchorMoment/g, item.relativeToAnchor)
      //     .replace(/\$timeOffset/g, item.offset);
      //   if(item.epoch) {
      //     query = query.replace(/\$epochUri/g, item.epoch.uri)
      //   }
      //   return query;
      // }

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
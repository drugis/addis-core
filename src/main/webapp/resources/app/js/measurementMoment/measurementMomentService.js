'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$q', '$filter', 'StudyService', 'UUIDService', 'EpochService', 'DurationService'];
    var MeasurementMomentService = function($q, $filter, StudyService, UUIDService, EpochService, DurationService) {

      function toFrontend(backendItem) {
        var frontendItem = {
          uri: backendItem['@id'],
          label: backendItem.label
        };

        if (backendItem.relative_to_epoch) {
          frontendItem.epochUri = backendItem.relative_to_epoch;
        }

        if (backendItem.relative_to_anchor) {
          frontendItem.relativeToAnchor = backendItem.relative_to_anchor;
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

          return unsorted.length > 1 ? sortByEpochAnchorAndDuration(unsorted) : unsorted;
        });
      }

      function sortByEpochAnchorAndDuration(unsorted) {
        return unsorted.sort(function(a, b) {

          // item contains properties to be sorted
          function isSortable(item) {
            return item.epoch && item.relativeToAnchor && item.offset;
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
        var measurementMoment = {
          '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
          '@type': 'ontology:MeasurementMoment',
          label: item.label
        };

        if (item.relativeToAnchor) {
          measurementMoment.relative_to_anchor = item.relativeToAnchor;
        }

        if (item.offset) {
          measurementMoment.time_offset = item.offset;
        }

        if (item.epoch) {
          measurementMoment.relative_to_epoch = item.epoch.uri;
        }

        return StudyService.getJsonGraph().then(function(graph) {
          return StudyService.saveJsonGraph(graph.concat(measurementMoment));
        });
      }

      function editItem(item) {
        return StudyService.getJsonGraph().then(function(graph) {

          var removed = _.remove(graph, function(node) {
            return item.uri === node['@id'];
          });

          var editItem = {
            '@id': removed[0]['@id'],
            '@type': 'ontology:MeasurementMoment',
            label: item.label
          };

          if (item.relativeToAnchor) {
            editItem.relative_to_anchor = item.relativeToAnchor;
          }

          if (item.offset) {
            editItem.time_offset = item.offset;
          }

          if (item.epoch) {
            editItem.relative_to_epoch = item.epoch.uri;
          }

          return StudyService.saveJsonGraph(graph.concat(editItem));
        });
      }

      function deleteResultsForMoment(graph, momentUri) {
        _.remove(graph, function(node) {
          return momentUri === node.of_moment;
        });
      }

      function cleanUpOutcomes(graph, momentUri) {
        var study = _.find(graph, function(node) {
          return node['@type'] === 'ontology:Study';
        });
        study.has_outcome = _.map(study.has_outcome, function(outcome) {
          if(outcome.is_measured_at) {
            // single is_measured_at is a string, not an array of strings for whatever reason.
            if(Array.isArray(outcome.is_measured_at)) {
              outcome.is_measured_at = _.filter(outcome.is_measured_at, function(measurementUri) {
                return measurementUri !== momentUri;
              });
              if (outcome.is_measured_at.length === 1) {
                outcome.is_measured_at = outcome.is_measured_at[0];
              }
            } else {
              if(outcome.is_measured_at === momentUri) {
                delete outcome.is_measured_at;
              }
            }
          }
          return outcome;
        });
      }

      function deleteItem(item) {
        return StudyService.getJsonGraph().then(function(graph) {
          _.remove(graph, function(node) {
            return item.uri === node['@id'];
          });

          deleteResultsForMoment(graph, item.uri);

          cleanUpOutcomes(graph, item.uri);

          return StudyService.saveJsonGraph(graph);
        });
      }

      function generateLabel(measurementMoment) {
        if (!measurementMoment.epoch || !measurementMoment.offset || !measurementMoment.relativeToAnchor) {
          return '';
        }
        var offsetStr = (measurementMoment.offset === 'PT0S') ? 'At' : $filter('durationFilter')(measurementMoment.offset) + ' from';
        var anchorStr = measurementMoment.relativeToAnchor === 'ontology:anchorEpochStart' ? 'start' : 'end';
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

'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = ['StudyService', 'UUIDService'];
  var ResultsService = function(StudyService, UUIDService) {

    var INTEGER_TYPE = '<http://www.w3.org/2001/XMLSchema#integer>';
    var DOUBLE_TYPE = '<http://www.w3.org/2001/XMLSchema#double>';

    var VARIABLE_TYPES = ['sample_size',
      'mean',
      'median',
      'geometric_mean',
      'log_mean',
      'least_squares_mean',
      'quantile_0.05',
      'quantile_0.95',
      'quantile_0.025',
      'quantile_0.975',
      'min',
      'max',
      'geometric_coefficient_of_variation',
      'first_quartile',
      'third_quartile',
      'standard_deviation',
      'standard_error',
      'count',
      'percentage',
      'proportion',
    ];

    var VARIABLE_TYPE_DETAILS = {
      'sample_size': {
        type: 'sample_size',
        label: 'sample size',
        uri: 'http://trials.drugis.org/ontology#sample_size',
        dataType: INTEGER_TYPE
      },
      'mean': {
        type: 'mean',
        label: 'mean',
        uri: 'http://trials.drugis.org/ontology#mean',
        dataType: DOUBLE_TYPE
      },
      'median': {
        type: 'median',
        label: 'median',
        uri: 'http://trials.drugis.org/ontology#median',
        dataType: DOUBLE_TYPE
      },
      'geometric_mean': {
        type: 'geometric_mean',
        label: 'geometric mean',
        uri: 'http://trials.drugis.org/ontology#geometric_mean',
        dataType: DOUBLE_TYPE
      },
      'log_mean': {
        type: 'geometric_mean',
        label: 'geometric mean',
        uri: 'http://trials.drugis.org/ontology#geometric_mean',
        dataType: DOUBLE_TYPE
      },
      'least_squares_mean': {
        type: 'least_squares_mean',
        label: 'least squares mean',
        uri: 'http://trials.drugis.org/ontology#least_squares_mean',
        dataType: DOUBLE_TYPE
      },
      'quantile_0.05': {
        type: 'quantile_0.05',
        label: 'quantile 0.05',
        uri: 'http://trials.drugis.org/ontology#quantile_0.05',
        dataType: DOUBLE_TYPE
      },
      'quantile_0.95': {
        type: 'quantile_0.95',
        label: 'quantile 0.95',
        uri: 'http://trials.drugis.org/ontology#quantile_0.95',
        dataType: DOUBLE_TYPE
      },
      'quantile_0.025': {
        type: 'quantile_0.025',
        label: 'quantile 0.025',
        uri: 'http://trials.drugis.org/ontology#quantile_0.025',
        dataType: DOUBLE_TYPE
      },
      'quantile_0.975': {
        type: 'quantile_0.975',
        label: 'quantile 0.975',
        uri: 'http://trials.drugis.org/ontology#quantile_0.975',
        dataType: DOUBLE_TYPE
      },
      'min': {
        type: 'min',
        label: 'min',
        uri: 'http://trials.drugis.org/ontology#min',
        dataType: DOUBLE_TYPE
      },
      'max': {
        type: 'max',
        label: 'max',
        uri: 'http://trials.drugis.org/ontology#max',
        dataType: DOUBLE_TYPE
      },
      'geometric_coefficient_of_variation': {
        type: 'geometric_coefficient_of_variation',
        label: 'geometric coefficient of variation',
        uri: 'http://trials.drugis.org/ontology#geometric_coefficient_of_variation',
        dataType: DOUBLE_TYPE
      },
      'first_quartile': {
        type: 'first_quartile',
        label: 'first quartile',
        uri: 'http://trials.drugis.org/ontology#first_quartile',
        dataType: DOUBLE_TYPE
      },
      'third_quartile': {
        type: 'third_quartile',
        label: 'third quartile',
        uri: 'http://trials.drugis.org/ontology#third_quartile',
        dataType: DOUBLE_TYPE
      },
      'standard_deviation': {
        type: 'standard_deviation',
        label: 'standard deviation',
        uri: 'http://trials.drugis.org/ontology#standard_deviation',
        dataType: DOUBLE_TYPE
      },
      'standard_error': {
        type: 'standard_error',
        label: 'standard error',
        uri: 'http://trials.drugis.org/ontology#standard_error',
        dataType: DOUBLE_TYPE
      },
      'count': {
        type: 'count',
        label: 'count',
        uri: 'http://trials.drugis.org/ontology#count',
        dataType: INTEGER_TYPE
      },
      'percentage': {
        type: 'percentage',
        label: 'percentage',
        uri: 'http://trials.drugis.org/ontology#percentage',
        dataType: DOUBLE_TYPE
      },
      'proportion': {
        type: 'proportion',
        label: 'proportion',
        uri: 'http://trials.drugis.org/ontology#proportion',
        dataType: DOUBLE_TYPE
      },
    };

    function getVariableDetails(variableTypeUri) {
      return _.find(VARIABLE_TYPE_DETAILS, ['uri', variableTypeUri]);
    }

    function updateResultValue(row, inputColumn) {
      return StudyService.getJsonGraph().then(function(graph) {
        if (!row.uri) {
          if (inputColumn.value) {
            // create branch
            var addItem = {
              '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
              of_group: row.group.armURI || row.group.groupUri,
              of_moment: row.measurementMoment.uri,
              of_outcome: row.variable.uri
            };
            addItem[inputColumn.valueName] = inputColumn.value;
            StudyService.saveJsonGraph(graph.concat(addItem));

            return addItem['@id'];
          } else {
            return undefined;
          }
        } else {
          // update branch
          var editItem = _.remove(graph, function(node) {
            return row.uri === node['@id'];
          })[0];

          if (inputColumn.value === null) {
            delete editItem[inputColumn.valueName];
          } else {
            editItem[inputColumn.valueName] = inputColumn.value;
          }

          if (!isEmptyResult(editItem)) {
            graph.push(editItem);
          } else {
            row.uri = undefined;
          }

          StudyService.saveJsonGraph(graph);
          return row.uri;
        }
      });
    }

    function isEmptyResult(item) {
      return !item.sample_size && !item.count && !item.standard_deviation && !item.mean;
    }

    function createValueItem(baseItem, backEndItem, type) {
      var valueItem = angular.copy(baseItem);
      valueItem.result_property = type;
      valueItem.value = backEndItem[type];
      return valueItem;
    }

    function toFrontend(accum, backEndItem) {
      // ?instance ?armUri ?momentUri ?result_property ?value
      var baseItem = {
        instance: backEndItem['@id'],
        armUri: backEndItem.of_group,
        momentUri: backEndItem.of_moment,
        outcomeUri: backEndItem.of_outcome,
      };

      if (isNonConformantMeasurementResult(backEndItem)) {
        baseItem.comment = backEndItem.comment;
      }

      _.each(VARIABLE_TYPES, function(variable_type) {
        if (backEndItem[variable_type] !== undefined) {
          accum.push(createValueItem(baseItem, backEndItem, variable_type));
        }
      });

      return accum;
    }

    function isResultForVariable(variableUri, item) {
      return isResult(item) && variableUri === item.of_outcome;
    }

    function isResultForNonConformantMeasurementOfOutcome(variableUri, item) {
      return isNonConformantMeasurementResult(item) && variableUri === item.of_outcome;
    }

    function isResultForNonConformantMeasurementOfGroup(groupUri, item) {
      return isNonConformantMeasurementResult(item) && groupUri === item.of_group;
    }

    function isResultForArm(armUri, item) {
      return isResult(item) && armUri === item.of_group;
    }

    function isResultForOutcome(outcomeUri, item) {
      return isResult(item) && outcomeUri === item.of_outcome;
    }

    function isResultForMeasurementMoment(measurementMomentUri, item) {
      return isResult(item) && measurementMomentUri === item.of_moment;
    }

    function isStudyNode(node) {
      return node['@type'] === 'ontology:Study';
    }

    function isResult(node) {
      return node.of_outcome && node.of_group && node.of_moment;
    }

    function isNonConformantMeasurementResult(node) {
      return node.comment && node.of_outcome && node.of_group && !node.of_moment;
    }

    function isMoment(node) {
      return node['@type'] === 'ontology:MeasurementMoment';
    }

    function cleanupMeasurements() {
      return StudyService.getJsonGraph().then(function(graph) {
        // first get all the info we need
        var study;
        var hasArmMap;
        var hasGroupMap;
        var isMomentMap = {};
        var hasOutcomeMap;
        var isMeasurementOnOutcome = {};

        _.each(graph, function(node) {
          if (isStudyNode(node)) {
            study = node;
          }

          if (isMoment(node)) {
            isMomentMap[node['@id']] = true;
          }
        });

        hasArmMap = study.has_arm.reduce(function(accum, item) {
          accum[item['@id']] = true;
          return accum;
        }, {});

        hasGroupMap = study.has_group.reduce(function(accum, item) {
          accum[item['@id']] = true;
          return accum;
        }, {});

        if (study.has_included_population) {
          hasGroupMap[study.has_included_population[0]['@id']] = true;
        }

        hasOutcomeMap = study.has_outcome.reduce(function(accum, item) {
          accum[item['@id']] = true;
          return accum;
        }, {});

        // add al measurements that are selected on at least one outcome to the isMeasurementOnOutcome map
        isMeasurementOnOutcome = _.reduce(study.has_outcome, function(accum, outcome) {
          var measuredAtList;
          if (!Array.isArray(outcome.is_measured_at)) {
            measuredAtList = [outcome.is_measured_at];
          } else {
            measuredAtList = outcome.is_measured_at || [];
          }
          _.forEach(measuredAtList, function(measurementMomentUri) {
            isMeasurementOnOutcome[measurementMomentUri] = true;
          });
          return accum;
        }, isMeasurementOnOutcome);

        // now its time for cleaning
        var filteredGraph = _.filter(graph, function(node) {
          if (isResult(node)) {
            return (hasArmMap[node.of_group] || hasGroupMap[node.of_group]) &&
              isMomentMap[node.of_moment] &&
              hasOutcomeMap[node.of_outcome] &&
              isMeasurementOnOutcome[node.of_moment];
          } else {
            return true;
          }
        });

        return StudyService.saveJsonGraph(filteredGraph);
      });
    }

    function setToMeasurementMoment(measurementMomentUri, measurementInstanceList) {
      return StudyService.getJsonGraph().then(function(graph) {

        _.each(graph, function(node) {
          if (measurementInstanceList.indexOf(node['@id']) > -1) {
            node.of_moment = measurementMomentUri;
            delete node.comment;
          }
        });

        return StudyService.saveJsonGraph(graph);
      });
    }

    function isExistingMeasurement(measurementMomentUri, measurementInstanceList) {
      var nonConformantMeasurementInstance = measurementInstanceList[0];
      return StudyService.getJsonGraph().then(function(graph) {

        var nonConformantMeasurement = _.find(graph, function(node) {
          return node['@id'] === nonConformantMeasurementInstance;
        });

        return !!_.find(graph, function(node) {
          return isResult(node) &&
            node.of_moment === measurementMomentUri &&
            nonConformantMeasurement.of_group === node.of_group &&
            nonConformantMeasurement.of_outcome === node.of_outcome;
        });
      });
    }

    function _queryResults(uri, typeFunction) {
      return StudyService.getJsonGraph().then(function(graph) {
        var resultJsonItems = graph.filter(typeFunction.bind(this, uri));
        return resultJsonItems.reduce(toFrontend, []);
      });
    }

    function queryResults(variableUri) {
      return _queryResults(variableUri, isResultForVariable);
    }

    function queryResultsByGroup(armUri) {
      return _queryResults(armUri, isResultForArm);
    }

    function queryResultsByOutcome(outcomeUri) {
      return _queryResults(outcomeUri, isResultForOutcome);
    }

    function queryResultsByMeasurementMoment(measurementMomentUri) {
      return _queryResults(measurementMomentUri, isResultForMeasurementMoment);
    }

    function queryNonConformantMeasurementsByOutcomeUri(outcomeUri) {
      return _queryResults(outcomeUri, isResultForNonConformantMeasurementOfOutcome);
    }

    function queryNonConformantMeasurementsByGroupUri(groupUri) {
      return _queryResults(groupUri, isResultForNonConformantMeasurementOfGroup);
    }

    return {
      updateResultValue: updateResultValue,
      queryResults: queryResults,
      queryResultsByGroup: queryResultsByGroup,
      queryResultsByOutcome: queryResultsByOutcome,
      queryResultsByMeasurementMoment: queryResultsByMeasurementMoment,
      queryNonConformantMeasurementsByOutcomeUri: queryNonConformantMeasurementsByOutcomeUri,
      queryNonConformantMeasurementsByGroupUri: queryNonConformantMeasurementsByGroupUri,
      cleanupMeasurements: cleanupMeasurements,
      setToMeasurementMoment: setToMeasurementMoment,
      isExistingMeasurement: isExistingMeasurement,
      isStudyNode: isStudyNode,
      getVariableDetails: getVariableDetails,
      INTEGER_TYPE: INTEGER_TYPE,
      DOUBLE_TYPE: DOUBLE_TYPE
    };
  };
  return dependencies.concat(ResultsService);
});

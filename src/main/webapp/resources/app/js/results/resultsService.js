'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = ['StudyService', 'RdfListService', 'UUIDService'];
  var ResultsService = function(StudyService, RdfListService, UUIDService) {

    var INTEGER_TYPE = '<http://www.w3.org/2001/XMLSchema#integer>';
    var DOUBLE_TYPE = '<http://www.w3.org/2001/XMLSchema#double>';
    var ONTOLOGY_BASE = 'http://trials.drugis.org/ontology#';

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
      'event_count',
      'percentage',
      'proportion',
      'exposure',
      'hazard_ratio'
    ];

    var VARIABLE_TYPE_DETAILS = {
      'sample_size': {
        type: 'sample_size',
        label: 'N',
        uri: 'http://trials.drugis.org/ontology#sample_size',
        dataType: INTEGER_TYPE,
        variableTypes: ['ontology:continuous', 'ontology:dichotomous'],
        category: 'Sample size',
        lexiconKey: 'sample-size',
        analysisReady: true
      },
      'mean': {
        type: 'mean',
        label: 'mean',
        uri: 'http://trials.drugis.org/ontology#mean',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Central tendency',
        lexiconKey: 'mean',
        analysisReady: true
      },
      'median': {
        type: 'median',
        label: 'median',
        uri: 'http://trials.drugis.org/ontology#median',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Central tendency',
        lexiconKey: 'median',
        analysisReady: false
      },
      'geometric_mean': {
        type: 'geometric_mean',
        label: 'geometric mean',
        uri: 'http://trials.drugis.org/ontology#geometric_mean',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Central tendency',
        lexiconKey: 'geometric-mean',
        analysisReady: false
      },
      'log_mean': {
        type: 'log_mean',
        label: 'log mean',
        uri: 'http://trials.drugis.org/ontology#log_mean',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Central tendency',
        lexiconKey: 'log-mean',
        analysisReady: false
      },
      'least_squares_mean': {
        type: 'least_squares_mean',
        label: 'least squares mean',
        uri: 'http://trials.drugis.org/ontology#least_squares_mean',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Central tendency',
        lexiconKey: 'least-squares-mean',
        analysisReady: false
      },
      hazard_ratio: {
        type: 'hazard_ratio',
        label: 'hazard ratio',
        uri: 'http://trials.drugis.org/ontology#hazard_ratio',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:survival'],
        lexiconKey: 'hazard-ratio',
        analysisReady: false
      },
      'quantile_0.05': {
        type: 'quantile_0.05',
        label: '5% quantile',
        uri: 'http://trials.drugis.org/ontology#quantile_0.05',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Quantiles',
        lexiconKey: 'quantile-0.05',
        analysisReady: false
      },
      'quantile_0.95': {
        type: 'quantile_0.95',
        label: '95% quantile',
        uri: 'http://trials.drugis.org/ontology#quantile_0.95',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Quantiles',
        lexiconKey: 'quantile-0.95',
        analysisReady: false
      },
      'quantile_0.025': {
        type: 'quantile_0.025',
        label: '2.5% quantile',
        uri: 'http://trials.drugis.org/ontology#quantile_0.025',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous', 'ontology:survival'],
        category: 'Quantiles',
        lexiconKey: 'quantile-0.025',
        analysisReady: false
      },
      'quantile_0.975': {
        type: 'quantile_0.975',
        label: '97.5% quantile',
        uri: 'http://trials.drugis.org/ontology#quantile_0.975',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous', 'ontology:survival'],
        category: 'Quantiles',
        lexiconKey: 'quantile-0.975',
        analysisReady: false
      },
      'min': {
        type: 'min',
        label: 'min',
        uri: 'http://trials.drugis.org/ontology#min',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Dispersion',
        lexiconKey: 'min',
        analysisReady: false
      },
      'max': {
        type: 'max',
        label: 'max',
        uri: 'http://trials.drugis.org/ontology#max',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Dispersion',
        lexiconKey: 'max',
        analysisReady: false
      },
      'geometric_coefficient_of_variation': {
        type: 'geometric_coefficient_of_variation',
        label: 'geometric coefficient of variation',
        uri: 'http://trials.drugis.org/ontology#geometric_coefficient_of_variation',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Dispersion',
        lexiconKey: 'geometric-coefficient-of-variation',
        analysisReady: false
      },
      'first_quartile': {
        type: 'first_quartile',
        label: 'first quartile',
        uri: 'http://trials.drugis.org/ontology#first_quartile',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Quantiles',
        lexiconKey: 'first-quartile',
        analysisReady: false
      },
      'third_quartile': {
        type: 'third_quartile',
        label: 'third quartile',
        uri: 'http://trials.drugis.org/ontology#third_quartile',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Quantiles',
        lexiconKey: 'third-quartile',
        analysisReady: false
      },
      'standard_deviation': {
        type: 'standard_deviation',
        label: 'standard deviation',
        uri: 'http://trials.drugis.org/ontology#standard_deviation',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Dispersion',
        lexiconKey: 'standard-deviation',
        analysisReady: true
      },
      'standard_error': {
        type: 'standard_error',
        label: 'standard error',
        uri: 'http://trials.drugis.org/ontology#standard_error',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:continuous'],
        category: 'Dispersion',
        lexiconKey: 'standard-error',
        analysisReady: true
      },
      'event_count': {
        type: 'event_count',
        label: 'number of events',
        uri: 'http://trials.drugis.org/ontology#event_count',
        dataType: INTEGER_TYPE,
        variableTypes: ['ontology:dichotomous'],
        lexiconKey: 'event-count',
        analysisReady: false
      },
      'count': {
        type: 'count',
        label: 'subjects with event',
        uri: 'http://trials.drugis.org/ontology#count',
        dataType: INTEGER_TYPE,
        variableTypes: ['ontology:dichotomous', 'ontology:survival'],
        lexiconKey: 'count',
        analysisReady: true
      },
      'percentage': {
        type: 'percentage',
        label: 'percentage with event',
        uri: 'http://trials.drugis.org/ontology#percentage',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:dichotomous'],
        lexiconKey: 'percentage',
        analysisReady: false
      },
      'proportion': {
        type: 'proportion',
        label: 'proportion with event',
        uri: 'http://trials.drugis.org/ontology#proportion',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:dichotomous'],
        lexiconKey: 'proportion',
        analysisReady: false
      },
      exposure: {
        type: 'exposure',
        label: 'total observation time',
        uri: 'http://trials.drugis.org/ontology#exposure',
        dataType: DOUBLE_TYPE,
        variableTypes: ['ontology:survival'],
        lexiconKey: 'exposure',
        analysisReady: true
      }
    };

    var TIME_SCALE_OPTIONS = [{
      label: 'Days',
      duration: 'P1D'
    }, {
      label: 'Weeks',
      duration: 'P1W'
    }, {
      label: 'Months',
      duration: 'P1M'
    }, {
      label: 'Years',
      duration: 'P1Y'
    }];

    function getVariableDetails(variableTypeUri) {
      var normalisedUri = variableTypeUri;
      if (!_.startsWith(variableTypeUri, ONTOLOGY_BASE)) {
        normalisedUri = _.replace(normalisedUri, 'ontology:', ONTOLOGY_BASE);
      }
      return _.find(VARIABLE_TYPE_DETAILS, ['uri', normalisedUri]);
    }

    function updateValue(row, inputColumn) {
      var inputColumnVariableDetails = getVariableDetails(inputColumn.resultProperty);
      return StudyService.getJsonGraph().then(function(graph) {
        if (!row.uri) { // create branch
          if (inputColumn.value !== null && inputColumn.value !== undefined) {
            var addItem = {
              '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
              of_group: row.group.armURI || row.group.groupUri,
              of_moment: row.measurementMoment.uri,
              of_outcome: row.variable.uri
            };
            addItem[inputColumnVariableDetails.type] = inputColumn.value;
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
            delete editItem[inputColumnVariableDetails.type];
          } else {
            editItem[inputColumnVariableDetails.type] = inputColumn.value;
          }

          if (!isEmptyResult(editItem)) {
            graph.push(editItem);
          } else {
            delete row.uri;
          }
        }
        StudyService.saveJsonGraph(graph);
        return row.uri;
      });
    }

    function updateCategoricalValue(row, inputColumn) {
      return StudyService.getJsonGraph().then(function(graph) {
        if (!row.uri) {
          if (inputColumn.value !== null && inputColumn.value !== undefined) {
            // create branch
            var addItem = {
              '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
              of_group: row.group.armURI || row.group.groupUri,
              of_moment: row.measurementMoment.uri,
              of_outcome: row.variable.uri,
              category_count: [{
                count: inputColumn.value,
                category: inputColumn.resultProperty['@id']
              }]
            };
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

          var countItem = _.find(editItem.category_count, function(countItem) {
            return countItem.category === inputColumn.resultProperty['@id'];
          });
          if (countItem) {
            if (inputColumn.value === null) {
              editItem.category_count = _.reject(editItem.category_count, function(count) {
                return count.category === inputColumn.resultProperty['@id'];
              });
            } else {
              countItem.count = inputColumn.value;
            }
          } else {
            countItem = {
              count: inputColumn.value,
              category: inputColumn.resultProperty['@id']
            };
            editItem.category_count.push(countItem);
          }

          if (!isEmptyCategoricalResult(editItem)) {
            graph.push(editItem);
          } else {
            delete row.uri;
          }
        }
        StudyService.saveJsonGraph(graph);
        return row.uri;
      });
    }

    function updateResultValue(row, inputColumn) {
      if (!inputColumn.isCategory) {
        return updateValue(row, inputColumn);
      } else {
        return updateCategoricalValue(row, inputColumn);
      }
    }

    function isEmptyCategoricalResult(item) {
      return !_.some(item.category_count, function(categoryItem) {
        return categoryItem.count !== undefined;
      });
    }

    function isEmptyResult(item) {
      return !_.some(VARIABLE_TYPES, function(type) {
        return item[type] !== undefined;
      });
    }

    function createValueItem(baseItem, backEndItem, type) {
      var valueItem = angular.copy(baseItem);
      valueItem.result_property = type;
      valueItem.value = backEndItem[type];
      return valueItem;
    }

    function createCategoryItem(baseItem, backEndItem, categoryItem) {
      var valueItem = angular.copy(baseItem);
      valueItem.isCategorical = true;
      valueItem.result_property = categoryItem.category && categoryItem.category.label ? categoryItem.label : categoryItem;
      valueItem.value = categoryItem.count;
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

      _.each(VARIABLE_TYPES, function(variableType) {
        if (backEndItem[variableType] !== undefined) {
          accum.push(createValueItem(baseItem, backEndItem, variableType));
        }
      });

      if (backEndItem.category_count) {
        _.each(backEndItem.category_count, function(categoryCount) {
          accum.push(createCategoryItem(baseItem, backEndItem, categoryCount));
        });
      }

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

    function hasValues(node) {
      if (!node.category_count) {
        return _.keys(_.pick(node, VARIABLE_TYPES)).length > 0;
      } else {
        return node.category_count.length && _.find(node.category_count, function(countNode) {
          return countNode.count !== null && countNode.count !== undefined;
        });
      }
    }


    function cleanupMeasurements() {
      return StudyService.getJsonGraph().then(function(graph) {
        // first get all the info we need
        var study;
        var hasArmMap;
        var hasGroupMap;
        var isMomentMap = {};
        var outcomeMap;
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

        outcomeMap = _.keyBy(study.has_outcome, '@id');

        // add all measurements that are selected on at least one outcome to the isMeasurementOnOutcome map
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

        // remove properties that are no longer measured by the outcome
        var filteredGraph = _.map(graph, function(node) {
          if (isResult(node) && outcomeMap[node.of_outcome]) {
            if (node.category_count) {
              var categoryIds = _.reduce(study.has_outcome, function(accum, outcome) {
                var categories = RdfListService.flattenList(outcome.of_variable[0].categoryList);
                return accum.concat(_.map(categories, '@id'));
              }, []);
              node.category_count = _.filter(node.category_count, function(countObject) {
                return _.includes(categoryIds, countObject.category);
              });
            } else {
              var resultProperties = _.keys(_.pick(node, VARIABLE_TYPES));
              resultProperties = _.map(resultProperties, function(resultProperty) {
                return VARIABLE_TYPE_DETAILS[resultProperty];
              });
              var missingProperties = _.filter(resultProperties, function(resultProperty) {
                return !_.includes(outcomeMap[node.of_outcome].has_result_property, resultProperty.uri);
              });
              return _.omit(node, _.map(missingProperties, 'type'));
            }
            return node;
          } else {
            return node;
          }
        });

        // now it's time for cleaning
        filteredGraph = _.filter(filteredGraph, function(node) {
          if (isResult(node)) {
            return (hasArmMap[node.of_group] || hasGroupMap[node.of_group]) &&
              isMomentMap[node.of_moment] &&
              outcomeMap[node.of_outcome] &&
              isMeasurementOnOutcome[node.of_moment] &&
              hasValues(node);
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
          if (_.includes(measurementInstanceList, node['@id'])) {
            node.of_moment = measurementMomentUri;
            delete node.comment;
          }
        });
        return StudyService.saveJsonGraph(graph);
      });
    }

    function moveMeasurementMoment(fromUri, toUri, variableUri, rowLabel) {
      return StudyService.getJsonGraph().then(function(graph) {
        var updatedGraph;
        if (!toUri) {
          updatedGraph = _.map(graph, function(node) {
            if (node.of_moment === fromUri && node.of_outcome === variableUri) {
              delete node.of_moment;
              node.comment = rowLabel;
            }
            return node;
          });
        } else {
          // first delete all old measurements at target coordinates
          var filteredGraph = _.reject(graph, function(node) {
            return node.of_moment === toUri && node.of_outcome === variableUri;
          });
          updatedGraph = _.map(filteredGraph, function(node) {
            if (node.of_moment === fromUri && node.of_outcome === variableUri) {
              node.of_moment = toUri;
            }
            return node;
          });
        }
        return StudyService.saveJsonGraph(updatedGraph);

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

    function getDefaultResultProperties(measurementType) {
      if (measurementType === 'ontology:continuous') {
        return [
          VARIABLE_TYPE_DETAILS.sample_size,
          VARIABLE_TYPE_DETAILS.mean,
          VARIABLE_TYPE_DETAILS.standard_deviation
        ];
      } else if (measurementType === 'ontology:dichotomous') {
        return [
          VARIABLE_TYPE_DETAILS.sample_size,
          VARIABLE_TYPE_DETAILS.count
        ];
      } else if (measurementType === 'ontology:categorical') {
        return [];
      } else if (measurementType === 'ontology:survival') {
        return [
          VARIABLE_TYPE_DETAILS.count,
          VARIABLE_TYPE_DETAILS.exposure
        ]
      } else {
        console.error('unknown measurement type ' + measurementType);
      }
    }

    function getResultPropertiesForType(measurementType) {
      return _.filter(VARIABLE_TYPE_DETAILS, function(varType) {
        return varType.variableTypes === 'all' || varType.variableTypes.indexOf(measurementType) > -1;
      });
    }

    function buildPropertyCategories(variable) {
      var properties = getResultPropertiesForType(variable.measurementType);
      var categories = _(properties)
        .keyBy('category')
        .reduce(function(accum, resultProperty, categoryName) {
          var categoryProperties = _(properties)
            .filter(['category', categoryName])
            .map(function(property) {
              return _.extend({}, property, {
                isSelected: !!_.find(variable.selectedResultProperties, ['type', property.type])
              });
            }).value();
          accum[categoryName] = {
            categoryLabel: categoryName,
            properties: categoryProperties
          };
          return accum;
        }, {});
      return categories;
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
      moveMeasurementMoment: moveMeasurementMoment,
      isExistingMeasurement: isExistingMeasurement,
      isStudyNode: isStudyNode,
      getVariableDetails: getVariableDetails,
      getDefaultResultProperties: getDefaultResultProperties,
      getResultPropertiesForType: getResultPropertiesForType,
      buildPropertyCategories: buildPropertyCategories,
      VARIABLE_TYPE_DETAILS: VARIABLE_TYPE_DETAILS,
      INTEGER_TYPE: INTEGER_TYPE,
      DOUBLE_TYPE: DOUBLE_TYPE,
      TIME_SCALE_OPTIONS: TIME_SCALE_OPTIONS
    };
  };
  return dependencies.concat(ResultsService);
});

'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = [
    'StudyService',
    'RdfListService',
    'UUIDService',
    'ARM_LEVEL_TYPE',
    'CONTRAST_TYPE',
    'ONTOLOGY_BASE',
    'RESULT_PROPERTY_TYPES',
    'RESULT_PROPERTY_TYPE_DETAILS',
    'CONTRAST_OPTIONS_DETAILS'
  ];
  var ResultsService = function(
    StudyService,
    RdfListService,
    UUIDService,
    ARM_LEVEL_TYPE,
    CONTRAST_TYPE,
    ONTOLOGY_BASE,
    RESULT_PROPERTY_TYPES,
    RESULT_PROPERTY_TYPE_DETAILS,
    CONTRAST_OPTIONS_DETAILS
  ) {

    function getResultPropertyDetails(resultPropertyUri, armOrContrast) {
      armOrContrast = getDataTypeOrDefault(armOrContrast);
      var uri = normaliseUri(resultPropertyUri);
      var details = _.merge({},
        CONTRAST_OPTIONS_DETAILS,
        RESULT_PROPERTY_TYPE_DETAILS[armOrContrast]);
      return _.find(details, ['uri', uri]);
    }

    function normaliseUri(resultPropertyDetails) {
      if (!_.startsWith(resultPropertyDetails, ONTOLOGY_BASE)) {
        return _.replace(resultPropertyDetails, 'ontology:', ONTOLOGY_BASE);
      }
      return resultPropertyDetails;
    }

    function setValue(row, inputColumn) {
      var inputColumnVariableDetails = getResultPropertyDetails(inputColumn.resultProperty, inputColumn.armOrContrast);
      return StudyService.getJsonGraph().then(function(graph) {
        if (!row.uri) {
          return createValue(row, inputColumn, inputColumnVariableDetails.type, graph);
        } else {
          return updateValue(row, inputColumn, inputColumnVariableDetails.type, graph);
        }
      });
    }

    function createValue(row, inputColumn, columnType, graph) {
      if (inputColumn.value !== null && inputColumn.value !== undefined) {
        var addItem = {
          '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
          of_group: row.group.armURI || row.group.groupUri,
          of_moment: row.measurementMoment.uri,
          of_outcome: row.variable.uri,
          arm_or_contrast: inputColumn.armOrContrast ? inputColumn.armOrContrast : ARM_LEVEL_TYPE
        };
        addItem[columnType] = inputColumn.value;
        StudyService.saveJsonGraph(graph.concat(addItem));
        return addItem['@id'];
      } else {
        return undefined;
      }
    }

    function updateValue(row, inputColumn, columnType, graph) {
      var editItem = _.remove(graph, function(node) {
        return row.uri === node['@id'];
      })[0];

      if (inputColumn.value === null) {
        delete editItem[columnType];
      } else {
        editItem[columnType] = inputColumn.value;
      }

      if (!isEmptyResult(editItem, row.variable.armOrContrast)) {
        graph.push(editItem);
      } else {
        delete row.uri;
      }
      StudyService.saveJsonGraph(graph);
      return row.uri;
    }

    function setCategoricalValue(row, inputColumn) {
      return StudyService.getJsonGraph().then(function(graph) {
        if (!row.uri) {
          return createCategoricalValue(row, inputColumn, graph);
        } else {
          return updateCategoricalValue(row, inputColumn, graph);
        }
      });
    }

    function updateCategoricalValue(row, inputColumn, graph) {
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
      StudyService.saveJsonGraph(graph);
      return row.uri;
    }

    function createCategoricalValue(row, inputColumn, graph) {
      if (inputColumn.value !== null && inputColumn.value !== undefined) {
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
    }

    function updateResultValue(row, inputColumn) {
      if (!inputColumn.isCategory) {
        return setValue(row, inputColumn);
      } else {
        return setCategoricalValue(row, inputColumn);
      }
    }

    function isEmptyCategoricalResult(item) {
      return !_.some(item.category_count, function(categoryItem) {
        return categoryItem.count !== undefined;
      });
    }

    function isEmptyResult(item, armOrContrast) {
      return !_.some(RESULT_PROPERTY_TYPES[armOrContrast], function(type) {
        return item[type] !== undefined;
      });
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
        return _.keys(_.pick(node, RESULT_PROPERTY_TYPES[getDataTypeOrDefault(node.arm_or_contrast)])).length > 0;
      } else {
        return node.category_count.length && _.find(node.category_count, function(countNode) {
          return countNode.count !== null && countNode.count !== undefined;
        });
      }
    }

    function cleanupMeasurements() {
      return StudyService.getJsonGraph().then(function(graph) {
        var filteredGraph = filterGraph(graph);
        return StudyService.saveJsonGraph(filteredGraph);
      });
    }

    function filterGraph(graph) {
      var study = _.find(graph, isStudyNode);
      var hasGroupMap = createHasPropertyByIdMap(study.has_group);
      if (study.has_included_population) {
        hasGroupMap[study.has_included_population[0]['@id']] = true;
      }
      var hasArmMap = createHasPropertyByIdMap(study.has_arm);
      var hasMomentMap = createHasPropertyByIdMap(_.filter(graph, isMoment));
      var hasOutcomeMap = _.keyBy(study.has_outcome, '@id');
      var hasMeasurementsMap = getMeasurementsForOutcomes(study);
      var filteredGraph = removeNoLongerMeasuredProperties(graph, hasOutcomeMap, study);

      return _.filter(filteredGraph, function(node) {
        if (isResult(node)) {
          return (hasArmMap[node.of_group] || hasGroupMap[node.of_group]) &&
            hasMomentMap[node.of_moment] &&
            hasOutcomeMap[node.of_outcome] &&
            hasMeasurementsMap[node.of_moment] &&
            hasValues(node);
        } else {
          return true;
        }
      });
    }

    function createHasPropertyByIdMap(propertyList) {
      return _(propertyList)
        .keyBy('@id')
        .mapValues(function() {
          return true;
        })
        .value();
    }

    function getMeasurementsForOutcomes(study) {
      return _.reduce(study.has_outcome, function(accum, outcome) {
        var measurementMomentUris;
        if (!Array.isArray(outcome.is_measured_at)) {
          accum[outcome.is_measured_at] = true;
        } else {
          measurementMomentUris = outcome.is_measured_at || [];
          _.forEach(measurementMomentUris, function(measurementMomentUri) {
            accum[measurementMomentUri] = true;
          });
        }
        return accum;
      }, {});
    }

    function removeNoLongerMeasuredProperties(graph, outcomeMap, study) {
      return _.map(graph, function(node) {
        if (isResult(node) && outcomeMap[node.of_outcome]) {
          if (node.category_count) {
            node.category_count = getCategoryCount(study, node);
          } else {
            return removeMissingPropertiesFromNode(node, outcomeMap);
          }
        }
        return node;
      });
    }

    function removeMissingPropertiesFromNode(node, outcomeMap) {
      var missingProperties = getMissingProperties(outcomeMap, node);
      return _.omit(node, _.map(missingProperties, 'type'));
    }

    function getMissingProperties(outcomeMap, node) {
      var resultProperties = getResultPropertiesFor(node);
      return filterResultProperties(resultProperties, outcomeMap, node);
    }

    function filterResultProperties(resultProperties, outcomeMap, node) {
      return _.filter(resultProperties, function(resultProperty) {
        return isResultPropertyStillIncludedForNode(resultProperty, outcomeMap, node);
      });
    }

    function isResultPropertyStillIncludedForNode(resultProperty, outcomeMap, node) {
      if (isConfidenceIntervalUri(resultProperty.uri)) {
        return !_.includes(outcomeMap[node.of_outcome].has_result_property, ONTOLOGY_BASE + 'confidence_interval_width');
      }
      return !_.includes(outcomeMap[node.of_outcome].has_result_property, resultProperty.uri);
    }

    function isConfidenceIntervalUri(uri) {
      return uri === (ONTOLOGY_BASE + 'confidence_interval_upper_bound') ||
        uri === (ONTOLOGY_BASE + 'confidence_interval_lower_bound');
    }

    function getCategoryCount(study, node) {
      var categoryIds = getCategoryIds(study);
      return _.filter(node.category_count, function(countObject) {
        return _.includes(categoryIds, countObject.category);
      });
    }

    function getCategoryIds(study) {
      return _.reduce(study.has_outcome, function(accum, outcome) {
        var categories = RdfListService.flattenList(outcome.of_variable[0].categoryList);
        return accum.concat(_.map(categories, '@id'));
      }, []);
    }

    function getResultPropertiesFor(node) {
      var armOrContrast = getDataTypeOrDefault(node.arm_or_contrast);
      var variableTypes = RESULT_PROPERTY_TYPES[armOrContrast];
      var resultProperties = _.keys(_.pick(node, variableTypes));
      var details = RESULT_PROPERTY_TYPE_DETAILS[armOrContrast];
      if (armOrContrast === CONTRAST_TYPE) {
        details = _.merge({}, details, CONTRAST_OPTIONS_DETAILS);
      }
      return _.map(resultProperties, function(resultProperty) {
        return details[resultProperty];
      });
    }

    function getDataTypeOrDefault(armOrContrast) {
      return armOrContrast ? armOrContrast : ARM_LEVEL_TYPE;
    }

    function setToMeasurementMoment(measurementMomentUri, measurementInstanceList) {
      return StudyService.getJsonGraph().then(function(graph) {
        _.forEach(graph, function(node) {
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

        return _.some(graph, function(node) {
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
        return _(resultJsonItems)
          .map(toFrontend)
          .flatten()
          .value();
      });
    }

    function toFrontend(backEndItem) {
      var baseItem = {
        instance: backEndItem['@id'],
        armUri: backEndItem.of_group,
        momentUri: backEndItem.of_moment,
        outcomeUri: backEndItem.of_outcome,
        armOrContrast: backEndItem.arm_or_contrast || ARM_LEVEL_TYPE
      };
      if (isNonConformantMeasurementResult(backEndItem)) {
        baseItem.comment = backEndItem.comment;
      }

      var valueItems = createValueItems(baseItem, backEndItem);
      var categoryItems = createCategoryItems(baseItem, backEndItem);
      return valueItems.concat(categoryItems);
    }

    function createCategoryItems(baseItem, backEndItem) {
      if (backEndItem.category_count) {
        return _.map(backEndItem.category_count, function(categoryCount) {
          return createCategoryItem(baseItem, categoryCount);
        });
      } else {
        return [];
      }
    }

    function createValueItems(baseItem, backEndItem) {
      return _(RESULT_PROPERTY_TYPES[baseItem.armOrContrast])
        .map(function(variableType) {
          if (backEndItem[variableType] !== undefined) {
            return createValueItem(baseItem, backEndItem, variableType);
          }
        })
        .compact()
        .value();
    }

    function createValueItem(baseItem, backEndItem, type) {
      var valueItem = angular.copy(baseItem);
      valueItem.result_property = type;
      valueItem.value = backEndItem[type];
      return valueItem;
    }

    function createCategoryItem(baseItem, categoryItem) {
      var valueItem = angular.copy(baseItem);
      valueItem.isCategorical = true;
      valueItem.result_property = categoryItem.category && categoryItem.category.label ? categoryItem.label : categoryItem;
      valueItem.value = categoryItem.count;
      return valueItem;
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
      getResultPropertyDetails: getResultPropertyDetails
    };
  };
  return dependencies.concat(ResultsService);
});

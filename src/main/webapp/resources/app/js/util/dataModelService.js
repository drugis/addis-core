'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = [
    'UUIDService',
    'ONTOLOGY_BASE'
  ];
  var DataModelService = function(
    UUIDService,
    ONTOLOGY_BASE
  ) {
    var INSTANCE_BASE = 'http://trials.drugis.org/instances/';
    var RDF_FIRST = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#first';
    var RDF_REST = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest';

    function makeCategoryInstance(categoryName) {
      return {
        '@id': INSTANCE_BASE + UUIDService.generate(),
        '@type': ONTOLOGY_BASE + 'Category',
        label: categoryName
      };
    }

    function updateCategories(data) {
      var graph = data['@graph'];
      graph = _.map(graph, function(node) {
        if (node['@type'] === 'http://trials.drugis.org/ontology#Category' && node.title) {
          var withLabel = _.merge({}, node, {
            label: node.title
          });
          return _.omit(withLabel, 'title');
        }
        return node;
      });
      var newCategoryInstances = [];
      var oldCategoriesByName = {};

      var categoryListIds = _.map(_.filter(graph, function(node) {
        return node.categoryList;
      }), 'categoryList');
      var categoryLists = _.filter(graph, function(node) {
        return _.includes(categoryListIds, node['@id']);
      });

      _.forEach(categoryLists, function(categoryList) {
        var currentNode = categoryList;
        while (currentNode && currentNode.first) {
          if (!_.startsWith(currentNode.first, INSTANCE_BASE)) {
            var newCategory = makeCategoryInstance(currentNode.first);
            newCategoryInstances.push(newCategory);
            oldCategoriesByName[currentNode.first] = newCategory;
            currentNode.first = newCategory['@id'];

            if (currentNode.rest['@list']) {
              var newRestCategory = makeCategoryInstance(currentNode.rest['@list'][0]);
              newCategoryInstances.push(newRestCategory);
              oldCategoriesByName[currentNode.rest['@list'][0]] = newRestCategory;
              currentNode.rest['@list'][0] = newRestCategory['@id'];
            }
          }
          currentNode = _.find(graph, ['@id', currentNode.rest]);
        }
      });
      data['@graph'] = graph = graph.concat(newCategoryInstances);
      graph = _.map(graph, function(node) {
        if (node.category && oldCategoriesByName[node.category]) {
          node.category = oldCategoriesByName[node.category]['@id'];
        }
        return node;
      });
      return data;
    }

    function normalizeFirstAndRest(data) {
      data['@graph'] = _.map(data['@graph'], function(node) {
        if (node[RDF_FIRST]) {
          node.first = node[RDF_FIRST];
          delete node[RDF_FIRST];
        }
        if (node[RDF_REST]) {
          node.rest = node[RDF_REST];
          delete node[RDF_REST];
        }
        return node;
      });
      return data;
    }

    function addTypeToUnits(data) {
      data['@graph'] = _.map(data['@graph'], function(node) {
        if (node.conversionMultiplier && node['@type'] !== 'http://trials.drugis.org/ontology#Unit' &&
          node['@type'] !== 'ontology:Unit') {
          var newNode = angular.copy(node);
          newNode.sameAs = node['@type'];
          newNode['@type'] = 'ontology:Unit';
          return newNode;
        }
        return node;
      });
      return data;
    }

    function correctUnitConceptType(data) {
      data['@graph'] = _.map(data['@graph'], function(node) {
        if (node['@type'] === 'http://www.w3.org/2002/07/owl#Class') {
          var newNode = angular.copy(node);
          newNode['@type'] = 'ontology:Unit';
          return newNode;
        }
        return node;
      });
      return data;
    }

    function addOverallPopulation(data) {
      var newPopulationNode;
      var newGraph = _.map(data['@graph'], function(node) {
        if (node['@type'] === 'http://trials.drugis.org/ontology#Study' && !node.has_included_population) {
          var newStudyNode = angular.copy(node);
          var newPopulationNodeUri = INSTANCE_BASE + UUIDService.generate();
          newPopulationNode = {
            '@id': newPopulationNodeUri,
            '@type': 'ontology:StudyPopulation'
          };
          newStudyNode.has_included_population = [newPopulationNodeUri];
          return newStudyNode;
        }
        return node;
      });
      if (newPopulationNode) {
        newGraph.push(newPopulationNode);
      }
      return _.extend({}, data, {
        '@graph': newGraph
      });
    }

    function applyOnLoadCorrections(data) {
      var graphData = normalizeFirstAndRest(data);
      graphData = updateCategories(graphData);
      graphData = addTypeToUnits(graphData);
      graphData = addOverallPopulation(graphData);
      return graphData;
    }

    return {
      updateCategories: updateCategories,
      normalizeFirstAndRest: normalizeFirstAndRest,
      addTypeToUnits: addTypeToUnits,
      correctUnitConceptType: correctUnitConceptType,
      addOverallPopulation: addOverallPopulation,
      applyOnLoadCorrections: applyOnLoadCorrections
    };
  };
  return dependencies.concat(DataModelService);
});

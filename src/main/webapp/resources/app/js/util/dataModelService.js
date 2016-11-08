'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = ['UUIDService'];
  var DataModelService = function(UUIDService) {
    var INSTANCE_BASE = 'http://trials.drugis.org/instances/';
    var RDF_FIRST = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#first';
    var RDF_REST = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest';

    function updateCategories(data) {
      var graph = data['@graph'];
      var oldStyleCategories = _.filter(graph, function(node) {
        return _.isString(node.category) && !_.startsWith(node.category, INSTANCE_BASE);
      });

      if (oldStyleCategories.length) {
        var ONTOLOGY_BASE = 'http://trials.drugis.org/ontology#';
        var categoryNames = _.uniq(_.map(oldStyleCategories, 'category'));
        var categoryInstances = _.map(categoryNames, function(categoryName) {
          return {
            '@id': INSTANCE_BASE + UUIDService.generate(),
            '@type': ONTOLOGY_BASE + 'Category',
            label: categoryName
          };
        });
        var categoriesByName = _.keyBy(categoryInstances, 'label');
        graph = _.map(graph, function(node) {
          if (node.category) {
            node.category = categoriesByName[node.category]['@id'];
          }
          return node;
        });
        data['@graph'] = graph = graph.concat(categoryInstances);

        var variablesToUpdate = _.filter(graph, function(node) {
          return node.categoryList;
        });
        var categoryListIds = _.map(variablesToUpdate, 'categoryList');
        var categoryLists = _.filter(graph, function(node) {
          return _.includes(categoryListIds, node['@id']);
        });
        _.forEach(categoryLists, function(categoryList) {
          var currentNode = categoryList;
          var firstProp = currentNode.first ? 'first' : RDF_FIRST;
          var restProp = currentNode.first ? 'rest' : RDF_REST;

          while (currentNode && currentNode[firstProp]) {
            currentNode[firstProp] = categoriesByName[currentNode[firstProp]]['@id'];
            if (currentNode[restProp]['@list']) {
              currentNode[restProp]['@list'][0] = categoriesByName[currentNode[restProp]['@list'][0]]['@id'];
            }
            currentNode = _.find(graph, function(node) {
              return node['@id'] === currentNode[restProp]['@id'];
            });
          }
        });
      }
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

    return {
      updateCategories: updateCategories,
      normalizeFirstAndRest: normalizeFirstAndRest
    };
  };
  return dependencies.concat(DataModelService);
});

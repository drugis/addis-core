'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = ['UUIDService'];
  var DataModelService = function(UUIDService) {
    var INSTANCE_BASE = 'http://trials.drugis.org/instances/';

    function updateCategories(graph) {
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
        graph = graph.concat(categoryInstances);
      }
      return graph;
    }

    return {
      updateCategories: updateCategories
    };
  };
  return dependencies.concat(DataModelService);
});

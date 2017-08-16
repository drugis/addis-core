'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'UUIDService'];
  var ConceptsService = function($q, UUIDService) {

    var modified = false;
    var conceptJsonPromise;

    var typeOptions = {
      'ontology:Drug': {
        uri: 'ontology:Drug',
        label: 'Drug'
      },
      'ontology:Variable': {
        uri: 'ontology:Variable',
        label: 'Variable'
      },
      'ontology:Unit':{
        uri: 'ontology:Unit',
        label: 'Unit'
      }
    };

    function toFrontEnd(conceptItem) {
      var frontEnd = {
        uri: conceptItem['@id'],
        type: typeOptions[conceptItem['@type']],
        label: conceptItem.label
      };
      if (conceptItem.comment) {
        frontEnd.comment = conceptItem.comment;
      }
      return frontEnd;
    }

    function toBackEnd(concept) {
      var backEnd = {
        '@id': 'http://trials.drugis.org/concepts/' + UUIDService.generate(),
        '@type': concept.type.uri,
        label: concept.label
      };
      if (concept.comment) {
        backEnd.comment = concept.comment;
      }
      return backEnd;
    }

    function queryItems() {
      return conceptJsonPromise.then(function(json) {
        return _.map(json['@graph'], toFrontEnd);
      });
    }

    function addItem(concept) {
      return conceptJsonPromise.then(function(json) {
        modified = true;
        json['@graph'].push(toBackEnd(concept));
        return json;
      });
    }

    function areConceptsModified() {
      return modified;
    }

    function conceptsSaved() {
      modified = false;
    }

    function loadJson(jsonPromise) {
      conceptJsonPromise = jsonPromise;
      return jsonPromise;
    }

    function getGraphAndContext() {
      return conceptJsonPromise.then(function(graphAndContext) {
        return graphAndContext;
      });
    }

    return {
      queryItems: queryItems,
      addItem: addItem,
      areConceptsModified: areConceptsModified,
      conceptsSaved: conceptsSaved,
      loadJson: loadJson,
      getGraphAndContext: getGraphAndContext,
      typeOptions: typeOptions
    };

  };
  return dependencies.concat(ConceptsService);
});

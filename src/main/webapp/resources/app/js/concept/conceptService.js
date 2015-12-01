'use strict';
define([], function() {
  var dependencies = ['$q', 'UUIDService'];
  var ConceptService = function($q, UUIDService) {

    var
      conceptsGraphUriBase = 'http://trials.drugis.org/concepts/',
      scratchConceptsGraphUri,
      modified = false,
      contextJsonPromise,
      typeOptions = {
        'http://trials.drugis.org/ontology#Drug': {
          uri: 'http://trials.drugis.org/ontology#Drug',
          label: 'Drug'
        },
        'http://trials.drugis.org/ontology#Variable': {
          uri: 'http://trials.drugis.org/ontology#Variable',
          label: 'Variable'
        }
      };

    function toFrontEnd(conceptItem) {
      var frontEnd = {
        uri: conceptItem['@id'],
        type: typeOptions[conceptItem['@type']],
        label: conceptItem['http://www.w3.org/2000/01/rdf-schema#label']
      };
      if (conceptItem['http://www.w3.org/2000/01/rdf-schema#comment']) {
        frontEnd.comment = conceptItem['http://www.w3.org/2000/01/rdf-schema#comment'];
      }
      return frontEnd;
    }

    function toBackEnd(concept) {
      var backEnd = {
        '@id': 'http://trials.drugis.org/concepts/' + UUIDService.generate(),
        '@type': concept.type.uri,
        'http://www.w3.org/2000/01/rdf-schema#label': concept.label
      };
      if (concept.comment) {
        backEnd['http://www.w3.org/2000/01/rdf-schema#comment'] = concept.comment;
      }
      return backEnd;
    }

    function queryItems() {
      return contextJsonPromise.then(function(json) {
        return _.map(json['@graph'], toFrontEnd)
      });
    }

    function addItem(concept) {
      return contextJsonPromise.then(function(json) {
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
      contextJsonPromise = jsonPromise;
      return jsonPromise;
    }

    function getGraphAndContext() {
      return contextJsonPromise.then(function(graphAndContext) {
        return graphAndContext;
      });
    }

    function getJsonGraph() {
      return contextJsonPromise.then(function(graph) {
        return graph['@graph'];
      });
    }

    function saveJsonGraph(newGraph) {
      return contextJsonPromise.then(function(jsonLd) {
        jsonLd['@graph'] = newGraph;
        modified = true;
      });
    }

    return {
      queryItems: queryItems,
      addItem: addItem,
      areConceptsModified: areConceptsModified,
      conceptsSaved: conceptsSaved,
      loadJson: loadJson
    };

  };
  return dependencies.concat(ConceptService);
});
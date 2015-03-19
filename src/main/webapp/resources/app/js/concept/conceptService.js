'use strict';
define([], function() {
  var dependencies = ['$q', 'SparqlResource', 'RemoteRdfStoreService', 'UUIDService'];
  var ConceptService = function($q, SparqlResource, RemoteRdfStoreService, UUIDService) {

    var loadDefer = $q.defer();
    var
      conceptsGraphUriBase = 'http://trials.drugis.org/concepts/',
      scratchConceptsGraphUri,
      modified = false;

    var queryConceptsTemplate = SparqlResource.get('queryConcepts.sparql');
    var addConceptTemplate = SparqlResource.get('addConcept.sparql');

    function loadStore(data) {
      console.log('concept loadStore start');
      return RemoteRdfStoreService.create(conceptsGraphUriBase).then(function(graphUri) {
        scratchConceptsGraphUri = graphUri;
        return RemoteRdfStoreService.load(scratchConceptsGraphUri, data).then(function() {
          loadDefer.resolve();
        });
      });
    }

    function queryItems(datasetUri) {
      return queryConceptsTemplate.then(function(template) {
        var query = fillInTemplate(template, datasetUri);
        return doNonModifyingQuery(query);
      });
    }

    function addItem(datasetUri, concept) {
      return addConceptTemplate.then(function(template) {
        var query = fillInConceptTemplate(template, datasetUri, concept);
        return doModifyingQuery(query);
      });
    }

    function areConceptsModified() {
      return modified;
    }

    function doModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeUpdate(scratchConceptsGraphUri, query).then(function() {
          modified = true;
        });
      });
    }

    function doNonModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeQuery(scratchConceptsGraphUri, query);
      });
    }


    function fillInTemplate(template, datasetUri) {
      var conceptGraphUri = datasetUri + '/concepts';
      return template.replace(/\$conceptGraphUri/g, conceptGraphUri);
    }

    function fillInConceptTemplate(template, datasetUri, concept) {
      var conceptGraphUri = datasetUri + '/concepts';
      return template.replace(/\$conceptGraphUri/g, conceptGraphUri)
        .replace(/\$conceptUri/g, 'http://trials.drugis.org/concepts/' + UUIDService.generate())
        .replace(/\$conceptType/g, concept.type.uri)
        .replace(/\$conceptTitle/g, concept.title)
        ;
    }

    return {
      queryItems: queryItems,
      loadStore: loadStore,
      addItem: addItem,
      areConceptsModified: areConceptsModified
    };

  };
  return dependencies.concat(ConceptService);
});

'use strict';
define([], function() {
  var dependencies = ['$q', 'SparqlResource', 'RemoteRdfStoreService'];
  var ConceptService = function($q, SparqlResource, RemoteRdfStoreService) {

    var loadDefer = $q.defer();
    var 
      conceptsGraphUriBase = 'http://trials.drugis.org/concepts/',
      scratchConceptsGraphUri,
      modified = false;

    var queryConceptsTemplate = SparqlResource.get('queryConcepts.sparql');

    function loadStore(data) {
      console.log('concept loadStore start');
      return RemoteRdfStoreService.create(conceptsGraphUriBase).then(function(graphUri) {
        scratchConceptsGraphUri = graphUri;
        return RemoteRdfStoreService.load(scratchConceptsGraphUri, data).then(function() {
          loadDefer.resolve();
        });
      });
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

    function queryItems(datasetUri) {
      return queryConceptsTemplate.then(function(template) {
        var query = fillInTemplate(template, datasetUri);
        return doNonModifyingQuery(query);
      });
    }

    function fillInTemplate(template, datasetUri) {
      var conceptGraphUri = datasetUri + '/concepts';
      return template.replace(/\$conceptGraphUri/g, conceptGraphUri);
    }

    return {
      queryItems: queryItems,
      loadStore: loadStore
    };

  };
  return dependencies.concat(ConceptService);
});
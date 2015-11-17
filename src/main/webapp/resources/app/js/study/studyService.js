'use strict';
define([], function() {
  var dependencies = ['$q', '$filter', 'UUIDService', 'RemoteRdfStoreService', 'SparqlResource', 'SanitizeService'];
  var StudyService = function($q, $filter, UUIDService, RemoteRdfStoreService, SparqlResource, SanitizeService) {

    var graphPrefix = 'http://trials.drugis.org/graphs/';
    var loadDefer = $q.defer();
    var scratchStudyUri,
      modified = false;
    var studyJsonPromise;

    var createEmptyStudyTemplate = SparqlResource.get('createEmptyStudy.sparql');
    var studyDataQuery = SparqlResource.get('queryStudyData.sparql');

    function doModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeUpdate(scratchStudyUri, query).then(function() {
          modified = true;
        });
      });
    }

    function doNonModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeQuery(scratchStudyUri, query);
      });
    }

    function createEmptyStudy(study) {
      var studyCopy = angular.copy(study);
      loadDefer = $q.defer();
      return RemoteRdfStoreService.create(graphPrefix).then(function(newGraphUri) {
        scratchStudyUri = newGraphUri;
        studyCopy.uuid = UUIDService.generate();
        return createEmptyStudyTemplate.then(function(template) {
          var query = fillTemplate(template, studyCopy);
          return RemoteRdfStoreService.executeUpdate(newGraphUri, query).then(function(response) {
            loadDefer.resolve();
            return response;
          });
        });
      });
    }

    function queryStudyData() {
      return loadDefer.promise.then(function() {
        return studyDataQuery.then(function(query) {
          return RemoteRdfStoreService.executeQuery(scratchStudyUri, query).then(function(results) {
            return results[0];
          });
        });
      });
    }

    function loadStore(data) {
      return RemoteRdfStoreService.create(graphPrefix).then(function(graphUri) {
        scratchStudyUri = graphUri;
        return RemoteRdfStoreService.load(scratchStudyUri, data).then(function() {
          loadDefer.resolve();
        });
      });
    }

    function getGraph() {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.getGraph(scratchStudyUri);
      });
    }

    function isStudyModified() {
      return modified;
    }

    function studySaved() {
      modified = false;
    }

    function getStudyUUID() {
      return $filter('stripFrontFilter')(scratchStudyUri, graphPrefix);
    }

    function reset() {
      loadDefer = $q.defer();
      modified = false;
    }

    function fillTemplate(template, study) {
      return template.replace(/\$studyUuid/g, study.uuid)
        .replace(/\$label/g, study.label)
        .replace(/\$comment/g, SanitizeService.sanitizeStringLiteral(study.comment));
    }

    function loadJson(jsonPromise) {
      studyJsonPromise = jsonPromise;
    }

    function getGraphAndContext() {
      return studyJsonPromise.then(function(graphAndContext) {
        return graphAndContext;
      });
    }

    function getJsonGraph() {
      return studyJsonPromise.then(function(graph) {
        return graph['@graph'];
      });
    }

    function saveJsonGraph(newGraph) {
      return studyJsonPromise.then(function(jsonLd) {
        jsonLd['@graph'] = newGraph;
      });
    }

    function getStudy() {
      return studyJsonPromise.then(function(graph) {
        return _.find(graph['@graph'], function(graphNode) {
          return graphNode['@type'] === 'ontology:Study';
        });
      });
    }

    function save(study) {
      return studyJsonPromise.then(function(jsonLd) {
        _.remove(jsonLd['@graph'], function(graphNode) {
          return graphNode['@type'] === 'ontology:Study';
        });
        jsonLd['@graph'].push(study);
      });
    }

    return {
      loadStore: loadStore,
      queryStudyData: queryStudyData,
      createEmptyStudy: createEmptyStudy,
      getGraph: getGraph,
      doModifyingQuery: doModifyingQuery,
      doNonModifyingQuery: doNonModifyingQuery,
      isStudyModified: isStudyModified,
      studySaved: studySaved,
      getStudyUUID: getStudyUUID,
      reset: reset,
      loadJson: loadJson,
      getStudy: getStudy,
      save: save,
      getJsonGraph: getJsonGraph,
      saveJsonGraph: saveJsonGraph,
      getGraphAndContext: getGraphAndContext
    };
  };

  return dependencies.concat(StudyService);
});
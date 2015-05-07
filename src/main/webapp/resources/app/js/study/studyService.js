'use strict';
define([], function() {
  var dependencies = ['$q', '$filter', 'UUIDService', 'RemoteRdfStoreService', 'SparqlResource'];
  var StudyService = function($q, $filter, UUIDService, RemoteRdfStoreService, SparqlResource) {

    var graphPrefix = 'http://trials.drugis.org/graphs/';
    var loadDefer = $q.defer();
    var scratchStudyUri,
      modified = false;

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
        .replace(/\$comment/g, study.comment);
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
      reset: reset
    };
  };

  return dependencies.concat(StudyService);
});
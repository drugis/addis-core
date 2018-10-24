'use strict';
define(['lodash', '../util/context'], function(_, externalContext) {
  var dependencies = [
    '$q',
    'UUIDService',
    'GraphResource'
  ];
  var StudyService = function(
    $q,
    UUIDService,
    GraphResource
  ) {
    var modified = false;
    var studyJsonPromise;
    var EMPTY_STUDY = {
      '@graph': [{
        '@type': 'ontology:Study',
        has_activity: [],
        has_arm: [],
        has_group: [],
        has_included_population: createIncludedPopulation(),
        has_eligibility_criteria: [],
        has_indication: [],
        has_objective: [],
        has_outcome: [],
        has_publication: []
      }],
      '@context': externalContext
    };

    function createEmptyStudy(study, userUid, datasetUid) {
      var uuid = UUIDService.generate();
      var emptyStudy = EMPTY_STUDY;
      emptyStudy['@id'] = 'http://trials.drugis.org/studies/' + uuid;
      emptyStudy.comment = study.comment;
      emptyStudy.label = study.label;
      var newVersionDefer = $q.defer();

      GraphResource.putJson({
        userUid: userUid,
        datasetUuid: datasetUid,
        graphUuid: uuid,
        commitTitle: 'Initial study creation: ' + study.label
      }, emptyStudy, function(value, responseHeaders) {
        var newVersion = responseHeaders('X-EventSource-Version');
        newVersion = newVersion.split('/')[4];
        newVersionDefer.resolve(newVersion);
      }, errorCallback);

      return newVersionDefer.promise;
    }

    function errorCallback(error) {
      console.error('error' + error);
    }

    function isStudyModified() {
      return modified;
    }

    function studySaved() {
      modified = false;
    }

    function reset() {
      modified = false;
    }

    function createIncludedPopulation() {
      return [{
        '@id': 'instance:' + UUIDService.generate(),
        '@type': 'ontology:StudyPopulation'
      }];
    }

    function findStudyNode(graph) {
      return _.find(graph, function(graphNode) {
        return graphNode['@type'] === 'ontology:Study';
      });
    }

    function findStudyGraphNode(graph) {
      return findStudyNode(graph['@graph']);
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
        modified = true;
      });
    }

    function getStudy() {
      return studyJsonPromise.then(function(graph) {
        return findStudyGraphNode(graph);
      });
    }

    function save(study) {
      return studyJsonPromise.then(function(jsonLd) {
        _.remove(jsonLd['@graph'], function(graphNode) {
          return graphNode['@type'] === 'ontology:Study';
        });
        jsonLd['@graph'].push(study);
        modified = true;
      });
    }

    return {
      createEmptyStudy: createEmptyStudy,
      isStudyModified: isStudyModified,
      studySaved: studySaved,
      reset: reset,
      loadJson: loadJson,
      getGraphAndContext: getGraphAndContext,
      getJsonGraph: getJsonGraph,
      saveJsonGraph: saveJsonGraph,
      getStudy: getStudy,
      save: save,
      findStudyNode: findStudyNode
    };
  };

  return dependencies.concat(StudyService);
});

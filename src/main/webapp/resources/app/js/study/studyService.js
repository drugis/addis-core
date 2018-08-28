'use strict';
define(['lodash', '../util/context'], function(_, externalContext) {
  var dependencies = ['$q', 'UUIDService', 'GraphResource'];
  var StudyService = function($q, UUIDService, GraphResource) {

    var loadDefer = $q.defer();
    var modified = false;
    var studyJsonPromise;

    function createEmptyStudy(study, userUid, datasetUid) {
      var uuid = UUIDService.generate();
      var emptyStudy = {
        '@graph': [{
          '@id': 'http://trials.drugis.org/studies/' + uuid,
          '@type': 'ontology:Study',
          comment: study.comment,
          label: study.label,
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
      }, function(error) {
        console.error('error' + error);
      });

      return newVersionDefer.promise;
    }

    function isStudyModified() {
      return modified;
    }

    function studySaved() {
      modified = false;
    }

    function reset() {
      loadDefer = $q.defer();
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

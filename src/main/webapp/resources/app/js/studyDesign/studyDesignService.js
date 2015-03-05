'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService'];
    var StudyDesignService = function($q, StudyService, SparqlResource, UUIDService) {

      function queryItems(studyUuid) {
      }

      function addItem(studyUuid, coordinate) {

      }

      function editItem(studyUuid, coordinate) {
      }

      function fillInTemplate(template, studyUuid, activity) {
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        editItem: editItem
      };
    };
    return dependencies.concat(StudyDesignService);
  });

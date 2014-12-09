'use strict';
define([],
  function() {
    var dependencies = ['StudyService', '$filter'];
    var ArmService = function(StudyService, $filter) {

      function edit(arm) {
        var editQuery =
          ' PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
          ' PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ' +

          ' DELETE {  ' +
          ' <' + arm.armURI.value + '> rdfs:label ?label .' +
          ' <' + arm.armURI.value + '> rdfs:comment ?comment .' +
          ' }' +
          ' INSERT { ' +
          ' <' + arm.armURI.value + '> rdfs:label "' + arm.label.value + '" .' +
          ' <' + arm.armURI.value + '> rdfs:comment "' + arm.comment.value + '" .' +
          ' }' +
          ' WHERE { ' +
          ' <' + arm.armURI.value + '> rdfs:label ?label .' +
          ' <' + arm.armURI.value + '> rdfs:comment ?comment . ' +
          ' }';

        return StudyService.doQuery(editQuery);
      }


      return {
        edit: edit
      };
    };

    return dependencies.concat(ArmService);
  });
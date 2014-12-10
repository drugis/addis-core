'use strict';
define([],
  function() {
    var dependencies = ['$resource', 'StudyService', 'SparqlResource'];
    var ArmService = function($resource, StudyService, SparqlResource) {

      var query = SparqlResource.get({
        name: 'editArmWithComment.sparql'
      });

      function edit(arm) {
        var editArmQuery = query.data.replace(/\$armURI/g, arm.armURI.value)
          .replace('$newArmLabel', arm.label.value)
          .replace('$newArmComment', arm.comment.value);

        return StudyService.doQuery(editArmQuery);
      }


      return {
        edit: edit
      };
    };

    return dependencies.concat(ArmService);
  });

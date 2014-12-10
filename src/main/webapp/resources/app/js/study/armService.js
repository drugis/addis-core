'use strict';
define([],
  function() {
    var dependencies = ['$resource', 'StudyService', 'SparqlResource'];
    var ArmService = function($resource, StudyService, SparqlResource) {

      var query = SparqlResource.get('editArmWithComment.sparql');

      function edit(arm) {
        var defer = $q.defer();
        query.$promise.then(function(query) {
          var editArmQuery =
            query.replace(/\$armURI/g, arm.armURI.value)
            .replace('$newArmLabel', arm.label.value)
            .replace('$newArmComment', arm.comment.value);
          defer.resolve(StudyService.doQuery(editArmQuery));
        });
        return defer.promise;
      }

      return {
        edit: edit
      };
    };

    return dependencies.concat(ArmService);
  });

'use strict';
define([],
  function() {
    var dependencies = ['$q', '$resource', 'StudyService', 'SparqlResource'];
    var ArmService = function($q, $resource, StudyService, SparqlResource) {

      var editArmQuery = SparqlResource.get({
        name: 'editArmWithComment.sparql'
      });

      var rawDeleteArmQuery = SparqlResource.get({
        name: 'deleteArm.sparql'
      });

      var rawDeleteHasArmQuery = SparqlResource.get({
        name: 'deleteHasArm.sparql'
      });

      function editArm(arm) {
        var defer = $q.defer();
        editArmQuery.$promise.then(function(query) {
          var editArmQuery = query.data.replace(/\$armURI/g, arm.armURI.value)
            .replace('$newArmLabel', arm.label.value)
            .replace('$newArmComment', arm.comment.value);
          defer.resolve(StudyService.doQuery(editArmQuery));
        });
        return defer.promise;
      }

      function deleteArm(arm) {
        var defer = $q.defer();

        $q.all([rawDeleteArmQuery.$promise, rawDeleteHasArmQuery.$promise]).then(function() {
          var deleteArmQuery = rawDeleteArmQuery.data.replace(/\$armURI/g, arm.armURI.value);
          var deleteHasArmQuery = rawDeleteHasArmQuery.data.replace(/\$armURI/g, arm.armURI.value);
          defer.resolve($q.all([StudyService.doQuery(deleteArmQuery),
            StudyService.doQuery(deleteHasArmQuery)
          ]));
        });
        return defer.promise;
      }

      return {
        editArm: editArm,
        deleteArm: deleteArm
      };
    };

    return dependencies.concat(ArmService);
  });

'use strict';
define([],
  function() {
    var dependencies = ['$q', '$resource', 'StudyService', 'SparqlResource'];
    var ArmService = function($q, $resource, StudyService, SparqlResource) {

      var editArmQuery = SparqlResource.get({
        name: 'editArmWithComment.sparql'
      });

      var deleteArmQuery = SparqlResource.get({
        name: 'deleteArm.sparql'
      });

      var deleteHasArmQuery = SparqlResource.get({
        name: 'deleteHasArm.sparql'
      });

      function editArm(arm) {
        var defer = $q.defer();
        editArmQuery.$promise.then(function(query) {
          var editArmQuery = query.data.replace(/\$armURI/g, arm.armURI.value)
            .replace('$newArmLabel', arm.label.value)
            .replace('$newArmComment', arm.comment.value);
          defer.resolve(StudyService.doQuery(editArmQuery));
        })
        return defer.promise;
      }

      function deleteArm(arm) {
        var defer = $q.defer();
        deleteArmQuery.$promise.then(function(query) {
          var deleteArmQuery = query.data.replace(/\$armURI/g, arm.armURI.value);
          defer.resolve(StudyService.doQuery(deleteArmQuery));
        });
        return defer.promise;
      }

      function deleteHasArm(arm) {
        var defer = $q.defer();
        deleteHasArmQuery.$promise.then(function(query) {
          var deleteHasArmQuery = query.data.replace(/\$armURI/g, arm.armURI.value);
          defer.resolve(StudyService.doQuery(deleteHasArmQuery));
        });
        return defer.promise;

      }

      return {
        editArm: editArm,
        deleteArm: deleteArm,
        deleteHasArm: deleteHasArm
      };
    };

    return dependencies.concat(ArmService);
  });

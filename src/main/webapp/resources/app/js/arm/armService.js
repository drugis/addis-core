'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource'];
    var ArmService = function($q, StudyService, SparqlResource) {

      var editArmWithCommentQuery = SparqlResource.get({
        name: 'editArmWithComment.sparql'
      });

      var editArmWithoutCommentQuery = SparqlResource.get({
        name: 'editArmWithoutComment.sparql'
      });

      var rawDeleteArmQuery = SparqlResource.get({
        name: 'deleteArm.sparql'
      });

      var rawDeleteHasArmQuery = SparqlResource.get({
        name: 'deleteHasArm.sparql'
      });

      function editArm(arm) {
        var defer = $q.defer();
        if (arm.comment) {
          editArmWithCommentQuery.$promise.then(function(query) {
            var editArmWithCommentQuery = query.data.replace(/\$armURI/g, arm.armURI.value)
              .replace('$newArmLabel', arm.label.value)
              .replace('$newArmComment', arm.comment.value);
            defer.resolve(StudyService.doModifyingQuery(editArmWithCommentQuery));
          });
        } else {
          editArmWithoutCommentQuery.$promise.then(function(query) {
            var editArmWithoutCommentQuery = query.data.replace(/\$armURI/g, arm.armURI.value)
              .replace('$newArmLabel', arm.label.value);
            defer.resolve(StudyService.doModifyingQuery(editArmWithoutCommentQuery));
          });
        }
        return defer.promise;
      }

      function deleteArm(arm) {
        var defer = $q.defer();

        $q.all([rawDeleteArmQuery.$promise, rawDeleteHasArmQuery.$promise]).then(function() {
          var deleteArmQuery = rawDeleteArmQuery.data.replace(/\$armURI/g, arm.armURI.value);
          var deleteHasArmQuery = rawDeleteHasArmQuery.data.replace(/\$armURI/g, arm.armURI.value);
          defer.resolve($q.all([StudyService.doModifyingQuery(deleteArmQuery),
            StudyService.doModifyingQuery(deleteHasArmQuery)
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

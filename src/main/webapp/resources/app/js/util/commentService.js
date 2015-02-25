'use strict';
define([],
  function() {
    var dependencies = ['StudyService', 'SparqlResource'];
    var commentService = function(StudyService, SparqlResource) {

      var addCommentTemplate = SparqlResource.get('addComment.sparql');

      function addComment(instanceUuid, comment) {
        return addCommentTemplate.then(function(template) {
          var query = template.replace(/\$instanceUuid/g, instanceUuid)
            .replace(/\$comment/g, comment);
          return StudyService.doModifyingQuery(query);
        });
      }

      return {
        addComment: addComment,
      };
    };
    return dependencies.concat(commentService);
  });

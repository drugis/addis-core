'use strict';
define([],
  function() {
    var dependencies = ['StudyService', 'SparqlResource'];
    var commentService = function(StudyService, SparqlResource) {

      var addCommentTemplate = SparqlResource.get('addComment.sparql');

      function addComment(itemUri, comment) {
        return addCommentTemplate.then(function(template) {
          var query = template.replace(/\$itemUri/g, itemUri)
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

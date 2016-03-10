'use strict';
define([], function() {
  var depencencies = ['DatasetResource'];
  var FeaturedDatasetDirective = function(DatasetResource) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/dataset/featuredDatasetDirective.html',
      scope: {
        datasetUuid: '=',
        userUid: '='
      },
      link: function(scope, element) {
        DatasetResource.getForJson({
          datasetUUID: scope.datasetUuid,
          userUid: scope.userUid
        }).$promise.then(function(response) {
          scope.dataset = {
            datasetUri: scope.datasetUuid,
            label: response['http://purl.org/dc/terms/title'],
            comment: response['http://purl.org/dc/terms/description'],
            creator: response['http://purl.org/dc/terms/creator']
          };

          scope.dataset.comment = 'asdfads sadfdsaf fdafdafdsfdfasdfdsfdfdfdsafd dafdafdsfdfdffaf fdfdffdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf dfdfdfdsfdfdsaf fdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf dfdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf dfdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf dfdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf dfdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf dfdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf dfdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf dfdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf dfdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf dfdfdsfdfdsfdsfdsfd dfdf fdfdfdsfdsfd dfdfdfbdjhfbdfd ddf dff dfdfdfdsfdfdsfdf d';
          scope.$watch('dataset.comment', function(){
            var commentElement = element.find('.featured-dataset-comment')[0];
            if (commentElement.offsetHeight < commentElement.scrollHeight ||
              commentElement.offsetWidth < commentElement.scrollWidth) {
              console.log('your element have overflow');
              $(commentElement).append('...'); 

            } else {
              console.log('your element doesn\'t have overflow');
            }
          });


        });
      }
    };
  };
  return depencencies.concat(FeaturedDatasetDirective);
});

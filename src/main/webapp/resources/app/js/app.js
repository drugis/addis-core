'use strict';
define(
  ['angular',
    'require',
    'jQuery',
    'mcda/config',
    'foundation'
  ],
  function(angular, require, $, Config) {
    var dependencies = [
      'ui.router'
    ];

    var app = angular.module('trialverse', dependencies);


    app.run(['$rootScope', '$window', '$http',
      function($rootScope, $window, $http) {
        var csrfToken = $window.config._csrf_token;
        var csrfHeader = $window.config._csrf_header;

        $http.defaults.headers.common[csrfHeader] = csrfToken;
        $rootScope.$on('$viewContentLoaded', function() {
          $(document).foundation();
        });

//        $rootScope.$safeApply = function($scope, fn) {
//          var phase = $scope.$root.$$phase;
//          if (phase === '$apply' || phase === '$digest') {
//            this.$eval(fn);
//          } else {
//            this.$apply(fn);
//          }
//        };

      }
    ]);

    app.config([$stateProvider', '$urlRouterProvider' '$httpProvider',
      function($stateProvider, $urlRouterProvider, $httpProvider,  ) {
        var baseTemplatePath = 'app/views/';

        $stateProvider
          .state('hello', {
            url: '/hello',
            templateUrl: baseTemplatePath + 'hello.html'
          });

        // Default route
        $urlRouterProvider.otherwise('/hello');
      }
    ]);

    return app;
  });
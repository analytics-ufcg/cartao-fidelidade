(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .directive('cfNavbar', cfNavbar);

    cfNavbar.$inject = ['$window'];

    /*jshint latedef: nofunc */
    function cfNavbar($window) {
      return {
        templateUrl: "views/directives/cf-navbar.html",
        restrict: 'E',
        scope: {
          scroll: '=scrollPosition'
        },
        link: function(scope, element, attrs) {
          var windowEl = angular.element($window);
          var handler = function() {
            scope.scroll = windowEl.scrollTop();
          }
          windowEl.on('scroll', scope.$apply.bind(scope, handler));
          handler();
        }
      }
    }

})();

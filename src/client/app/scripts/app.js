(function() {
  'use strict';

  /**
   * @ngdoc overview
   * @name cartaoFidelidadeApp
   * @description
   * # cartaoFidelidadeApp
   *
   * Main module of the application.
   */
  angular
    .module('cartaoFidelidadeApp', ['ngResource', 'ui.router', 'ui.bootstrap'])
    .constant('RESTAPI', {
      url: 'http://localhost:8080/'
    })
    .config(routeConfig);

  routeConfig.$inject = ['$stateProvider', '$urlRouterProvider'];

  /*jshint latedef: nofunc */
  function routeConfig($stateProvider, $urlRouterProvider) {
    $stateProvider
    .state('home', {
      url: "/",
      templateUrl: "views/main.html",
      controller: "MainCtrl",
      controllerAs: "main"
    });
    $urlRouterProvider.otherwise('/');
  }
})();

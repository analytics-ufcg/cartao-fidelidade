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
      url: 'http://localhost:8080'
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
    })
    .state('fornecedor', {
      url: "/fornecedor",
      templateUrl: "views/fornecedor.html",
      controller: "FornecedorCtrl",
      controllerAs: "ctrl"
    })
    .state('overview', {
      url: "/overview",
      templateUrl: "views/overview.html",
      controller: "OverviewCtrl",
      controllerAs: "ctrl"
    });
    $urlRouterProvider.otherwise('/');
  }
})();

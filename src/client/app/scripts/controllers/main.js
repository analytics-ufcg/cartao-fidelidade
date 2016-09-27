(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .controller('MainCtrl', MainCtrl);

  MainCtrl.$inject = ['$location', '$anchorScroll', '$state', 'Fornecedores'];

  /*jshint latedef: nofunc */
  function MainCtrl($location, $anchorScroll, $state, Fornecedores) {
    var vm = this;
    vm.fornecedores = [];
    vm.cpfCnpj = "";
    vm.ano = "2008";
    vm.tipo = "1";
    vm.getFornecedores = getFornecedores;
    vm.search = search;
    vm.gotoBottom = gotoBottom;

    vm.fornecedores = Fornecedores.ranked.query({"ano": vm.ano, "tipo": vm.tipo});

    function getFornecedores() {
      vm.fornecedores = Fornecedores.ranked.query({"ano": vm.ano, "tipo": vm.tipo});
    }

    function search() {
      $state.go('fornecedor', {'cpfCnpj': vm.cpfCnpj});
    }

    function gotoBottom() {
      $location.hash('ranking');
      $anchorScroll();
    };

  }
})();

(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .controller('MainCtrl', MainCtrl);

  MainCtrl.$inject = ['Fornecedores'];

  /*jshint latedef: nofunc */
  function MainCtrl(Fornecedores) {
    var vm = this;
    vm.fornecedores = [];
    vm.getFornecedores = getFornecedores;
    vm.ano = "2008";

    vm.fornecedores = Fornecedores.ranked.query({"ano": vm.ano, "tipo": 1});

    function getFornecedores() {
      vm.fornecedores = Fornecedores.ranked.query({"ano": vm.ano, "tipo": 1});
    }
  }
})();

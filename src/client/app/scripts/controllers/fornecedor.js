(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .controller('FornecedorCtrl', FornecedorCtrl);

  FornecedorCtrl.$inject = ['Fornecedores', '$stateParams'];

  /*jshint latedef: nofunc */
  function FornecedorCtrl(Fornecedores, $stateParams) {
    var vm = this;
    vm.fornecedor = {};

    vm.fornecedor = Fornecedores.simples.get({"cpfCnpj": $stateParams.cpfCnpj, "tipo": 1});
  }
})();

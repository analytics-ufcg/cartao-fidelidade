(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .controller('FornecedorCtrl', FornecedorCtrl);

  FornecedorCtrl.$inject = ['Fornecedores', '$stateParams'];

  /*jshint latedef: nofunc */
  function FornecedorCtrl(Fornecedores, $stateParams) {
    var vm = this;
    vm.fornecedor = {};
    vm.cpfCnpj = $stateParams.cpfCnpj;

    vm.fornecedor = Fornecedores.simples.get({"cpfCnpj": vm.cpfCnpj, "ano": 2008, "tipo": 1});
  }
})();

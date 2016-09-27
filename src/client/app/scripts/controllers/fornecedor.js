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
    vm.ano = "2008";
    vm.tipo = "1";
    vm.municipioSelecionado = {};
    vm.getFornecedores = getFornecedores;
    vm.selectMunicipio = selectMunicipio;

    vm.fornecedor = Fornecedores.simples.get({"cpfCnpj": vm.cpfCnpj, "ano": vm.ano, "tipo": vm.tipo});

    function getFornecedores() {
      vm.fornecedor = Fornecedores.simples.get({"cpfCnpj": vm.cpfCnpj, "ano": vm.ano, "tipo": vm.tipo});
    }

    function selectMunicipio(id) {
      for (var i = 0; i < vm.fornecedor.municipios.length; i++) {
        if (vm.fornecedor.municipios[i].codMunicipio === id+"") {
          vm.municipioSelecionado = vm.fornecedor.municipios[i];
          break;
        }
      }
    }

  }
})();
